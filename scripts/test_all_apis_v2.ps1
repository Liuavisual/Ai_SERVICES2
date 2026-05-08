@"
param(
    [string]$BaseUrl = "http://localhost:8080/api/v1",
    [string]$TokenFile = "$env:TEMP\admin_token.txt"
)

function Test-Api {
    param(
        [string]$Method = "GET",
        [string]$Url,
        [string]$Body = $null,
        [int]$ExpectedCode = 200,
        [string]$Description = ""
    )

    $token = (Get-Content $TokenFile -Raw -Encoding ascii).Trim()
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }

    try {
        if ($Body) {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -Headers $headers -Body $Body -UseBasicParsing -TimeoutSec 10
        } else {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -Headers $headers -UseBasicParsing -TimeoutSec 10
        }
        if ($response.StatusCode -eq $ExpectedCode) {
            Write-Host "  [PASS] $Method $Url" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  [FAIL] $Method $Url" -ForegroundColor Red
            Write-Host "         Expected: $ExpectedCode, Got: $($response.StatusCode)" -ForegroundColor Red
            return $false
        }
    }
    catch {
        $sc = if ($_.Exception.Response.StatusCode) { [int]$_.Exception.Response.StatusCode } else { "N/A" }
        if ($sc -eq $ExpectedCode) {
            Write-Host "  [PASS] $Method $Url" -ForegroundColor Green
            return $true
        }
        Write-Host "  [FAIL] $Method $Url (HTTP $sc)" -ForegroundColor Red
        Write-Host "         $_" -ForegroundColor Red
        return $false
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Delta AI - 全量API测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$total = 0
$passed = 0
$failed = 0

# Helper to track results
function Run-Test {
    param($Method, $Url, $Body, $ExpectedCode, $Desc)
    $global:total++
    $result = Test-Api -Method $Method -Url $Url -Body $Body -ExpectedCode $ExpectedCode -Description $Desc
    if ($result) { $global:passed++ } else { $global:failed++ }
}

# --- 认证模块 ---
Write-Host "`n[模块1] 认证模块" -ForegroundColor Cyan
Run-Test -Method POST -Url "$BaseUrl/auth/heartbeat"

# --- 系统用户管理 ---
Write-Host "`n[模块2] 系统用户管理" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/sys-users/page?page=1&size=5"
Run-Test -Method GET -Url "$BaseUrl/sys-users/export?role=SYS_ADMIN"

# --- 权限管理 ---
Write-Host "`n[模块3] 权限管理" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/permission/list"
Run-Test -Method GET -Url "$BaseUrl/permission/list-by-group?group=system"
Run-Test -Method GET -Url "$BaseUrl/permission/roles"
Run-Test -Method GET -Url "$BaseUrl/permission/roles/1/permissions"
Run-Test -Method GET -Url "$BaseUrl/permission/users/1/roles"

# --- 客户管理 ---
Write-Host "`n[模块4] 客户管理" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/customers/page?page=1&size=5"

# --- 客户生命周期 ---
Write-Host "`n[模块5] 客户生命周期" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/customer-lifecycle/churned"
Run-Test -Method GET -Url "$BaseUrl/customer-lifecycle/at-risk"

# --- 客户画像 ---
Write-Host "`n[模块6] 客户画像" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/customer-profiles/page?page=1&size=5"
Run-Test -Method GET -Url "$BaseUrl/customer-profiles/orders/page?page=1&size=5"

# --- 满意度评价 ---
Write-Host "`n[模块7] 满意度评价" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/satisfaction/page?page=1&size=5"

# --- 工单管理 ---
Write-Host "`n[模块8] 工单管理" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/work-orders/page?page=1&size=5"
Run-Test -Method GET -Url "$BaseUrl/work-orders/count"

# --- 消息管理 ---
Write-Host "`n[模块9] 消息管理" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/messages/page?page=1&size=5"

# --- 待处理消息 ---
Write-Host "`n[模块10] 待处理消息" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/pending-messages/page?page=1&size=5"
Run-Test -Method GET -Url "$BaseUrl/pending-messages/count"

# --- 陪玩师管理 ---
Write-Host "`n[模块11] 陪玩师管理" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/companions/page?page=1&size=5"
Run-Test -Method GET -Url "$BaseUrl/companions/all"
Run-Test -Method GET -Url "$BaseUrl/companions/export"

# --- 陪玩师等级 ---
Write-Host "`n[模块12] 陪玩师等级" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/companion-levels/page?page=1&size=5"
Run-Test -Method GET -Url "$BaseUrl/companion-levels/all"

# --- 陪玩师排程 ---
Write-Host "`n[模块13] 陪玩师排程" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/companion-schedules/page?page=1&size=5"
Run-Test -Method GET -Url "$BaseUrl/companion-schedules/export"

# --- 订单管理 ---
Write-Host "`n[模块14] 订单管理" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/orders/page?page=1&size=5"

# --- 服务项目 ---
Write-Host "`n[模块15] 服务项目" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/service-items/page?page=1&size=5"

# --- 服务追踪 ---
Write-Host "`n[模块16] 服务追踪" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/service-tracks/1"

# --- 客服-客户分配 ---
Write-Host "`n[模块17] 客服-客户分配" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/cs-user-customer/page?page=1&size=5"

# --- AI配置 ---
Write-Host "`n[模块18] AI配置" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/ai-config"

# --- 俱乐部配置 ---
Write-Host "`n[模块19] 俱乐部配置" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/club-config"

# --- 游戏配置 ---
Write-Host "`n[模块20] 游戏配置" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/game-configs/page?page=1&size=5"

# --- FAQ知识库 ---
Write-Host "`n[模块21] FAQ知识库" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/faq-items?page=1&size=5"

# --- 关键词 ---
Write-Host "`n[模块22] 关键词管理" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/keywords/page?page=1&size=5"

# --- 回复话术 ---
Write-Host "`n[模块23] 回复话术" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/replies/page?page=1&size=5"

# --- 平台配置 ---
Write-Host "`n[模块24] 平台配置" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/platform-configs"

# --- 人格配置 ---
Write-Host "`n[模块25] AI人格配置" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/personality/configs"

# --- 缓存统计 ---
Write-Host "`n[模块26] 缓存统计" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/cache-stats/all"

# --- 活动套餐 ---
Write-Host "`n[模块27] 活动套餐" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/activity-packages/1"

# --- 营销活动 ---
Write-Host "`n[模块28] 营销活动" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/campaigns/page?page=1&size=5"

# --- SaaS订阅 ---
Write-Host "`n[模块29] SaaS订阅" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/subscriptions/page?page=1&size=5"

# --- 定价方案 ---
Write-Host "`n[模块30] 定价方案" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/pricing-plans/page?page=1&size=5"

# --- 质检记录 ---
Write-Host "`n[模块31] 质检记录" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/quality-checks/page?page=1&size=5"

# --- 裂变推荐 ---
Write-Host "`n[模块32] 裂变推荐" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/referrals/page?page=1&size=5"

# --- 营收报表 ---
Write-Host "`n[模块33] 营收报表" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/reports/page?page=1&size=5"

# --- 陪玩师结算 ---
Write-Host "`n[模块34] 陪玩师结算" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/settlements/page?page=1&size=5"

# --- 陪玩师培训 ---
Write-Host "`n[模块35] 陪玩师培训" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/trainings/page?page=1&size=5"

# --- 知识库 ---
Write-Host "`n[模块36] 知识库" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/knowledge/search?query=test"
Run-Test -Method GET -Url "$BaseUrl/knowledge/categories"

# --- 统计 ---
Write-Host "`n[模块37] 统计" -ForegroundColor Cyan
Run-Test -Method GET -Url "$BaseUrl/stats/personal?period=DAILY"
Run-Test -Method GET -Url "$BaseUrl/stats/team?period=DAILY"
Run-Test -Method GET -Url "$BaseUrl/stats/global?period=DAILY"

# --- 对话测试 ---
Write-Host "`n[模块38] 对话测试" -ForegroundColor Cyan
Run-Test -Method POST -Url "$BaseUrl/chat-test/send" -Body '{"message":"你好","sessionId":"test-session-001"}'

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  测试完成! 总计: $total, 通过: $passed, 失败: $failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "========================================" -ForegroundColor Cyan
"@ | Out-File -FilePath "d:\Project\AI-SERVERS\scripts\test_all_apis_v2.ps1" -Encoding UTF8

Write-Host "Script written"