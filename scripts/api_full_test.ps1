# ============================================================
# Delta AI Customer Service - Full API Functional Test Script
# @author 刘建国
# ============================================================
param(
    [string]$BaseUrl = "http://localhost:8080/api/v1"
)

# Result container (hashtable to bypass scope issues)
$g = @{}
$g.Total = 0
$g.Passed = 0
$g.Failed = 0
$g.Fails = @()
$g.Perf = @()

function Get-Token {
    try {
        $body = '{"username":"admin","password":"123456"}'
        $resp = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method POST -ContentType "application/json;charset=UTF-8" -Body $body
        return $resp.data.token
    } catch {
        Write-Host "[FATAL] Login Failed" -ForegroundColor Red
        exit 1
    }
}

$token = Get-Token
$headers = @{Authorization="Bearer $token"; "Content-Type"="application/json;charset=UTF-8"}

function Test-GET {
    param($label, $path, $expectCode=200)
    $g.Total++
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $r = Invoke-RestMethod -Uri "$BaseUrl$path" -Method GET -Headers $headers
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $g.Perf += New-Object PSObject -Property @{Name=$label; Method="GET"; Time=$ms}
        if ($r.code -eq $expectCode) {
            $g.Passed++
            Write-Host "  [PASS] $label ($($ms)ms)" -ForegroundColor Green
        } else {
            $g.Failed++
            $g.Fails += "[FAIL] $label => code=$($r.code) msg=$($r.message)"
            Write-Host "  [FAIL] $label => code=$($r.code) msg=$($r.message)" -ForegroundColor Red
        }
    } catch {
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $errCode = 0
        try { $errCode = $_.Exception.Response.StatusCode.value__ } catch {}
        $g.Failed++
        $g.Fails += "[FAIL] $label => HTTP_$errCode"
        Write-Host "  [FAIL] $label => HTTP_$errCode ($($ms)ms)" -ForegroundColor Red
    }
}

function Test-POST {
    param($label, $path, $body, $expectCode=200)
    $g.Total++
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $jsonBody = if ($body -is [string]) { $body } else { $body | ConvertTo-Json -Depth 5 -Compress }
        $r = Invoke-RestMethod -Uri "$BaseUrl$path" -Method POST -Headers $headers -Body $jsonBody
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $g.Perf += New-Object PSObject -Property @{Name=$label; Method="POST"; Time=$ms}
        if ($r.code -eq $expectCode) {
            $g.Passed++
            Write-Host "  [PASS] $label ($($ms)ms)" -ForegroundColor Green
            return $r
        } else {
            $g.Failed++
            $g.Fails += "[FAIL] $label => code=$($r.code) msg=$($r.message)"
            Write-Host "  [FAIL] $label => code=$($r.code) msg=$($r.message)" -ForegroundColor Red
            return $null
        }
    } catch {
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $errCode = 0
        try { $errCode = $_.Exception.Response.StatusCode.value__ } catch {}
        $g.Failed++
        $g.Fails += "[FAIL] $label => HTTP_$errCode"
        Write-Host "  [FAIL] $label => HTTP_$errCode ($($ms)ms)" -ForegroundColor Red
        return $null
    }
}

function Test-PUT {
    param($label, $path, $body, $expectCode=200)
    $g.Total++
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $jsonBody = if ($body -is [string]) { $body } else { $body | ConvertTo-Json -Depth 5 -Compress }
        $r = Invoke-RestMethod -Uri "$BaseUrl$path" -Method PUT -Headers $headers -Body $jsonBody
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $g.Perf += New-Object PSObject -Property @{Name=$label; Method="PUT"; Time=$ms}
        if ($r.code -eq $expectCode) {
            $g.Passed++
            Write-Host "  [PASS] $label ($($ms)ms)" -ForegroundColor Green
        } else {
            $g.Failed++
            $g.Fails += "[FAIL] $label => code=$($r.code) msg=$($r.message)"
            Write-Host "  [FAIL] $label => code=$($r.code) msg=$($r.message)" -ForegroundColor Red
        }
    } catch {
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $errCode = 0
        try { $errCode = $_.Exception.Response.StatusCode.value__ } catch {}
        $g.Failed++
        $g.Fails += "[FAIL] $label => HTTP_$errCode"
        Write-Host "  [FAIL] $label => HTTP_$errCode ($($ms)ms)" -ForegroundColor Red
    }
}

