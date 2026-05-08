param(
    [string]$BaseUrl = "http://localhost:8080/api/v1",
    [string]$TokenFile = "$env:TEMP\admin_token.txt"
)

# ANSI颜色
$GREEN = "Green"
$RED = "Red"
$YELLOW = "Yellow"
$CYAN = "Cyan"

function Write-Status {
    param($Message, $Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Test-Api {
    param(
        [string]$Method = "GET",
        [string]$Url,
        [string]$Body = $null,
        [string]$ExpectedCode = "200",
        [string]$Description = ""
    )

    $token = Get-Content $TokenFile -Raw -Encoding ascii
    $token = $token.Trim()

    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }

    $params = @{
        Uri = $Url
        Method = $Method
        Headers = $headers
        UseBasicParsing = $true
    }

    if ($Body) {
        $params.Body = $Body
    }

    try {
        $response = Invoke-WebRequest @params -TimeoutSec 10
        $httpCode = $response.StatusCode
        $content = $response.Content

        $result = "pass"
        $statusColor = $GREEN
        if ($httpCode -ne [int]$ExpectedCode) {
            $result = "FAIL"
            $statusColor = $RED
        }

        Write-Host "  [$result] $Method $Url" -ForegroundColor $statusColor
        if ($result -eq "FAIL") {
            Write-Host "         Expected: $ExpectedCode, Got: $httpCode" -ForegroundColor $RED
            Write-Host "         Body: $content" -ForegroundColor $RED
        }
        return @{Code = $httpCode; Body = $content; Status = $result}
    }
    catch {
        $statusCode = if ($_.Exception.Response.StatusCode) { [int]$_.Exception.Response.StatusCode } else { "N/A" }
        Write-Host "  [ERROR] $Method $Url" -ForegroundColor $RED
        Write-Host "         Exception: $_" -ForegroundColor $RED
        return @{Code = $statusCode; Body = $_.Exception.Message; Status = "ERROR"}
    }
}

Write-Status "========================================" $CYAN
Write-Status "  Delta AI Customer Service - 全量API测试" $CYAN
Write-Status "========================================" $CYAN
Write-Status ""

# ============================================
# 1. 认证模块
# ============================================
Write-Status "[模块1] 认证模块 (Auth)" $CYAN
Write-Status "----------------------------------------" $CYAN

# 1.1 POST /logout - 登出（先测试再重新登录）
Write-Status "  [SKIP] POST /auth/logout (跳过，保持会话)" $YELLOW

# 1.2 POST /heartbeat - 心跳
Test-Api -Method POST -Url "$BaseUrl/auth/heartbeat" -ExpectedCode 200 -Description "心跳检测"

# 1.3 POST /refresh - 刷新Token（需要refreshToken，跳过）
Write-Status "  [SKIP] POST /auth/refresh (需要refreshToken)" $YELLOW

# 1.4 GET /v1/auth/register - 注册（跳过，防止创建垃圾用户）
Write-Status "  [SKIP] POST /auth/register (跳过注册测试)" $YELLOW

Write-Status ""

# ============================================
# 2. 系统用户管理
# ============================================
Write-Status "[模块2] 系统用户管理 (SysUser)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/sys-users/page?page=1&size=5" -Description "分页查询用户"
Test-Api -Method GET -Url "$BaseUrl/sys-users/1" -Description "获取用户详情(ID=1)"
Test-Api -Method GET -Url "$BaseUrl/sys-users/export?role=SYS_ADMIN" -Description "导出用户Excel"

Write-Status ""

# ============================================
# 3. 权限管理
# ============================================
Write-Status "[模块3] 权限管理 (Permission)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/permission/list" -Description "获取所有权限列表"
Test-Api -Method GET -Url "$BaseUrl/permission/list-by-group?group=system" -Description "按分组获取权限"
Test-Api -Method GET -Url "$BaseUrl/permission/roles" -Description "获取所有角色"
Test-Api -Method GET -Url "$BaseUrl/permission/roles/1/permissions" -Description "获取角色权限"
Test-Api -Method GET -Url "$BaseUrl/permission/users/1/roles" -Description "获取用户角色"

Write-Status ""

# ============================================
# 4. 客户管理
# ============================================
Write-Status "[模块4] 客户管理 (Customer)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/customers/page?page=1&size=5" -Description "分页查询客户"
Test-Api -Method GET -Url "$BaseUrl/customers/1" -Description "获取客户详情"

Write-Status ""

# ============================================
# 5. 客户生命周期
# ============================================
Write-Status "[模块5] 客户生命周期 (Lifecycle)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/customer-lifecycle/churned" -Description "获取已流失客户"
Test-Api -Method GET -Url "$BaseUrl/customer-lifecycle/at-risk" -Description "获取濒临流失客户"

Write-Status ""

# ============================================
# 6. 客户画像
# ============================================
Write-Status "[模块6] 客户画像 (CustomerProfile)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/customer-profiles/page?page=1&size=5" -Description "分页查询客户画像"
Test-Api -Method GET -Url "$BaseUrl/customer-profiles/orders/page?page=1&size=5" -Description "分页查询消费记录"

Write-Status ""

# ============================================
# 7. 满意度评价
# ============================================
Write-Status "[模块7] 满意度评价 (Satisfaction)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/satisfaction/page?page=1&size=5" -Description "分页查询满意度评价"

Write-Status ""

# ============================================
# 8. 工单管理
# ============================================
Write-Status "[模块8] 工单管理 (WorkOrder)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/work-orders/page?page=1&size=5" -Description "分页查询工单"
Test-Api -Method GET -Url "$BaseUrl/work-orders/count" -Description "获取待处理工单数"

Write-Status ""

# ============================================
# 9. 消息管理
# ============================================
Write-Status "[模块9] 消息管理 (Message)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/messages/page?page=1&size=5" -Description "分页查询消息"

Write-Status ""

# ============================================
# 10. 待处理消息
# ============================================
Write-Status "[模块10] 待处理消息 (PendingMessage)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/pending-messages/page?page=1&size=5" -Description "分页查询待处理消息"
Test-Api -Method GET -Url "$BaseUrl/pending-messages/count" -Description "获取待处理消息数量"

Write-Status ""

# ============================================
# 11. 陪玩师管理
# ============================================
Write-Status "[模块11] 陪玩师管理 (Companion)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/companions/page?page=1&size=5" -Description "分页查询陪玩师"
Test-Api -Method GET -Url "$BaseUrl/companions/all" -Description "获取所有启用陪玩师"
Test-Api -Method GET -Url "$BaseUrl/companions/1" -Description "获取陪玩师详情"
Test-Api -Method GET -Url "$BaseUrl/companions/export" -Description "导出陪玩师Excel"

Write-Status ""

# ============================================
# 12. 陪玩师等级
# ============================================
Write-Status "[模块12] 陪玩师等级 (CompanionLevel)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/companion-levels/page?page=1&size=5" -Description "分页查询等级"
Test-Api -Method GET -Url "$BaseUrl/companion-levels/all" -Description "获取所有等级"
Test-Api -Method GET -Url "$BaseUrl/companion-levels/1" -Description "获取等级详情"

Write-Status ""

# ============================================
# 13. 陪玩师排程
# ============================================
Write-Status "[模块13] 陪玩师排程 (CompanionSchedule)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/companion-schedules/page?page=1&size=5" -Description "分页查询排程"
Test-Api -Method GET -Url "$BaseUrl/companion-schedules/1" -Description "获取排程详情"
Test-Api -Method GET -Url "$BaseUrl/companion-schedules/export" -Description "导出排程Excel"

Write-Status ""

# ============================================
# 14. 订单管理
# ============================================
Write-Status "[模块14] 订单管理 (Order)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/orders/page?page=1&size=5" -Description "分页查询订单"
Test-Api -Method GET -Url "$BaseUrl/orders/1" -Description "获取订单详情"

Write-Status ""

# ============================================
# 15. 服务项目
# ============================================
Write-Status "[模块15] 服务项目管理 (ServiceItem)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/service-items/page?page=1&size=5" -Description "分页查询服务项目"
Test-Api -Method GET -Url "$BaseUrl/service-items/1" -Description "获取服务项目详情"

Write-Status ""

# ============================================
# 16. 服务追踪
# ============================================
Write-Status "[模块16] 服务追踪 (ServiceTrack)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/service-tracks/1" -Description "获取服务追踪详情"
Test-Api -Method GET -Url "$BaseUrl/service-tracks/user/1" -Description "获取用户服务追踪列表"

Write-Status ""

# ============================================
# 17. 客服-客户分配
# ============================================
Write-Status "[模块17] 客服-客户分配 (CsUserCustomer)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/cs-user-customer/page?page=1&size=5" -Description "分页查询分配关系"

Write-Status ""

# ============================================
# 18. AI配置
# ============================================
Write-Status "[模块18] AI配置 (AiConfig)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/ai-config" -Description "获取所有AI配置"

Write-Status ""

# ============================================
# 19. 俱乐部配置
# ============================================
Write-Status "[模块19] 俱乐部配置 (ClubConfig)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/club-config" -Description "获取俱乐部配置"

Write-Status ""

# ============================================
# 20. 游戏配置
# ============================================
Write-Status "[模块20] 游戏配置 (GameConfig)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/game-configs/page?page=1&size=5" -Description "分页查询游戏配置"
Test-Api -Method GET -Url "$BaseUrl/game-configs/1" -Description "获取游戏配置详情"

Write-Status ""

# ============================================
# 21. FAQ知识库
# ============================================
Write-Status "[模块21] FAQ知识库 (FaqItem)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/faq-items?page=1&size=5" -Description "分页查询FAQ"

Write-Status ""

# ============================================
# 22. 关键词管理
# ============================================
Write-Status "[模块22] 关键词管理 (Keyword)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/keywords/page?page=1&size=5" -Description "分页查询关键词"
Test-Api -Method GET -Url "$BaseUrl/keywords/1" -Description "获取关键词详情"

Write-Status ""

# ============================================
# 23. 回复话术管理
# ============================================
Write-Status "[模块23] 回复话术 (Reply)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/replies/page?page=1&size=5" -Description "分页查询回复话术"
Test-Api -Method GET -Url "$BaseUrl/replies/1" -Description "获取回复话术详情"

Write-Status ""

# ============================================
# 24. 平台配置
# ============================================
Write-Status "[模块24] 平台配置 (PlatformConfig)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/platform-configs" -Description "获取所有平台配置"
Test-Api -Method GET -Url "$BaseUrl/platform-configs/WECHAT" -Description "获取指定平台配置"

Write-Status ""

# ============================================
# 25. 人格配置
# ============================================
Write-Status "[模块25] AI人格配置 (Personality)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/personality/configs" -Description "查询人格配置列表"
Test-Api -Method GET -Url "$BaseUrl/personality/configs/1" -Description "查询人格配置详情"

Write-Status ""

# ============================================
# 26. 缓存统计
# ============================================
Write-Status "[模块26] 缓存统计 (CacheStats)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/cache-stats/all" -Description "获取所有缓存统计"

Write-Status ""

# ============================================
# 27. 活动套餐
# ============================================
Write-Status "[模块27] 活动套餐 (ActivityPackage)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/activity-packages/1" -Description "获取活动套餐详情"

Write-Status ""

# ============================================
# 28. 营销活动
# ============================================
Write-Status "[模块28] 营销活动 (Campaign)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/campaigns/page?page=1&size=5" -Description "分页查询营销活动"
Test-Api -Method GET -Url "$BaseUrl/campaigns/1" -Description "获取营销活动详情"

Write-Status ""

# ============================================
# 29. SaaS订阅
# ============================================
Write-Status "[模块29] SaaS订阅 (Subscription)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/subscriptions/page?page=1&size=5" -Description "分页查询订阅记录"
Test-Api -Method GET -Url "$BaseUrl/subscriptions/1" -Description "获取订阅详情"

Write-Status ""

# ============================================
# 30. 定价方案
# ============================================
Write-Status "[模块30] 定价方案 (PricingPlan)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/pricing-plans/page?page=1&size=5" -Description "分页查询定价方案"
Test-Api -Method GET -Url "$BaseUrl/pricing-plans/1" -Description "获取定价方案详情"

Write-Status ""

# ============================================
# 31. 质检记录
# ============================================
Write-Status "[模块31] 质检记录 (QualityCheck)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/quality-checks/page?page=1&size=5" -Description "分页查询质检记录"
Test-Api -Method GET -Url "$BaseUrl/quality-checks/1" -Description "获取质检记录详情"

Write-Status ""

# ============================================
# 32. 裂变推荐
# ============================================
Write-Status "[模块32] 裂变推荐 (Referral)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/referrals/page?page=1&size=5" -Description "分页查询推荐记录"
Test-Api -Method GET -Url "$BaseUrl/referrals/1" -Description "获取推荐记录详情"

Write-Status ""

# ============================================
# 33. 营收报表
# ============================================
Write-Status "[模块33] 营收报表 (RevenueReport)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/reports/page?page=1&size=5" -Description "分页查询营收日报"
Test-Api -Method GET -Url "$BaseUrl/reports/1" -Description "获取营收日报详情"

Write-Status ""

# ============================================
# 34. 陪玩师结算
# ============================================
Write-Status "[模块34] 陪玩师结算 (Settlement)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/settlements/page?page=1&size=5" -Description "分页查询结算记录"
Test-Api -Method GET -Url "$BaseUrl/settlements/1" -Description "获取结算记录详情"

Write-Status ""

# ============================================
# 35. 陪玩师培训
# ============================================
Write-Status "[模块35] 陪玩师培训 (Training)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/trainings/page?page=1&size=5" -Description "分页查询培训记录"
Test-Api -Method GET -Url "$BaseUrl/trainings/1" -Description "获取培训记录详情"

Write-Status ""

# ============================================
# 36. 聊天测试 & 知识库
# ============================================
Write-Status "[模块36] 聊天测试 & 知识库 (ChatTest)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/knowledge/search?query=test" -Description "知识库搜索"
Test-Api -Method GET -Url "$BaseUrl/knowledge/categories" -Description "知识库分类"

Write-Status ""

# ============================================
# 37. 统计
# ============================================
Write-Status "[模块37] 统计 (Stats)" $CYAN
Write-Status "----------------------------------------" $CYAN

Test-Api -Method GET -Url "$BaseUrl/stats/personal?period=DAILY" -Description "个人统计"
Test-Api -Method GET -Url "$BaseUrl/stats/team?period=DAILY" -Description "团队统计"
Test-Api -Method GET -Url "$BaseUrl/stats/global?period=DAILY" -Description "全局统计"

Write-Status ""

# ============================================
# 38. 对话测试
# ============================================
Write-Status "[模块38] 对话测试 (ChatTest-Send)" $CYAN
Write-Status "----------------------------------------" $CYAN

$chatBody = '{"message":"你好","sessionId":"test-session-001"}'
Test-Api -Method POST -Url "$BaseUrl/chat-test/send" -Body $chatBody -Description "发送测试消息"

Write-Status ""

# ============================================
Write-Status "========================================" $CYAN
Write-Status "  API测试完成" $CYAN
Write-Status "========================================" $CYAN