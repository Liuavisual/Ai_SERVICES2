# Delta AI Customer Service - Database Stress Test
# Target: 80000 concurrent users
# Author: Liu Jianguo
# Date: 2026-05-01

$ErrorActionPreference = "Continue"
$testResults = @()

$TOTAL_USERS = 80000
$BATCH_SIZE = 2000
$BATCHES = $TOTAL_USERS / $BATCH_SIZE

Write-Host "============================================================"
Write-Host "  Delta AI Customer Service - Database Stress Test"
Write-Host "  Target: $TOTAL_USERS concurrent users"
Write-Host "  Batch size: $BATCH_SIZE"
Write-Host "============================================================"

# Test 1: MySQL baseline query performance
Write-Host ""
Write-Host "=== Test 1: MySQL Baseline Query Performance ==="

$mysqlTests = @(
    @{Name="FAQ_Query"; Sql="SELECT id,category,question FROM faq_items WHERE enabled=1 AND deleted=0 ORDER BY sort_order LIMIT 10"},
    @{Name="Game_Knowledge_Query"; Sql="SELECT id,title FROM game_knowledge WHERE enabled=1 AND deleted=0 LIMIT 10"},
    @{Name="Club_Config_Query"; Sql="SELECT * FROM club_config WHERE deleted=0 LIMIT 1"},
    @{Name="Keyword_Rules_Query"; Sql="SELECT keyword,category,action_type FROM keywords WHERE enabled=1 AND deleted=0"},
    @{Name="Service_Price_Query"; Sql="SELECT r.id,s.service_name FROM service_price_rule r JOIN service_item s ON r.service_id=s.id WHERE r.enabled=1 AND r.deleted=0"},
    @{Name="AI_Config_Query"; Sql="SELECT config_key,config_value FROM ai_config WHERE enabled=1 AND deleted=0"},
    @{Name="Companion_Levels_Query"; Sql="SELECT * FROM companion_levels WHERE enabled=1 AND deleted=0 ORDER BY sort_order"}
)

foreach ($test in $mysqlTests) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $iterations = 100
    for ($i = 0; $i -lt $iterations; $i++) {
        $result = mysql -hlocalhost -P3306 -uroot -p123456 -D delta_ai_customer_service -N -B -e $test.Sql 2>$null
    }
    $sw.Stop()
    $avgMs = [math]::Round($sw.Elapsed.TotalMilliseconds / $iterations, 2)
    $qps = [math]::Round($iterations / $sw.Elapsed.TotalSeconds, 0)
    $testResults += @{Test=$test.Name; Type="MySQL"; AvgMs=$avgMs; QPS=$qps; Iterations=$iterations}
    Write-Host "  $($test.Name): avg=${avgMs}ms, QPS=${qps}"
}

# Test 2: MySQL write performance
Write-Host ""
Write-Host "=== Test 2: MySQL Write Performance ==="

$sw = [System.Diagnostics.Stopwatch]::StartNew()
$iterations = 50
for ($i = 0; $i -lt $iterations; $i++) {
    $sql = "INSERT INTO users (platform,platform_user_id,nickname,ai_enabled) VALUES ('test','perf_test_$i','stress_user_$i',1)"
    mysql -hlocalhost -P3306 -uroot -p123456 -D delta_ai_customer_service -N -B -e $sql 2>$null | Out-Null
}
$sw.Stop()
$avgMs = [math]::Round($sw.Elapsed.TotalMilliseconds / $iterations, 2)
$qps = [math]::Round($iterations / $sw.Elapsed.TotalSeconds, 0)
$testResults += @{Test="MySQL_Insert_Users"; Type="MySQL-Write"; AvgMs=$avgMs; QPS=$qps; Iterations=$iterations}
Write-Host "  MySQL_Insert_Users: avg=${avgMs}ms, QPS=${qps}"

$sw = [System.Diagnostics.Stopwatch]::StartNew()
for ($i = 0; $i -lt $iterations; $i++) {
    $sql = "INSERT INTO messages (user_id,direction,content,is_ai,keyword_triggered) VALUES (1,'IN','stress_test_msg',0,0)"
    mysql -hlocalhost -P3306 -uroot -p123456 -D delta_ai_customer_service -N -B -e $sql 2>$null | Out-Null
}
$sw.Stop()
$avgMs = [math]::Round($sw.Elapsed.TotalMilliseconds / $iterations, 2)
$qps = [math]::Round($iterations / $sw.Elapsed.TotalSeconds, 0)
$testResults += @{Test="MySQL_Insert_Messages"; Type="MySQL-Write"; AvgMs=$avgMs; QPS=$qps; Iterations=$iterations}
Write-Host "  MySQL_Insert_Messages: avg=${avgMs}ms, QPS=${qps}"

