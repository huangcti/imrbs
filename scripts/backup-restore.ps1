# 會議室預約系統資料備份/還原腳本
# PowerShell Script

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("backup", "restore")]
    [string]$Action,
    
    [string]$BackupDir = ".\backup",
    [string]$DataDir = "$env:USERPROFILE\.imrbs\data"
)

function Backup-Data {
    Write-Host "開始備份資料..." -ForegroundColor Green
    
    if (-not (Test-Path $DataDir)) {
        Write-Host "錯誤: 資料目錄不存在: $DataDir" -ForegroundColor Red
        exit 1
    }

    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupPath = Join-Path $BackupDir $timestamp
    
    New-Item -ItemType Directory -Path $backupPath -Force | Out-Null
    
    Copy-Item -Path "$DataDir\*" -Destination $backupPath -Recurse -Force
    
    Write-Host "備份完成: $backupPath" -ForegroundColor Green
    Write-Host "備份檔案:" -ForegroundColor Cyan
    Get-ChildItem $backupPath | Format-Table Name, Length, LastWriteTime
}

function Restore-Data {
    Write-Host "開始還原資料..." -ForegroundColor Green
    
    if (-not (Test-Path $BackupDir)) {
        Write-Host "錯誤: 備份目錄不存在: $BackupDir" -ForegroundColor Red
        exit 1
    }

    $backups = Get-ChildItem $BackupDir -Directory | Sort-Object Name -Descending
    
    if ($backups.Count -eq 0) {
        Write-Host "錯誤: 沒有找到備份" -ForegroundColor Red
        exit 1
    }

    Write-Host "可用的備份:" -ForegroundColor Cyan
    for ($i = 0; $i -lt $backups.Count; $i++) {
        Write-Host "  [$i] $($backups[$i].Name)"
    }

    $selection = Read-Host "選擇要還原的備份編號 (0-$($backups.Count - 1))"
    
    if ($selection -notmatch '^\d+$' -or [int]$selection -ge $backups.Count) {
        Write-Host "錯誤: 無效的選擇" -ForegroundColor Red
        exit 1
    }

    $selectedBackup = $backups[[int]$selection]
    
    Write-Host "確認還原備份: $($selectedBackup.Name)? (Y/N)" -ForegroundColor Yellow
    $confirm = Read-Host
    
    if ($confirm -ne 'Y' -and $confirm -ne 'y') {
        Write-Host "取消還原" -ForegroundColor Yellow
        exit 0
    }

    New-Item -ItemType Directory -Path $DataDir -Force | Out-Null
    
    Copy-Item -Path "$($selectedBackup.FullName)\*" -Destination $DataDir -Recurse -Force
    
    Write-Host "還原完成" -ForegroundColor Green
}

# 主程式
switch ($Action) {
    "backup" { Backup-Data }
    "restore" { Restore-Data }
}
