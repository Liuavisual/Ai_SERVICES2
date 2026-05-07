# ============================================================
# Delta AI Customer Service - 自动化健康检查与监控脚本
# @author 刘建国
# 功能：定时健康检查、性能监控、自动报警
# 使用：powershell -File health_check.ps1 [-BaseUrl http://localhost:8080/api/v1]
# 配合：Windows 任务计划程序 或 cron 实现定时执行
# ============================================================
param(
    [string]$BaseUrl = "http://localhost:8080/api/v1",
    [int]$TimeoutSec = 30,
    [string]$ReportDir = "D:\Project\AI-SERVERS\reports",
    [double]$AlertThresholdMs = 500
)

$ErrorActionPreference = "Continue"
$startTime = Get-Date
$results = @()
$allPassed = $true
$totalMs = 0

# ============================================================
# Health Check Endpoints Definition
# ============================================================
$checks = @(
    # [Category] Critical Health Checks
    @{Name="login";         Method="POST"; Path="/auth/login";   Body='{"username":"admin","password":"123456"}'; Expect=200},
    @{Name="users-page";    Method="GET";  Path="/sys-users/page?page=1&size=10";                                    Expect=200},
    @{Name="kw-page";       Method="GET";  Path="/keywords/page?page=1&size=10";                                    Expect=200},
    @{Name="comp-page";     Method="GET";  Path="/companions/page?page=1&size=10";                                  Expect=200},
    @{Name="cust-page";     Method="GET";  Path="/customers/page?page=1&size=10";                                   Expect=200},
    @{Name="ord-page";      Method="GET";  Path="/orders/page?page=1&size=10";                                      Expect=200},
    @{Name="wo-page";       Method="GET";  Path="/work-orders/page?page=1&size=10";                                 Expect=200},
    @{Name="gc-page";       Method="GET";  Path="/game-configs/page?page=1&size=10";                                Expect=200},
    @{Name="si-page";       Method="GET";  Path="/service-items/page?page=1&size=10";                               Expect=200},
    @{Name="faq-page";      Method="GET";  Path="/faq?page=1&size=10";                                              Expect=200},
    @{Name="assign-page";   Method="GET";  Path="/cs-user-customer/page?page=1&size=10";                            Expect=200},
    # [Category] Detail Checks
    @{Name="kw-detail";     Method="GET";  Path="/keywords/1";                                                      Expect=200},
    @{Name="comp-detail";   Method="GET";  Path="/companions/1";                                                    Expect=200},
    @{Name="cust-detail";   Method="GET";  Path="/customers/1";                                                     Expect=200},
    @{Name="ord-detail";    Method="GET";  Path="/orders/1";                                                        Expect=200},
    @{Name="wo-detail";     Method="GET";  Path="/work-orders/1";                                                   Expect=200},
    @{Name="gc-detail";     Method="GET";  Path="/game-configs/1";                                                  Expect=200},
    @{Name="si-detail";     Method="GET";  Path="/service-items/1";                                                 Expect=200},
    @{Name="faq-detail";    Method="GET";  Path="/faq/1";                                                           Expect=200},
    # [Category] Config & Stats
    @{Name="club-config";   Method="GET";  Path="/club-configs/1";                                                  Expect=200},
    @{Name="platform-cfg";  Method="GET";  Path="/platform-configs";                                                Expect=200},
    @{Name="ai-config";     Method="GET";  Path="/ai-configs";                                                      Expect=200},
    @{Name="stat-global";   Method="GET";  Path="/stats/global";                                                    Expect=200},
    @{Name="stat-personal"; Method="GET";  Path="/stats/personal";                                                  Expect=200},
    @{Name="stat-team";     Method="GET";  Path="/stats/team";                                                      Expect=200},
    @{Name="wo-stats";      Method="GET";  Path="/work-orders/stats";                                               Expect=200},
    @{Name="cache-stats";   Method="GET";  Path="/cache-stats";                                                     Expect=200}
)

