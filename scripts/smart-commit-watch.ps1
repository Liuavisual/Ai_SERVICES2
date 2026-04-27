$ErrorActionPreference = "SilentlyContinue"

$ProjectRoot = "D:\Project\AI-SERVERS"
$LogFile = "$ProjectRoot\scripts\auto-commit.log"
$WatchInterval = 60
$SignificantExtensions = @("*.java", "*.xml", "*.yml", "*.yaml", "*.properties", "*.js", "*.vue", "*.css", "*.html", "*.json", "*.pom")

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $entry = "[$timestamp] $Message"
    Add-Content -Path $LogFile -Value $entry -Encoding UTF8
    Write-Host $entry
}

function Get-SignificantChanges {
    param([string]$Root)
    $changed = @()
    foreach ($ext in $SignificantExtensions) {
        $files = git -C $Root diff --name-only HEAD -- $ext 2>$null
        $changed += $files
    }
    $untracked = @()
    foreach ($ext in $SignificantExtensions) {
        $files = git -C $Root ls-files --others --exclude-standard -- $ext 2>$null
        $untracked += $files
    }
    return @($changed + $untracked) | Where-Object { $_ -ne "" } | Select-Object -Unique
}

function Invoke-SmartCommit {
    param([string]$Message)
    Push-Location $ProjectRoot
    try {
        $changes = Get-SignificantChanges $ProjectRoot
        if ($changes.Count -eq 0) {
            return $false
        }
        git add -A 2>$null
        $commitMsg = if ($Message) { $Message } else {
            $date = Get-Date -Format "yyyy-MM-dd"
            $count = $changes.Count
            "chore: auto-commit $date ($count files changed)"
        }
        git commit -m $commitMsg 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Log "Committed: $commitMsg"
            git push origin main 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Log "Pushed to remote successfully"
            } else {
                Write-Log "Push failed (will retry next cycle)"
            }
            return $true
        }
        return $false
    } finally {
        Pop-Location
    }
}

Write-Log "=== Auto-commit watcher started ==="
Write-Log "Project: $ProjectRoot"
Write-Log "Watch interval: ${WatchInterval}s"
Write-Log "Monitoring extensions: $($SignificantExtensions -join ', ')"

while ($true) {
    Start-Sleep -Seconds $WatchInterval
    $changes = Get-SignificantChanges $ProjectRoot
    if ($changes.Count -gt 0) {
        Write-Log "Detected $($changes.Count) changed files"
        Invoke-SmartCommit
    }
}