function Test-DELETE {
    param($label, $path, $expectCode=200)
    $g.Total++
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $r = Invoke-RestMethod -Uri "$BaseUrl$path" -Method DELETE -Headers $headers
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $g.Perf += New-Object PSObject -Property @{Name=$label; Method="DELETE"; Time=$ms}
        if ($r.code -eq $expectCode) {
            $g.Passed++
            Write-Host "  [PASS] $label ($($ms)ms)" -ForegroundColor Green
        } else {
            $g.Failed++
            $g.Fails += "[FAIL] $label => code=$($r.code) msg=$($r.message)"
            Write-Host "  [FAIL] $label => code=$($r.code) msg=$($r.message)" -ForegroundColor Red
        }
    } catch {
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $errCode = 0
        try { $errCode = $_.Exception.Response.StatusCode.value__ } catch {}
        $g.Failed++
        $g.Fails += "[FAIL] $label => HTTP_$errCode"
        Write-Host "  [FAIL] $label => HTTP_$errCode ($($ms)ms)" -ForegroundColor Red
    }
}

# Helper for building URL with query params (avoids & interpretation)
function Build-Url {
    param($path, $params = @{})
    $q = @()
    foreach ($k in $params.Keys) {
        $q += "$k=$($params[$k])"
    }
    $qs = $q -join "&"
    if ($qs) { return "$path?$qs" } else { return $path }
}

# ============================================================
# TEST SUITE
# ============================================================
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Delta AI - Full API Functional Test" -ForegroundColor Cyan
Write-Host "  Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# ---- Group 1: Auth ----
Write-Host "`n[1] Auth Module" -ForegroundColor Yellow
Test-POST "login-ok" "/auth/login" '{"username":"admin","password":"123456"}'
Test-POST "login-badpw" "/auth/login" '{"username":"admin","password":"wrong"}' 500
Test-POST "login-empty" "/auth/login" '{"username":"admin","password":""}' 500

# ---- Group 2: Sys Users ----
Write-Host "`n[2] Sys Users" -ForegroundColor Yellow
Test-GET "users-page" (Build-Url "/sys-users/page" @{page=1;size=10})
Test-GET "users-cs-list" "/sys-users/cs-users"
Test-GET "users-profile" "/sys-users/profile"
Test-POST "users-add" "/sys-users" @{username="cs_test99";password="123456";nickname="TestCS";role="CS_STAFF";email="t@t.com"}

# ---- Group 3: Keywords ----
Write-Host "`n[3] Keywords" -ForegroundColor Yellow
Test-GET "kw-page" (Build-Url "/keywords/page" @{page=1;size=10})
Test-GET "kw-get-1" "/keywords/1"
Test-POST "kw-add" "/keywords" @{keyword="newKeyword";category="test";severity="LOW"}
Test-PUT "kw-update" "/keywords" @{id=1;keyword="updated";category="upd";severity="HIGH"}
Test-DELETE "kw-del-999" "/keywords/99999" 500

# ---- Group 4: Companions ----
Write-Host "`n[4] Companions" -ForegroundColor Yellow
Test-GET "comp-page" (Build-Url "/companions/page" @{page=1;size=10})
Test-GET "comp-get-1" "/companions/1"
Test-GET "comp-all" "/companions/all"
Test-POST "comp-add" "/companions" @{nickname="NewComp";levelId=1;gameId=1;description="new"}

# ---- Group 5: Companion Levels ----
Write-Host "`n[5] Companion Levels" -ForegroundColor Yellow
Test-GET "lev-all" "/companion-levels"
Test-GET "lev-get-1" "/companion-levels/1"
Test-GET "lev-enabled" "/companion-levels/enabled"
Test-POST "lev-add" "/companion-levels" @{levelName="Diamond";levelCode="DIAMOND";basePrice=500;sortOrder=5}

# ---- Group 6: Customers ----
Write-Host "`n[6] Customers" -ForegroundColor Yellow
Test-GET "cust-page" (Build-Url "/customers/page" @{page=1;size=10})
Test-GET "cust-get-1" "/customers/1"
Test-GET "cust-platform" (Build-Url "/customers/page" @{page=1;size=10;platform="WECHAT"})

# ---- Group 7: Orders ----
Write-Host "`n[7] Orders" -ForegroundColor Yellow
Test-GET "ord-page" (Build-Url "/orders/page" @{page=1;size=10})
Test-GET "ord-get-1" "/orders/1"

# ---- Group 8: Work Orders ----
Write-Host "`n[8] Work Orders" -ForegroundColor Yellow
Test-GET "wo-page" (Build-Url "/work-orders/page" @{page=1;size=10})
Test-GET "wo-get-1" "/work-orders/1"
Test-GET "wo-stats" "/work-orders/stats"

