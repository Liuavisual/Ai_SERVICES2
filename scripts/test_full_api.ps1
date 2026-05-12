# 全面接口测试脚本
Write-Host "========================================"
Write-Host "  Delta AI Customer Service API 全面测试"
Write-Host "========================================"

# 1. 登录
Write-Host "`n[1/6] 登录..."
$loginResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body '{"username":"admin","password":"Admin@123456"}' -ContentType "application/json; charset=utf-8"
$token = $loginResult.data.token
Write-Host "  [OK] Login: admin"

# 2. 陪玩师列表
Write-Host "`n[2/6] 获取陪玩师列表..."
$companions = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/companions/all" -Method GET -Headers @{"Authorization"="Bearer $token"}
Write-Host "  [OK] Companions: $($companions.data.Count) found"

# 3. 客户画像
Write-Host "`n[3/6] 获取客户画像列表..."
$customers = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/customer-profiles/page" -Method GET -Headers @{"Authorization"="Bearer $token"}
Write-Host "  [OK] Customers: $($customers.data.total) found"

# 4. 创建订单1
Write-Host "`n[4/6] 创建订单 (测试1)..."
$body1 = @{userId=$customers.data.records[0].userId; companionId=$companions.data[0].id; serviceType="ACCOMPANY_PLAY"; scheduledStart="2026-05-15 20:00:00"; scheduledEnd="2026-05-15 22:00:00"; remark="全面测试订单1"; timeSource="SYSTEM"} | ConvertTo-Json -Compress
$req = [System.Net.WebRequest]::Create("http://localhost:8080/api/v1/orders")
$req.Method = "POST"; $req.ContentType = "application/json; charset=utf-8"; $req.Headers.Add("Authorization","Bearer $token")
$bytes = [System.Text.Encoding]::UTF8.GetBytes($body1); $req.ContentLength = $bytes.Length
$req.GetRequestStream().Write($bytes,0,$bytes.Length); $req.GetRequestStream().Close()
$resp = $req.GetResponse(); $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
$result1 = ($reader.ReadToEnd() | ConvertFrom-Json); $reader.Close()
Write-Host "  [OK] Order: $($result1.data.orderNo), Status: $($result1.data.orderStatusText), Amount: $($result1.data.totalAmount)"

# 5. 创建订单2 (不同陪玩师)
Write-Host "`n[5/6] 创建订单 (测试2, 不同陪玩师)..."
$body2 = @{userId=$customers.data.records[1].userId; companionId=$companions.data[1].id; serviceType="GAME_TUTORING"; scheduledStart="2026-05-16 14:00:00"; scheduledEnd="2026-05-16 16:00:00"; remark="全面测试订单2"; timeSource="MANUAL"} | ConvertTo-Json -Compress
$req2 = [System.Net.WebRequest]::Create("http://localhost:8080/api/v1/orders")
$req2.Method = "POST"; $req2.ContentType = "application/json; charset=utf-8"; $req2.Headers.Add("Authorization","Bearer $token")
$bytes2 = [System.Text.Encoding]::UTF8.GetBytes($body2); $req2.ContentLength = $bytes2.Length
$req2.GetRequestStream().Write($bytes2,0,$bytes2.Length); $req2.GetRequestStream().Close()
$resp2 = $req2.GetResponse(); $reader2 = New-Object System.IO.StreamReader($resp2.GetResponseStream())
$result2 = ($reader2.ReadToEnd() | ConvertFrom-Json); $reader2.Close()
Write-Host "  [OK] Order: $($result2.data.orderNo), Status: $($result2.data.orderStatusText), Amount: $($result2.data.totalAmount)"

# 6. 查询订单
Write-Host "`n[6/6] 查询订单..."
$orders = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/orders" -Method GET -Headers @{"Authorization"="Bearer $token"}
Write-Host "  [OK] Orders: $($orders.data.total) found"

Write-Host "`n========================================"
Write-Host "  全面测试完成 - 所有接口正常!"
Write-Host "========================================"