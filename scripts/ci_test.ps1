# ============================================================
# Delta AI Customer Service - 持续集成自动化测试脚本
# @author 刘建国
# 功能：构建、部署、测试全流程自动化
# 使用：.\ci_test.ps1 [-SkipBuild] [-SkipTest] [-SkipReport]
#      适用于CI/CD流水线或本地开发验证
# ============================================================
param(
    [switch]$SkipBuild,
    [switch]$SkipTest,
    [switch]$SkipReport,
    [string]$ProjectRoot = "D:\Project\AI-SERVERS",
    [string]$BaseUrl = "http://localhost:8080/api/v1",
    [int]$BuildTimeout = 300,
    [int]$StartupTimeout = 120
)

$ErrorActionPreference = "Continue"
$startTime = Get-Date
$allPassed = $true
$steps = @()

function Write-Step {
    param($msg, $status="")
    $prefix = "[$([datetime]::Now.ToString('HH:mm:ss'))]"
    $statusStr = if ($status) { " <$status>" } else { "" }
    Write-Host "$prefix $msg$statusStr" -ForegroundColor Cyan
}

function Write-Result {
    param($check, $passed, $detail="")
    $icon = if ($passed) { "[PASS]" } else { "[FAIL]" }
    $color = if ($passed) { "Green" } else { "Red" }
    $detailStr = if ($detail) { " - $detail" } else { "" }
    Write-Host "  $icon $check$detailStr" -ForegroundColor $color
    return @{ Check=$check; Passed=$passed; Detail=$detail }
}

# ============================================================
# Step 1: Build & Compile
# ============================================================
Write-Step "Step 1: Build & Compile"

if (-not $SkipBuild) {
    Push-Location $ProjectRoot
    $buildOutput = mvn clean package -DskipTests -q 2>&1
    $buildExit = $LASTEXITCODE
    Pop-Location
    
    if ($buildExit -eq 0) {
        $steps += Write-Result "Maven Build" $true
    } else {
        $steps += Write-Result "Maven Build" $false "Exit code: $buildExit"
        $allPassed = $false
        if (-not $SkipTest) { exit 1 }
    }
} else {
    Write-Host "  [SKIP] Build skipped" -ForegroundColor Yellow
}

# ============================================================
# Step 2: Database Check
# ============================================================
Write-Step "Step 2: Database Connectivity"

try {
    $connStr = "Server=localhost;Database=delta_ai_customer_service;Uid=root;Pwd=123456;Port=3306;CharSet=utf8mb4;"
    $conn = New-Object MySql.Data.MySqlClient.MySqlConnection($connStr)
    $conn.Open()
    $cmd = $conn.CreateCommand()
    $cmd.CommandText = "SELECT COUNT(*) FROM sys_user WHERE deleted=0"
    $count = $cmd.ExecuteScalar()
    $conn.Close()
    $steps += Write-Result "MySQL Connection" $true "Users: $count"
} catch {
    $steps += Write-Result "MySQL Connection" $false $_.Exception.Message
    $allPassed = $false
}

# ============================================================
# Step 3: Start Application
# ============================================================
Write-Step "Step 3: Start Application"

$existingPid = (netstat -ano | Select-String ':8080.*LISTENING' | ForEach-Object { ($_ -split '\s+')[-1] } | Select-Object -First 1)
if ($existingPid) {
    taskkill /PID $existingPid /F 2>&1 | Out-Null
    Start-Sleep 3
    Write-Host "  Killed existing process on 8080 (PID $existingPid)" -ForegroundColor Gray
}

$proc = Start-Process java `
    -ArgumentList "-Dspring.profiles.active=dev", "-Dfile.encoding=UTF-8", "-jar", "$ProjectRoot\delta-admin\target\delta-admin-1.0.0-SNAPSHOT.jar" `
    -NoNewWindow -PassThru

Write-Host "  Starting (PID $($proc.Id))..." -ForegroundColor Gray

# Wait for startup
$ready = $false
$sw = [System.Diagnostics.Stopwatch]::StartNew()
while ($sw.ElapsedMilliseconds -lt ($StartupTimeout * 1000)) {
    try {
        $resp = Invoke-WebRequest -Uri "$BaseUrl/auth/login" -Method OPTIONS -TimeoutSec 2 -ErrorAction Stop
        if ($resp.StatusCode -eq 200 -or $resp.StatusCode -eq 405 -or $resp.StatusCode -eq 403) {
            $ready = $true
            break
        }
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 405 -or $_.Exception.Response.StatusCode.value__ -eq 403) {
            $ready = $true
            break
        }
    }
    Start-Sleep 2
}
$sw.Stop()

if ($ready) {
    $steps += Write-Result "Application Startup" $true "$([math]::Round($sw.ElapsedMilliseconds/1000,1))s"
} else {
    $steps += Write-Result "Application Startup" $false "Timeout after ${StartupTimeout}s"
    $allPassed = $false
    if (-not $SkipTest) { exit 1 }
}

# ============================================================
# Step 4: Core API Tests
# ============================================================
Write-Step "Step 4: Core API Tests"