# ============================================================
# Login to get token
# ============================================================
function Get-AuthToken {
    try {
        $loginBody = '{"username":"admin","password":"123456"}'
        $resp = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method POST `
            -ContentType "application/json;charset=UTF-8" -Body $loginBody `
            -TimeoutSec $TimeoutSec
        return $resp.data.token
    } catch {
        Write-Host "[FATAL] Login failed: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

$token = Get-AuthToken
if (-not $token) {
    $results += "[FATAL] Authentication failed - all checks aborted"
    $allPassed = $false
    exit 1
}

$headers = @{
    Authorization = "Bearer $token"
    "Content-Type" = "application/json;charset=UTF-8"
}

# ============================================================
# Execute Health Checks
# ============================================================
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Delta AI - Health Check Report" -ForegroundColor Cyan
Write-Host "  Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "  Target: $BaseUrl" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

foreach ($check in $checks) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $uri = "$BaseUrl$($check.Path)"
        
        if ($check.Method -eq "GET") {
            $resp = Invoke-RestMethod -Uri $uri -Method GET -Headers $headers -TimeoutSec $TimeoutSec
        } elseif ($check.Method -eq "POST") {
            $resp = Invoke-RestMethod -Uri $uri -Method POST -Headers $headers -Body $check.Body -TimeoutSec $TimeoutSec
        }
        
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $totalMs += $ms
        
        $passed = ($resp.code -eq $check.Expect)
        $status = if ($passed) { "OK" } else { "FAIL" }
        $color = if ($passed) { "Green" } else { "Red" }
        
        if ($ms -gt $AlertThresholdMs) {
            $color = "Yellow"
            $status = "SLOW"
            if ($passed) { $allPassed = $false }
        }
        
        $result = @{
            Name = $check.Name
            Method = $check.Method
            Path = $check.Path
            Status = $status
            TimeMs = $ms
            Code = $resp.code
            Message = $resp.message
        }
        $results += $result
        
        $timeStr = "$($ms)ms"
        Write-Host "  [$status] $($check.Name) => $timeStr" -ForegroundColor $color
        
    } catch {
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $errCode = 0
        try { $errCode = $_.Exception.Response.StatusCode.value__ } catch {}
        
        $result = @{
            Name = $check.Name
            Method = $check.Method
            Path = $check.Path
            Status = "ERROR"
            TimeMs = $ms
            Code = $errCode
            Message = $_.Exception.Message
        }
        $results += $result
        $allPassed = $false
        
        Write-Host "  [ERROR] $($check.Name) => HTTP$errCode ($($ms)ms)" -ForegroundColor Red
    }
}

# ============================================================
# Performance Metrics
# ============================================================
$passedCount = ($results | Where-Object { $_.Status -eq "OK" }).Count
$failedCount = ($results | Where-Object { $_.Status -ne "OK" }).Count
$slowCount = ($results | Where-Object { $_.Status -eq "SLOW" }).Count
$avgMs = if ($results.Count -gt 0) { [math]::Round($totalMs / $results.Count, 1) } else { 0 }
$elapsed = [math]::Round(((Get-Date) - $startTime).TotalSeconds, 1)

# ============================================================
# Summary Output
# ============================================================
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Total: $($results.Count) | Passed: $passedCount | Failed: $failedCount | Slow: $slowCount" -ForegroundColor White
Write-Host "  Avg Response: ${avgMs}ms | Total Time: ${elapsed}s" -ForegroundColor White
$healthColor = if ($allPassed) { "Green" } else { "Red" }
$healthText = if ($allPassed) { "HEALTHY" } else { "UNHEALTHY" }
Write-Host "  System Status: $healthText" -ForegroundColor $healthColor
Write-Host "============================================" -ForegroundColor Cyan

# ============================================================
# Generate Report File
# ============================================================
if (-not (Test-Path $ReportDir)) {
    New-Item -ItemType Directory -Path $ReportDir -Force | Out-Null
}

