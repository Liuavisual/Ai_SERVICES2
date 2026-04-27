$ProjectRoot = "D:\Project\AI-SERVERS"
$LogFile = "$ProjectRoot\scripts\auto-commit.log"
$SignificantExtensions = @(".java", ".xml", ".yml", ".yaml", ".properties", ".vue", ".js", ".css", ".html", ".json", ".dockerfile", ".gitignore")

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "$timestamp | $Message" | Out-File -Append -FilePath $LogFile -Encoding UTF8
}

Set-Location $ProjectRoot

$statusOutput = git status --porcelain 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Log "ERROR: git status failed"
    exit 1
}

$changes = $statusOutput | Where-Object { $_ -match "^\s*[MADRC]" }
if (-not $changes) {
    exit 0
}

$significantChanges = $changes | Where-Object {
    $file = if ($_ -match '\s+(.+)$') { $Matches[1] } else { "" }
    $ext = [System.IO.Path]::GetExtension($file)
    $ext -in $SignificantExtensions
}

if (-not $significantChanges) {
    Write-Log "SKIP: Only non-significant file changes detected"
    exit 0
}

$javaCount = ($significantChanges | Where-Object { $_ -match "\.java$" }).Count
$vueCount = ($significantChanges | Where-Object { $_ -match "\.vue$|\.js$|\.css$" }).Count
$configCount = ($significantChanges | Where-Object { $_ -match "\.(yml|yaml|xml|properties)$" }).Count
$otherCount = $significantChanges.Count - $javaCount - $vueCount - $configCount

$parts = @()
if ($javaCount -gt 0) { $parts += "${javaCount} Java" }
if ($vueCount -gt 0) { $parts += "${vueCount} Frontend" }
if ($configCount -gt 0) { $parts += "${configCount} Config" }
if ($otherCount -gt 0) { $parts += "${otherCount} Other" }
$changeSummary = $parts -join ", "

$modifiedFiles = $significantChanges | ForEach-Object {
    $file = if ($_ -match '\s+(.+)$') { $Matches[1] } else { "" }
    $fileName = [System.IO.Path]::GetFileName($file)
    $dirName = Split-Path (Split-Path $file -Parent) -Leaf
    "$dirName/$fileName"
} | Select-Object -First 5

$fileList = $modifiedFiles -join ", "
if ($significantChanges.Count -gt 5) { $fileList += " ..." }

$dateStr = Get-Date -Format "yyyy-MM-dd"
$timeStr = Get-Date -Format "HH:mm"
$commitMessage = "auto: ${changeSummary} changed [${dateStr} ${timeStr}] - ${fileList}"

git add -A 2>&1 | Out-Null
git commit -m $commitMessage 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Log "COMMIT: $commitMessage"
    git push origin main 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Log "PUSH: Success"
    } else {
        Write-Log "PUSH: Failed (will retry on next schedule)"
    }
} else {
    Write-Log "COMMIT: Nothing to commit"
}