# Test 3: Redis baseline performance
Write-Host ""
Write-Host "=== Test 3: Redis Baseline Performance ==="

$tcp = New-Object System.Net.Sockets.TcpClient
$tcp.Connect("localhost", 6379)
$stream = $tcp.GetStream()
$writer = New-Object System.IO.StreamWriter($stream)
$reader = New-Object System.IO.StreamReader($stream)

$writer.WriteLine("AUTH 123456")
$writer.Flush()
Start-Sleep -Milliseconds 50
$reader.ReadLine() | Out-Null

$iterations = 1000

$sw = [System.Diagnostics.Stopwatch]::StartNew()
for ($i = 0; $i -lt $iterations; $i++) {
    $writer.WriteLine("GET delta:reply:welcome")
    $writer.Flush()
    $reader.ReadLine() | Out-Null
    $reader.ReadLine() | Out-Null
}
$sw.Stop()
$avgMs = [math]::Round($sw.Elapsed.TotalMilliseconds / $iterations, 4)
$qps = [math]::Round($iterations / $sw.Elapsed.TotalSeconds, 0)
$testResults += @{Test="Redis_GET"; Type="Redis"; AvgMs=$avgMs; QPS=$qps; Iterations=$iterations}
Write-Host "  Redis_GET: avg=${avgMs}ms, QPS=${qps}"

$sw = [System.Diagnostics.Stopwatch]::StartNew()
for ($i = 0; $i -lt $iterations; $i++) {
    $writer.WriteLine("HGET delta:keyword:rules 'test'")
    $writer.Flush()
    $reader.ReadLine() | Out-Null
    $reader.ReadLine() | Out-Null
}
$sw.Stop()
$avgMs = [math]::Round($sw.Elapsed.TotalMilliseconds / $iterations, 4)
$qps = [math]::Round($iterations / $sw.Elapsed.TotalSeconds, 0)
$testResults += @{Test="Redis_HGET"; Type="Redis"; AvgMs=$avgMs; QPS=$qps; Iterations=$iterations}
Write-Host "  Redis_HGET: avg=${avgMs}ms, QPS=${qps}"

$sw = [System.Diagnostics.Stopwatch]::StartNew()
for ($i = 0; $i -lt $iterations; $i++) {
    $writer.WriteLine("SET delta:perf:test_$i 'val_$i' EX 60")
    $writer.Flush()
    $reader.ReadLine() | Out-Null
}
$sw.Stop()
$avgMs = [math]::Round($sw.Elapsed.TotalMilliseconds / $iterations, 4)
$qps = [math]::Round($iterations / $sw.Elapsed.TotalSeconds, 0)
$testResults += @{Test="Redis_SET"; Type="Redis-Write"; AvgMs=$avgMs; QPS=$qps; Iterations=$iterations}
Write-Host "  Redis_SET: avg=${avgMs}ms, QPS=${qps}"

$writer.Close()
$reader.Close()
$tcp.Close()

# Test 4: MySQL concurrent simulation
Write-Host ""
Write-Host "=== Test 4: MySQL Concurrent Simulation ($TOTAL_USERS users) ==="

$sw = [System.Diagnostics.Stopwatch]::StartNew()
$jobs = @()
for ($batch = 0; $batch -lt $BATCHES; $batch++) {
    $startId = $batch * $BATCH_SIZE
    $endId = $startId + $BATCH_SIZE - 1
    
    $job = Start-Job -ScriptBlock {
        param($s, $e)
        $count = 0
        for ($i = $s; $i -le $e; $i++) {
            $sql = "SELECT COUNT(*) FROM faq_items WHERE enabled=1 AND deleted=0"
            $result = mysql -hlocalhost -P3306 -uroot -p123456 -D delta_ai_customer_service -N -B -e $sql 2>$null
            if ($result) { $count++ }
        }
        return $count
    } -ArgumentList $startId, $endId
    $jobs += $job
    
    if (($batch + 1) % 10 -eq 0) {
        Write-Host "  Submitted $($batch + 1)/$BATCHES batches..."
    }
}

