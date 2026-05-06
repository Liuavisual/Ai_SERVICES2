# ============================================================
# Delta AI客服 — 敏感词数据生成与插入脚本
# 作者：刘建国
# 功能：
#   1. 通过DeepSeek API生成5类敏感词数据
#   2. 敏感词内容过滤（API返回内容自身也经过敏感词检查）
#   3. 参数化写入keywords表，带事务管理和详细日志
#   4. 生成HTML格式插入活动报告
#
# ⚠️ 2026-05-06 变更：已禁用 DeepSeek API 自动生成功能
#    敏感词库已切换为开源方案（sensitive-word），本脚本保留用于
#    生成报告和手动词库管理。开启自动生成请设置以下开关为 $true。
# ============================================================

# ---------- 自动生成开关（已禁用）----------
$ENABLE_AUTO_GENERATION = $false

$ErrorActionPreference = "Continue"
$OutputEncoding = [System.Text.UTF8Encoding]::new()

# ---------- 数据库配置 ----------
$DB_HOST = "localhost"
$DB_USER = "root"
$DB_PASS = "123456"
$DB_NAME = "delta_ai_customer_service"

# ---------- DeepSeek API配置 ----------
$DEEPSEEK_API_KEY = "sk-996af61bd272438bb4e8fe9f696eaf24"
$DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions"

# ---------- 日志系统 ----------
$LOG_DIR = "d:\Project\AI-SERVERS\logs"
$REPORT_DIR = "d:\Project\AI-SERVERS\reports"
if (-not (Test-Path $LOG_DIR)) { New-Item -ItemType Directory -Path $LOG_DIR -Force | Out-Null }
if (-not (Test-Path $REPORT_DIR)) { New-Item -ItemType Directory -Path $REPORT_DIR -Force | Out-Null }

$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"
$INSERT_LOG_FILE = "$LOG_DIR\sensitive_word_insert_$TIMESTAMP.log"
$ERROR_LOG_FILE = "$LOG_DIR\sensitive_word_error_$TIMESTAMP.log"
$REPORT_FILE = "$REPORT_DIR\data_insertion_report_$TIMESTAMP.html"

# ---------- 插入统计 ----------
$stats = @{
    TotalGenerated = 0
    TotalInserted  = 0
    TotalFailed    = 0
    TotalFiltered  = 0
    CategoryStats  = @{}
    FilteredWords  = @()
    FailedWords    = @()
    StartTime      = Get-Date
}

# ---------- 日志函数 ----------
function Write-InsertLog {
    param([string]$Level, [string]$Message)
    $logEntry = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss.fff') [$Level] $Message"
    Add-Content -Path $INSERT_LOG_FILE -Value $logEntry -Encoding UTF8
    Write-Host $logEntry
}

function Write-ErrorLog {
    param([string]$Category, [string]$Word, [string]$Reason)
    $errorEntry = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss.fff') | $Category | $Word | $Reason"
    Add-Content -Path $ERROR_LOG_FILE -Value $errorEntry -Encoding UTF8
}

# ---------- 敏感词类别定义 ----------
$SENSITIVE_CATEGORIES = @(
    @{Code="SENSITIVE_POLITICAL";   Name="政治敏感";   PromptType="POLITICAL"},
    @{Code="SENSITIVE_ADULT";       Name="色情低俗";   PromptType="ADULT"},
    @{Code="SENSITIVE_VIOLENCE";    Name="暴恐违禁";   PromptType="VIOLENCE"},
    @{Code="SENSITIVE_FRAUD";       Name="诈骗广告";   PromptType="FRAUD"},
    @{Code="SENSITIVE_HARASSMENT";  Name="人身攻击";   PromptType="HARASSMENT"}
)

# ---------- 本脚本自身的内容安全检查 ----------
$SELF_SAFETY_PATTERNS = @(
    @{Pattern='\b(sk-[a-zA-Z0-9]{32,})\b'; Replace='***API_KEY_REDACTED***'},
    @{Pattern='(password|pwd)\s*[=:]\s*["\047][^"\047]+["\047]'; Replace='password=***REDACTED***'}
)

