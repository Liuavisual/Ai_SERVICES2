$ErrorActionPreference = "SilentlyContinue"

$ProjectRoot = "D:\Project\AI-SERVERS"
$LogFile = "$ProjectRoot\scripts\auto-commit.log"

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $entry = "[$timestamp] $Message"
    Add-Content -Path $LogFile -Value $entry -Encoding UTF8
    Write-Host $entry
}

Push-Location $ProjectRoot
try {
    $date = Get-Date -Format "yyyy-MM-dd"
    git add -A 2>$null
    
    $status = git status --porcelain 2>$null
    if ($status.Count -eq 0) {
        Write-Log "Daily commit: No changes to commit"
        exit 0
    }
    
    $commitMsg = "chore: daily auto-commit $date"
    git commit -m $commitMsg 2>$null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "Daily commit: $commitMsg"
        git push origin main 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Log "Daily push: success"
        } else {
            Write-Log "Daily push: failed"
        }
    } else {
        Write-Log "Daily commit: nothing to commit"
    }
} finally {
    Pop-Location
}
