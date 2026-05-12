# 1. 登录获取 token
$loginBody = '{"username":"admin","password":"Admin@123456"}'
$loginResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $loginBody -ContentType "application/json; charset=utf-8"
$token = $loginResult.data.token
Write-Host "Login OK, got token"

# 2. 获取陪玩师列表
$headers = @{ "Authorization" = "Bearer $token" }
$companions = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/companions/all" -Method GET -Headers $headers
Write-Host "Companions: $($companions.data.Count) found"
$companionId = $companions.data[0].id
$companionName = $companions.data[0].realName
Write-Host "Selected companion: $companionName (id=$companionId)"

# 3. 获取客户列表
$customers = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/customer-profiles/page" -Method GET -Headers $headers
Write-Host "Customers: $($customers.data.total) found"
$userId = $customers.data.records[0].userId
Write-Host "Selected customer: userId=$userId"

# 4. 创建订单
$body = @{
    userId = $userId
    companionId = $companionId
    serviceType = "ACCOMPANY_PLAY"
    scheduledStart = "2026-05-14 20:00:00"
    scheduledEnd = "2026-05-14 22:00:00"
    remark = "API验证测试订单"
    timeSource = "SYSTEM"
} | ConvertTo-Json -Compress

Write-Host "`nCreating order with body: $body"
$url = "http://localhost:8080/api/v1/orders"
$req = [System.Net.WebRequest]::Create($url)
$req.Method = "POST"
$req.ContentType = "application/json; charset=utf-8"
$req.Headers.Add("Authorization", "Bearer $token")
$bytes = [System.Text.Encoding]::UTF8.GetBytes($body)
$req.ContentLength = $bytes.Length
$stream = $req.GetRequestStream()
$stream.Write($bytes, 0, $bytes.Length)
$stream.Close()

try {
    $resp = $req.GetResponse()
    $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
    $result = $reader.ReadToEnd()
    $reader.Close()
    Write-Host "`n=== ORDER CREATED SUCCESS ==="
    Write-Host $result
} catch [System.Net.WebException] {
    $errResp = $_.Exception.Response
    $reader = New-Object System.IO.StreamReader($errResp.GetResponseStream())
    $errBody = $reader.ReadToEnd()
    $reader.Close()
    Write-Host "`n=== ORDER FAILED ==="
    Write-Host $errBody
}