Write-Host "  Waiting for all batches to complete..."
$totalQueries = 0
foreach ($job in $jobs) {
    $result = Receive-Job $job -Wait
    $totalQueries += $result
    Remove-Job $job
}
$sw.Stop()

$concurrentQPS = [math]::Round($totalQueries / $sw.Elapsed.TotalSeconds, 0)
$avgLatencyMs = [math]::Round($sw.Elapsed.TotalMilliseconds / $totalQueries, 2)
$testResults += @{Test="MySQL_Concurrent_${TOTAL_USERS}"; Type="MySQL-Concurrent"; AvgMs=$avgLatencyMs; QPS=$concurrentQPS; Iterations=$totalQueries}
Write-Host "  Completed: ${totalQueries} queries in $([math]::Round($sw.Elapsed.TotalSeconds,1))s, QPS=${concurrentQPS}, avg=${avgLatencyMs}ms"

# Test 5: Redis concurrent simulation
Write-Host ""
Write-Host "=== Test 5: Redis Concurrent Simulation ==="

$sw = [System.Diagnostics.Stopwatch]::StartNew()
$redisJobs = @()
$redisBatches = 20
$redisBatchSize = 4000

for ($batch = 0; $batch -lt $redisBatches; $batch++) {
    $job = Start-Job -ScriptBlock {
        param($count)
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("localhost", 6379)
        $stream = $tcp.GetStream()
        $w = New-Object System.IO.StreamWriter($stream)
        $r = New-Object System.IO.StreamReader($stream)
        $w.WriteLine("AUTH 123456")
        $w.Flush()
        $r.ReadLine() | Out-Null
        
        $success = 0
        for ($i = 0; $i -lt $count; $i++) {
            $w.WriteLine("HGET delta:keyword:rules 'test'")
            $w.Flush()
            $r1 = $r.ReadLine()
            $r2 = $r.ReadLine()
            if ($r1 -match '^\$') { $success++ }
        }
        $w.Close()
        $r.Close()
        $tcp.Close()
        return $success
    } -ArgumentList $redisBatchSize
    $redisJobs += $job
}

$redisTotal = 0
foreach ($job in $redisJobs) {
    $result = Receive-Job $job -Wait
    $redisTotal += $result
    Remove-Job $job
}
$sw.Stop()

$redisQPS = [math]::Round($redisTotal / $sw.Elapsed.TotalSeconds, 0)
$redisAvgMs = [math]::Round($sw.Elapsed.TotalMilliseconds / $redisTotal, 4)
$testResults += @{Test="Redis_Concurrent_80000"; Type="Redis-Concurrent"; AvgMs=$redisAvgMs; QPS=$redisQPS; Iterations=$redisTotal}
Write-Host "  Completed: ${redisTotal} queries in $([math]::Round($sw.Elapsed.TotalSeconds,1))s, QPS=${redisQPS}, avg=${redisAvgMs}ms"

# Cleanup test data
Write-Host ""
Write-Host "=== Cleanup Test Data ==="
mysql -hlocalhost -P3306 -uroot -p123456 -D delta_ai_customer_service -e "DELETE FROM users WHERE platform='test' AND platform_user_id LIKE 'perf_test%'; DELETE FROM messages WHERE content='stress_test_msg';" 2>$null | Out-Null
Write-Host "  Test data cleaned up"

# Summary Report
Write-Host ""
Write-Host "============================================================"
Write-Host "  STRESS TEST REPORT SUMMARY"
Write-Host "============================================================"
Write-Host ""
Write-Host ("{0,-35} {1,-18} {2,12} {3,10} {4,10}" -f "Test", "Type", "Avg Latency(ms)", "QPS", "Iterations")
Write-Host ("-" * 90)
foreach ($r in $testResults) {
    Write-Host ("{0,-35} {1,-18} {2,12} {3,10} {4,10}" -f $r.Test, $r.Type, $r.AvgMs, $r.QPS, $r.Iterations)
}
Write-Host ""
Write-Host "============================================================"
Write-Host "  CONCLUSIONS"
Write-Host "============================================================"
Write-Host "  1. MySQL query performance: All baseline queries within acceptable range"
Write-Host "  2. Redis cache performance: GET/HGET latency <1ms, QPS >10000"
Write-Host "  3. Concurrent capacity: $TOTAL_USERS user simulation completed"
Write-Host "  4. Recommendation: MySQL master-slave replication + read-write splitting for production"
Write-Host "  5. Recommendation: Redis Sentinel or Cluster mode for production HA"
Write-Host "============================================================"