$dateStr = Get-Date -Format "yyyyMMdd_HHmmss"
$jsonReport = "$ReportDir\health_$dateStr.json"
$htmlReport = "$ReportDir\health_$dateStr.html"

# JSON Report
$report = @{
    Timestamp = (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
    Target = $BaseUrl
    Total = $results.Count
    Passed = $passedCount
    Failed = $failedCount
    Slow = $slowCount
    AvgResponseMs = $avgMs
    TotalTimeSec = $elapsed
    SystemHealthy = $allPassed
    Results = $results
}
$report | ConvertTo-Json -Depth 4 | Out-File $jsonReport -Encoding UTF8

# HTML Report
$htmlRows = ""
foreach ($r in $results) {
    $rowColor = switch ($r.Status) {
        "OK"    { "#d4edda" }
        "SLOW"  { "#fff3cd" }
        default { "#f8d7da" }
    }
    $htmlRows += @"
    <tr style="background:$rowColor">
        <td>$($r.Name)</td>
        <td>$($r.Method)</td>
        <td>$($r.Path)</td>
        <td>$($r.Status)</td>
        <td>$($r.TimeMs)ms</td>
        <td>$($r.Code)</td>
    </tr>
"@
}

$healthBadge = if ($allPassed) { 
    '<span style="background:#28a745;color:white;padding:5px 15px;border-radius:3px">HEALTHY</span>' 
} else { 
    '<span style="background:#dc3545;color:white;padding:5px 15px;border-radius:3px">UNHEALTHY</span>' 
}

$html = @"
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>Delta AI - Health Check Report</title>
    <style>
        body{font-family:Arial,sans-serif;margin:20px;background:#f5f5f5}
        .container{max-width:1000px;margin:0 auto;background:white;padding:20px;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1)}
        h1{color:#333;border-bottom:2px solid #007bff;padding-bottom:10px}
        .summary{padding:15px;background:#e9ecef;border-radius:5px;margin:15px 0}
        table{width:100%;border-collapse:collapse;margin-top:15px}
        th{background:#007bff;color:white;padding:10px;text-align:left}
        td{padding:8px;border-bottom:1px solid #ddd}
        .footer{text-align:center;color:#666;margin-top:20px;font-size:12px}
    </style>
</head>
<body>
    <div class="container">
        <h1>Delta AI Customer Service - Health Check Report</h1>
        <p>Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') | Target: $BaseUrl</p>
        <div class="summary">
            <strong>System Status:</strong> $healthBadge<br>
            <strong>Total:</strong> $($results.Count) tests | 
            <strong>Passed:</strong> $passedCount | 
            <strong>Failed:</strong> $failedCount | 
            <strong>Slow:</strong> $slowCount<br>
            <strong>Avg Response:</strong> ${avgMs}ms | 
            <strong>Total Time:</strong> ${elapsed}s
        </div>
        <table>
            <tr>
                <th>Endpoint</th>
                <th>Method</th>
                <th>Path</th>
                <th>Status</th>
                <th>Response</th>
                <th>Code</th>
            </tr>
            $htmlRows
        </table>
        <div class="footer">
            Delta AI Customer Service - Automated Health Check
        </div>
    </div>
</body>
</html>
"@
$html | Out-File $htmlReport -Encoding UTF8

Write-Host "`nReports:" -ForegroundColor Cyan
Write-Host "  JSON: $jsonReport" -ForegroundColor Cyan
Write-Host "  HTML: $htmlReport" -ForegroundColor Cyan

# ============================================================
# Windows Task Scheduler Integration
# ============================================================
Write-Host "`nTo set up automated scheduling (Windows):" -ForegroundColor Yellow
Write-Host "  schtasks /Create /TN 'DeltaAI-HealthCheck' /TR 'powershell -File `"$PSScriptRoot\health_check.ps1`"' /SC HOURLY /MO 1" -ForegroundColor Yellow

exit $(if ($allPassed) { 0 } else { 1 })