if (-not $SkipTest) {
    try {
        $loginBody = '{"username":"admin","password":"123456"}'
        $loginResp = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method POST `
            -ContentType "application/json;charset=UTF-8" -Body $loginBody
        
        if ($loginResp.code -eq 200) {
            $token = $loginResp.data.token
            $steps += Write-Result "Login" $true
        } else {
            $steps += Write-Result "Login" $false "code=$($loginResp.code)"
            $allPassed = $false
            exit 1
        }
        
        $headers = @{Authorization="Bearer $token"}
        
        # Test all critical endpoints
        $apiTests = @(
            @{Name="users-page";        Url="/sys-users/page?page=1&size=10"},
            @{Name="kw-page";           Url="/keywords/page?page=1&size=10"},
            @{Name="comp-page";         Url="/companions/page?page=1&size=10"},
            @{Name="cust-page";         Url="/customers/page?page=1&size=10"},
            @{Name="ord-page";          Url="/orders/page?page=1&size=10"},
            @{Name="wo-page";           Url="/work-orders/page?page=1&size=10"},
            @{Name="gc-page";           Url="/game-configs/page?page=1&size=10"},
            @{Name="si-page";           Url="/service-items/page?page=1&size=10"},
            @{Name="faq-page";          Url="/faq?page=1&size=10"},
            @{Name="assign-page";       Url="/cs-user-customer/page?page=1&size=10"},
            @{Name="club-1";            Url="/club-configs/1"},
            @{Name="plat-all";          Url="/platform-configs"},
            @{Name="ai-get";            Url="/ai-configs"},
            @{Name="stat-global";       Url="/stats/global"},
            @{Name="stat-personal";     Url="/stats/personal"},
            @{Name="cache-stats";       Url="/cache-stats"}
        )
        
        $apiPassed = 0
        $apiFailed = 0
        $totalMs = 0
        $perfResults = @()
        
        foreach ($test in $apiTests) {
            $sw2 = [System.Diagnostics.Stopwatch]::StartNew()
            try {
                $resp = Invoke-RestMethod -Uri "$BaseUrl$($test.Url)" -Method GET -Headers $headers
                $sw2.Stop()
                $ms = $sw2.ElapsedMilliseconds
                $totalMs += $ms
                $perfResults += @{Name=$test.Name; Time=$ms}
                
                if ($resp.code -eq 200) {
                    $apiPassed++
                } else {
                    $apiFailed++
                    Write-Host "    [FAIL] $($test.Name) => code=$($resp.code)" -ForegroundColor Red
                }
            } catch {
                $sw2.Stop()
                $ms = $sw2.ElapsedMilliseconds
                $apiFailed++
                Write-Host "    [FAIL] $($test.Name) => ERR ($($ms)ms)" -ForegroundColor Red
            }
        }
        
        $avgApiMs = if ($perfResults.Count -gt 0) { [math]::Round(($perfResults | Measure-Object -Property Time -Average).Average, 1) } else { 0 }
        $steps += Write-Result "API Tests" ($apiFailed -eq 0) "Passed:$apiPassed Failed:$apiFailed Avg:${avgApiMs}ms"
        
        if ($apiFailed -gt 0) { $allPassed = $false }
        
    } catch {
        $steps += Write-Result "API Tests" $false "Script error: $($_.Exception.Message)"
        $allPassed = $false
    }
} else {
    Write-Host "  [SKIP] Tests skipped" -ForegroundColor Yellow
}

# ============================================================
# Step 5: Performance Validation
# ============================================================
Write-Step "Step 5: Performance Validation"

if ($perfResults.Count -gt 0) {
    $sorted = $perfResults | Sort-Object Time
    $p95idx = [int]($sorted.Count * 0.95)
    if ($p95idx -lt 0) { $p95idx = 0 }
    if ($p95idx -ge $sorted.Count) { $p95idx = $sorted.Count - 1 }
    $p95 = $sorted[$p95idx].Time
    $max = ($perfResults | Measure-Object -Property Time -Maximum).Maximum
    
    $perfOk = $max -lt 1000
    $steps += Write-Result "Response Times" $perfOk "Max:${max}ms P95:${p95}ms Avg:${avgApiMs}ms"
    if (-not $perfOk) { $allPassed = $false }
}

# ============================================================
# Step 6: Generate Report
# ============================================================
Write-Step "Step 6: Generate Report"

$reportDir = "$ProjectRoot\reports"
if (-not (Test-Path $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
}

$dateStr = Get-Date -Format "yyyyMMdd_HHmmss"
$elapsed = [math]::Round(((Get-Date) - $startTime).TotalSeconds, 1)

$statusEmoji = if ($allPassed) { "HEALTHY" } else { "UNHEALTHY" }
$statusColor = if ($allPassed) { "#28a745" } else { "#dc3545" }

$reportJson = @{
    Timestamp = (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
    Status = $statusEmoji
    AllPassed = $allPassed
    ElapsedSec = $elapsed
    Steps = $steps
    PerfAvgMs = $avgApiMs
    PerfMaxMs = $max
    PerfP95Ms = $p95
} | ConvertTo-Json -Depth 3

$reportPath = "$reportDir\ci_report_$dateStr.json"
$reportJson | Out-File $reportPath -Encoding UTF8

$summaryPath = "$reportDir\latest.txt"
@"
Delta AI CI Test Report
=======================
Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Status: $statusEmoji
Duration: ${elapsed}s
API: $apiPassed passed, $apiFailed failed, Avg=${avgApiMs}ms, Max=${max}ms
"@ | Out-File $summaryPath -Encoding UTF8

Write-Host "  Report saved: $reportPath" -ForegroundColor Cyan

# ============================================================
# Final Summary
# ============================================================
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "  CI/CD Pipeline Complete" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
$finalColor = if ($allPassed) { "Green" } else { "Red" }
Write-Host "  Status: $statusEmoji" -ForegroundColor $finalColor
Write-Host "  Duration: ${elapsed}s" -ForegroundColor White
Write-Host "============================================" -ForegroundColor Cyan

# Cleanup
if ($proc) {
    Write-Host "`nStopping application..." -ForegroundColor Gray
    $proc.Kill()
}

exit $(if ($allPassed) { 0 } else { 1 })