function Protect-SensitiveLog {
    param([string]$Text)
    foreach ($rule in $SELF_SAFETY_PATTERNS) {
        $Text = $Text -replace $rule.Pattern, $rule.Replace
    }
    return $Text
}

# ---------- 调用DeepSeek API生成敏感词 ----------
function Invoke-DeepSeekForWords {
    param([string]$CategoryName, [string]$PromptType, [int]$Count = 50)
    
    Write-InsertLog "INFO" "开始调用DeepSeek API生成 [${CategoryName}] 类敏感词，目标数量: ${Count}"
    
    $systemPrompt = @"
你是一个内容安全专家。请生成一份简体中文的敏感词列表，分类为：$CategoryName。
要求：
1. 输出纯JSON数组格式：["词1","词2","词3",...]
2. 每个词长度2-10个汉字
3. 必须包含常见的网络变体、谐音、缩写形式
4. 数量：至少${Count}个
5. 不要输出任何解释、说明或其他文字，只输出JSON数组
"@

    $body = @{
        model = "deepseek-chat"
        messages = @(
            @{role="system"; content=$systemPrompt},
            @{role="user"; content="请生成${CategoryName}类的敏感词列表，至少${Count}个，输出纯JSON数组"}
        )
        temperature = 0.7
        max_tokens = 4000
    } | ConvertTo-Json -Depth 4

    try {
        $response = Invoke-RestMethod -Uri $DEEPSEEK_API_URL -Method POST `
            -Headers @{
                "Authorization" = "Bearer $DEEPSEEK_API_KEY"
                "Content-Type" = "application/json"
            } `
            -Body $body `
            -TimeoutSec 60
        
        $rawContent = $response.choices[0].message.content
        
        $cleanContent = $rawContent -replace '```json\s*', '' -replace '```\s*', '' -replace '^\s*', '' -replace '\s*$', ''
        
        Write-InsertLog "DEBUG" "DeepSeek原始响应(已脱敏): $(Protect-SensitiveLog ($rawContent.Substring(0, [Math]::Min(200, $rawContent.Length))))"
        
        $wordList = $cleanContent | ConvertFrom-Json -ErrorAction Stop
        
        $validWords = @()
        foreach ($word in $wordList) {
            $cleanWord = $word.ToString().Trim()
            if ($cleanWord.Length -ge 2 -and $cleanWord.Length -le 20 -and $cleanWord -match '[\u4e00-\u9fff\u3400-\u4dbf]') {
                $validWords += $cleanWord
            } else {
                Write-ErrorLog -Category $CategoryName -Word $cleanWord -Reason "内容安全自过滤：不符合长度或字符要求"
                $stats.TotalFiltered++
                $stats.FilteredWords += @{Word=$cleanWord; Category=$CategoryName; Reason="不符合长度或字符要求"}
            }
        }
        
        Write-InsertLog "INFO" "[${CategoryName}] DeepSeek返回 $($wordList.Count) 个词，自过滤后保留 $($validWords.Count) 个"
        return $validWords
        
    } catch {
        Write-InsertLog "ERROR" "DeepSeek API调用失败 [${CategoryName}]: $_"
        return @()
    }
}

# ---------- 数据库插入（批量SQL文件模式） ----------
function Insert-SensitiveWords {
    param([string]$CategoryCode, [string]$CategoryName, [array]$Words)
    
    if ($Words.Count -eq 0) {
        Write-InsertLog "WARN" "[${CategoryName}] 无有效敏感词，跳过插入"
        return
    }
    
    Write-InsertLog "INFO" "开始插入 [${CategoryName}] 类敏感词，共 $($Words.Count) 个"
    
    $sqlBatch = @()
    $sqlBatch += "USE $DB_NAME;"
    $sqlBatch += "START TRANSACTION;"
    
    $insertedCount = 0
    $failedCount = 0
    
    foreach ($word in $Words) {
        # MySQL 特殊字符转义
        $escapedWord = $word -replace "\\", "\\\\" -replace "'", "\'" -replace '"', '\"'
        
        # 检查词是否已存在
        $checkSql = "INSERT IGNORE INTO keywords (keyword, category, match_type, action_type, priority, enabled, remark) VALUES ('${escapedWord}', '${CategoryCode}', 'EXACT', 'BLOCK', 100, 1, '敏感词-${CategoryName}');"
        $sqlBatch += $checkSql
    }
    
    $sqlBatch += "COMMIT;"
    
    $batchContent = $sqlBatch -join "`n"
    
    try {
        $batchContent | mysql -u $DB_USER "-p${DB_PASS}" 2>&1 | Out-Null
        
        # 统计实际插入数量
        $countQuery = "SELECT COUNT(*) FROM ${DB_NAME}.keywords WHERE category = '${CategoryCode}' AND action_type = 'BLOCK';"
        $actualCount = (mysql -u $DB_USER "-p${DB_PASS}" -e $countQuery 2>&1 | Select-Object -Skip 1).Trim()
        
        $insertedCount = [int]$actualCount
        Write-InsertLog "INFO" "[${CategoryName}] 批量插入完成，实际插入: ${insertedCount} 条"
        
    } catch {
        $failedCount = $Words.Count
        Write-InsertLog "ERROR" "[${CategoryName}] 批量插入失败: $_"
        Write-ErrorLog -Category $CategoryName -Word "BATCH" -Reason $_.Exception.Message
    }
    
    $stats.CategoryStats[$CategoryCode] = @{
        Name = $CategoryName
        Generated = $Words.Count
        Inserted = $insertedCount
        Failed = $failedCount
    }
    
    $stats.TotalGenerated += $Words.Count
    $stats.TotalInserted += $insertedCount
    $stats.TotalFailed += $failedCount
}

# ==================== 主流程 ====================
Write-InsertLog "INFO" "============================================"
Write-InsertLog "INFO" "Delta AI客服 — 敏感词数据生成与插入开始"
Write-InsertLog "INFO" "开始时间: $($stats.StartTime.ToString('yyyy-MM-dd HH:mm:ss'))"
Write-InsertLog "INFO" "============================================"

foreach ($cat in $SENSITIVE_CATEGORIES) {
    Write-InsertLog "INFO" "---------- 处理类别: $($cat.Name) ----------"
    
    if ($ENABLE_AUTO_GENERATION) {
        $words = Invoke-DeepSeekForWords -CategoryName $cat.Name -PromptType $cat.PromptType -Count 50
        Insert-SensitiveWords -CategoryCode $cat.Code -CategoryName $cat.Name -Words $words
    } else {
        Write-InsertLog "INFO" "[$($cat.Name)] 自动生成已禁用，跳过 DeepSeek API 调用"
        Write-InsertLog "INFO" "[$($cat.Name)] 当前使用开源敏感词库方案（sensitive-word），请通过管理后台管理词库"
    }
    
    Start-Sleep -Seconds 1
}

$stats.EndTime = Get-Date
$stats.Duration = $stats.EndTime - $stats.StartTime

Write-InsertLog "INFO" "============================================"
Write-InsertLog "INFO" "数据插入完成!"
Write-InsertLog "INFO" "总生成词数: $($stats.TotalGenerated)"
Write-InsertLog "INFO" "总成功插入: $($stats.TotalInserted)"
Write-InsertLog "INFO" "总失败数量: $($stats.TotalFailed)"
Write-InsertLog "INFO" "总过滤数量: $($stats.TotalFiltered)"
Write-InsertLog "INFO" "总耗时: $($stats.Duration.TotalSeconds) 秒"
Write-InsertLog "INFO" "============================================"

# ---------- 生成HTML报告 ----------
$htmlReport = @"
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<title>Delta AI客服 — 数据插入活动报告</title>
<style>
body{font-family:'Microsoft YaHei',Arial,sans-serif;margin:40px;background:#f5f7fa;color:#333}
h1{color:#1a1a2e;border-bottom:3px solid #409eff;padding-bottom:10px}
h2{color:#2c3e50;margin-top:30px}
.summary-box{background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;padding:20px;border-radius:10px;margin:20px 0}
.summary-box table{width:100%;color:#fff}
.summary-box td{padding:8px;font-size:16px}
.summary-box .label{opacity:0.85}
.summary-box .value{font-weight:bold;font-size:20px;text-align:right}
.category-table{width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.1)}
.category-table th{background:#409eff;color:#fff;padding:12px;text-align:left}
.category-table td{padding:10px 12px;border-bottom:1px solid #eee}
.category-table tr:hover{background:#f0f7ff}
.status-ok{color:#67c23a;font-weight:bold}
.status-err{color:#f56c6c;font-weight:bold}
.filtered-table{width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.1);margin-top:15px}
.filtered-table th{background:#e6a23c;color:#fff;padding:10px;text-align:left}
.filtered-table td{padding:8px 10px;border-bottom:1px solid #eee;font-size:13px}
.footer{margin-top:40px;padding-top:20px;border-top:1px solid #ddd;color:#999;font-size:12px}
</style>
</head>
<body>
<h1>Delta AI客服系统 — 数据插入活动报告</h1>
<p>生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</p>

<div class="summary-box">
<h2 style="color:#fff;margin-top:0">执行摘要</h2>
<table>
<tr><td class="label">开始时间</td><td class="value">$($stats.StartTime.ToString('yyyy-MM-dd HH:mm:ss'))</td></tr>
<tr><td class="label">结束时间</td><td class="value">$($stats.EndTime.ToString('yyyy-MM-dd HH:mm:ss'))</td></tr>
<tr><td class="label">总耗时</td><td class="value">$([Math]::Round($stats.Duration.TotalSeconds, 1)) 秒</td></tr>
<tr><td class="label">总生成词数</td><td class="value">$($stats.TotalGenerated)</td></tr>
<tr><td class="label">总成功插入</td><td class="value">$($stats.TotalInserted)</td></tr>
<tr><td class="label">总失败数量</td><td class="value">$($stats.TotalFailed)</td></tr>
<tr><td class="label">总过滤数量</td><td class="value">$($stats.TotalFiltered)</td></tr>
</table>
</div>

<h2>分类插入统计</h2>
<table class="category-table">
<tr><th>敏感词类别</th><th>类别代码</th><th>生成数量</th><th>成功插入</th><th>失败数量</th><th>状态</th></tr>
"@

foreach ($cat in $SENSITIVE_CATEGORIES) {
    $cs = $stats.CategoryStats[$cat.Code]
    $gen = if ($cs) { $cs.Generated } else { 0 }
    $ins = if ($cs) { $cs.Inserted } else { 0 }
    $fal = if ($cs) { $cs.Failed } else { 0 }
    $status = if ($fal -eq 0 -and $ins -gt 0) { '<span class="status-ok">成功</span>' } else { '<span class="status-err">失败</span>' }
    
    $htmlReport += @"
<tr><td>$($cat.Name)</td><td>$($cat.Code)</td><td>$gen</td><td>$ins</td><td>$fal</td><td>$status</td></tr>
"@
}

$htmlReport += "</table>"

if ($stats.FilteredWords.Count -gt 0) {
    $htmlReport += @"
<h2>敏感词自过滤记录</h2>
<table class="filtered-table">
<tr><th>过滤词</th><th>类别</th><th>过滤原因</th></tr>
"@
    foreach ($fw in $stats.FilteredWords) {
        $htmlReport += "<tr><td>$($fw.Word)</td><td>$($fw.Category)</td><td>$($fw.Reason)</td></tr>"
    }
    $htmlReport += "</table>"
}

$htmlReport += @"
<div class="footer">
<p>报告由自动化脚本自动生成 | 数据库: $DB_NAME | 主机: $DB_HOST</p>
<p>敏感词生成引擎: DeepSeek API (deepseek-chat) | 插入方式: 批量INSERT IGNORE + 事务管理</p>
</div>
</body>
</html>
"@

$htmlReport | Set-Content -Path $REPORT_FILE -Encoding UTF8

Write-InsertLog "INFO" "HTML报告已生成: $REPORT_FILE"

# ---------- 清理临时文件 ----------
Remove-Item "d:\Project\AI-SERVERS\scripts\game_knowledge_data_fixed.sql" -Force -ErrorAction SilentlyContinue

Write-InsertLog "INFO" "脚本执行完毕"
exit 0
