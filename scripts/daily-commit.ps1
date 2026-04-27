$ProjectRoot = "D:\Project\AI-SERVERS"
$LogFile = "$ProjectRoot\scripts\auto-commit.log"

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "$timestamp | $Message" | Out-File -Append -FilePath $LogFile -Encoding UTF8
}

Set-Location $ProjectRoot

$statusOutput = git status --porcelain 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Log "DAILY ERROR: git status failed"
    exit 1
}

$changes = $statusOutput | Where-Object { $_ -match "^\s*[MADRC]" }
if (-not $changes) {
    Write-Log "DAILY SKIP: No changes to commit"
    exit 0
}

$dateStr = Get-Date -Format "yyyy-MM-dd"
$changeCount = ($changes | Measure-Object).Count
$commitMessage = "daily: ${changeCount} files changed [${dateStr}] daily checkpoint"

git add -A 2>&1 | Out-Null
git commit -m $commitMessage 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Log "DAILY COMMIT: $commitMessage"
    git push origin main 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Log "DAILY PUSH: Success"
    } else {
        Write-Log "DAILY PUSH: Failed"
    }
} else {
    Write-Log "DAILY COMMIT: Nothing to commit"
}
