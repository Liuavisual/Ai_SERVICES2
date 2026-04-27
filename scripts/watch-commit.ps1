$ProjectRoot = "D:\Project\AI-SERVERS"
$LogFile = "$ProjectRoot\scripts\auto-commit.log"
$SignificantExtensions = @(".java", ".xml", ".yml", ".yaml", ".properties", ".vue", ".js", ".css", ".html", ".json")
$DebounceSeconds = 30
$script:lastTrigger = [datetime]::MinValue

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "$timestamp | $Message" | Out-File -Append -FilePath $LogFile -Encoding UTF8
}

$watcher = New-Object System.IO.FileSystemWatcher
$watcher.Path = $ProjectRoot
$watcher.Filter = "*.*"
$watcher.IncludeSubdirectories = $true
$watcher.NotifyFilter = [System.IO.NotifyFilters]::FileName -bor [System.IO.NotifyFilters]::LastWrite

$action = {
    $filePath = $Event.SourceEventArgs.FullPath
    $ext = [System.IO.Path]::GetExtension($filePath)

    if ($ext -notin $SignificantExtensions) { return }
    if ($filePath -match "\\target\\" -or $filePath -match "\\.idea\\" -or $filePath -match "\\node_modules\\") { return }

    $now = Get-Date
    if (($now - $script:lastTrigger).TotalSeconds -lt $DebounceSeconds) { return }
    $script:lastTrigger = $now

    Write-Log "WATCH: Significant change detected - $filePath"
    Start-Sleep -Seconds 5

    & "$ProjectRoot\scripts\auto-commit.ps1"
}

Register-ObjectEvent $watcher "Changed" -Action $action | Out-Null
Register-ObjectEvent $watcher "Created" -Action $action | Out-Null
Register-ObjectEvent $watcher "Deleted" -Action $action | Out-Null
Register-ObjectEvent $watcher "Renamed" -Action $action | Out-Null

$watcher.EnableRaisingEvents = $true

Write-Log "WATCH: File watcher started, monitoring $ProjectRoot"
Write-Host "File watcher running. Press Ctrl+C to stop."

try {
    while ($true) { Start-Sleep -Seconds 60 }
} finally {
    $watcher.EnableRaisingEvents = $false
    $watcher.Dispose()
    Write-Log "WATCH: File watcher stopped"
}