# ---- Group 9: Game Configs ----
Write-Host "`n[9] Game Configs" -ForegroundColor Yellow
Test-GET "gc-page" (Build-Url "/game-configs/page" @{page=1;size=10})
Test-GET "gc-get-1" "/game-configs/1"
Test-GET "gc-club" "/game-configs/club/1"

# ---- Group 10: Service Items ----
Write-Host "`n[10] Service Items" -ForegroundColor Yellow
Test-GET "si-page" (Build-Url "/service-items/page" @{page=1;size=10})
Test-GET "si-get-1" "/service-items/1"
Test-GET "si-club" "/service-items/club/1"

# ---- Group 11: Club Config ----
Write-Host "`n[11] Club Config" -ForegroundColor Yellow
Test-GET "club-get" "/club-configs/1"
Test-PUT "club-upd" "/club-configs" @{id=1;clubName="Delta";mainGames="LOL";serviceSlogan="Pro"}

# ---- Group 12: Platform Config ----
Write-Host "`n[12] Platform Config" -ForegroundColor Yellow
Test-GET "plat-all" "/platform-configs"

# ---- Group 13: AI Config ----
Write-Host "`n[13] AI Config" -ForegroundColor Yellow
Test-GET "ai-get" "/ai-configs"

# ---- Group 14: FAQ ----
Write-Host "`n[14] FAQ" -ForegroundColor Yellow
Test-GET "faq-page" (Build-Url "/faq" @{page=1;size=10})
Test-GET "faq-get-1" "/faq/1"
Test-POST "faq-add" "/faq" @{category="general";question="test?";answer="answer"}

# ---- Group 15: Stats ----
Write-Host "`n[15] Stats" -ForegroundColor Yellow
Test-GET "stat-global" "/stats/global"
Test-GET "stat-personal" "/stats/personal"
Test-GET "stat-team" "/stats/team"

# ---- Extra: CS Assignment + Cache ----
Write-Host "`n[Extra]" -ForegroundColor Yellow
Test-GET "assign-page" (Build-Url "/cs-user-customer/page" @{page=1;size=10})
Test-GET "assign-get-1" "/cs-user-customer/1"
Test-GET "cache-info" "/cache-stats"

# ============================================================
# Summary
# ============================================================
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "  TEST SUMMARY" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
$rate = 0
if ($g.Total -gt 0) { $rate = [math]::Round($g.Passed/$g.Total*100,1) }
Write-Host "  Total: $($g.Total)  Passed: $($g.Passed)  Failed: $($g.Failed)" -ForegroundColor White
$color = if ($rate -ge 90) { 'Green' } else { 'Yellow' }
Write-Host "  Rate: ${rate}%" -ForegroundColor $color
Write-Host "==========================================" -ForegroundColor Cyan

if ($g.Fails.Count -gt 0) {
    Write-Host "`nFail Details:" -ForegroundColor Red
    foreach ($d in $g.Fails) { Write-Host "  $d" -ForegroundColor Red }
}

if ($g.Perf.Count -gt 0) {
    $avg = [math]::Round(($g.Perf | Measure-Object -Property Time -Average).Average, 1)
    $max = [math]::Round(($g.Perf | Measure-Object -Property Time -Maximum).Maximum, 1)
    $sorted = $g.Perf | Sort-Object Time
    $p95idx = [int]($sorted.Count * 0.95)
    if ($p95idx -lt 0) { $p95idx = 0 }
    if ($p95idx -ge $sorted.Count) { $p95idx = $sorted.Count - 1 }
    $p95 = [math]::Round($sorted[$p95idx].Time, 1)
    Write-Host "`nPerf: Avg=${avg}ms Max=${max}ms P95=${p95}ms" -ForegroundColor Yellow
    $slow = $g.Perf | Where-Object { $_.Time -gt 100 } | Sort-Object Time -Descending
    if ($slow.Count -gt 0) {
        Write-Host "Slow queries (>100ms):" -ForegroundColor DarkYellow
        foreach ($s in $slow) { Write-Host "  [$($s.Method)] $($s.Name) => $($s.Time)ms" -ForegroundColor DarkYellow }
    }
}

# Report file
$rptPath = "D:\Project\AI-SERVERS\scripts\api_test_report.txt"
$txt = @"
Delta AI - API Test Report
===========================
Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
URL: $BaseUrl
Result: $($g.Total) tests, $($g.Passed) passed, $($g.Failed) failed (${rate}%)
Perf: Avg=${avg}ms Max=${max}ms P95=${p95}ms
Fails:
$($g.Fails -join "`n")
"@
$txt | Out-File $rptPath -Encoding UTF8
Write-Host "Report saved: $rptPath" -ForegroundColor Cyan
