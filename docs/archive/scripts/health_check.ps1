# ============================================================
# Delta AI Customer Service - 自动化健康检查与监控脚本
# @author 刘建国
# 功能：定时健康检查、性能监控、自动报警
# 使用：powershell -File health_check.ps1 [-BaseUrl http://localhost:8080/api/v1]
# 配合：Windows 任务计划程序 或 cron 实现定时执行
# 迁移日期: 2026-05-09
# 迁移原因: 已完成其历史使命，保留供后续运维参考
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

# Health Check Endpoints Definition
$checks = @(
    @{Name="login";         Method="POST"; Path="/auth/login";   Body='{"username":"admin","password":"123456"}'; Expect=200},
    @{Name="users-page";    Method="GET";  Path="/sys-users/page?page=1&size=10"; Expect=200},
    @{Name="kw-page";       Method="GET";  Path="/keywords/page?page=1&size=10"; Expect=200},
    @{Name="comp-page";     Method="GET";  Path="/companions/page?page=1&size=10"; Expect=200},
    @{Name="cust-page";     Method="GET";  Path="/customers/page?page=1&size=10"; Expect=200},
    @{Name="ord-page";      Method="GET";  Path="/orders/page?page=1&size=10"; Expect=200},
    @{Name="wo-page";       Method="GET";  Path="/work-orders/page?page=1&size=10"; Expect=200},
    @{Name="gc-page";       Method="GET";  Path="/game-configs/page?page=1&size=10"; Expect=200},
    @{Name="si-page";       Method="GET";  Path="/service-items/page?page=1&size=10"; Expect=200},
    @{Name="faq-page";      Method="GET";  Path="/faq?page=1&size=10"; Expect=200},
    @{Name="assign-page";   Method="GET";  Path="/cs-user-customer/page?page=1&size=10"; Expect=200}
)

function Get-AuthToken {
    try {
        $loginBody = '{"username":"admin","password":"123456"}'
        $resp = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method POST `
            -ContentType "application/json;charset=UTF-8" -Body $loginBody -TimeoutSec $TimeoutSec
        return $resp.data.token
    } catch {
        Write-Host "[FATAL] Login failed: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

$token = Get-AuthToken
if (-not $token) { exit 1 }

$headers = @{Authorization = "Bearer $token"; "Content-Type" = "application/json;charset=UTF-8"}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Delta AI - Health Check Report" -ForegroundColor Cyan
Write-Host "  Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

foreach ($check in $checks) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $resp = Invoke-RestMethod -Uri "$BaseUrl$($check.Path)" -Method GET -Headers $headers -TimeoutSec $TimeoutSec
        $sw.Stop()
        $ms = $sw.ElapsedMilliseconds
        $passed = ($resp.code -eq $check.Expect)
        $status = if ($passed) { "OK" } else { "FAIL" }
        $color = if ($passed) { "Green" } else { "Red" }
        Write-Host "  [$status] $($check.Name) => $($ms)ms" -ForegroundColor $color
    } catch {
        $sw.Stop()
        Write-Host "  [ERROR] $($check.Name)" -ForegroundColor Red
        $allPassed = $false
    }
}

$healthText = if ($allPassed) { "HEALTHY" } else { "UNHEALTHY" }
Write-Host "  System Status: $healthText" -ForegroundColor $(if ($allPassed) { "Green" } else { "Red" })
exit $(if ($allPassed) { 0 } else { 1 })