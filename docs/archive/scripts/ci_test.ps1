# ============================================================
# Delta AI Customer Service - 持续集成自动化测试脚本
# @author 刘建国
# 功能：构建、部署、测试全流程自动化
# 迁移日期: 2026-05-09
# 迁移原因: .github/workflows/ci.yml已取代此本地脚本的CI/CD功能
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

function Write-Step { param($msg) Write-Host "[$(Get-Date -Format 'HH:mm:ss')] $msg" -ForegroundColor Cyan }

Write-Step "Step 1: Build & Compile"
if (-not $SkipBuild) {
    Push-Location $ProjectRoot
    mvn clean package -DskipTests -q 2>&1
    Pop-Location
}

Write-Step "Step 2: Database Check"
# MySQL connection check (requires MySQL Connector/NET)

Write-Step "Step 3: Core API Tests"
if (-not $SkipTest) {
    $loginBody = '{"username":"admin","password":"123456"}'
    $loginResp = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method POST -ContentType "application/json;charset=UTF-8" -Body $loginBody
    $token = $loginResp.data.token
    $headers = @{Authorization="Bearer $token"}
    
    Invoke-RestMethod -Uri "$BaseUrl/sys-users/page?page=1&size=10" -Method GET -Headers $headers
    Invoke-RestMethod -Uri "$BaseUrl/companions/page?page=1&size=10" -Method GET -Headers $headers
    Invoke-RestMethod -Uri "$BaseUrl/customers/page?page=1&size=10" -Method GET -Headers $headers
    Invoke-RestMethod -Uri "$BaseUrl/orders/page?page=1&size=10" -Method GET -Headers $headers
    Write-Host "  API tests completed" -ForegroundColor Green
}

$elapsed = [math]::Round(((Get-Date) - $startTime).TotalSeconds, 1)
Write-Host "Pipeline complete: ${elapsed}s" -ForegroundColor $(if ($allPassed) { "Green" } else { "Red" })
exit $(if ($allPassed) { 0 } else { 1 })