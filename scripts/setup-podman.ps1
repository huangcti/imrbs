#!/usr/bin/env pwsh
# =============================================================================
# IMRBS - Podman 環境檢查與設置腳本
# =============================================================================

$ErrorActionPreference = "Stop"

function Write-Info($message) {
    Write-Host "[INFO] $message" -ForegroundColor Cyan
}

function Write-Success($message) {
    Write-Host "[SUCCESS] $message" -ForegroundColor Green
}

function Write-Error($message) {
    Write-Host "[ERROR] $message" -ForegroundColor Red
}

function Write-Warning($message) {
    Write-Host "[WARNING] $message" -ForegroundColor Yellow
}

Write-Host @"
╔══════════════════════════════════════════════════════════════╗
║        IMRBS - Podman 環境檢查與設置                         ║
╚══════════════════════════════════════════════════════════════╝
"@ -ForegroundColor Cyan

# 檢查 Podman
Write-Info "`n檢查 Podman 安裝狀態..."

$podmanPaths = @(
    "C:\Program Files\RedHat\Podman\podman.exe",
    "$env:ProgramFiles\RedHat\Podman\podman.exe",
    "$env:LOCALAPPDATA\Microsoft\WinGet\Packages\RedHat.Podman*\podman.exe"
)

$podmanFound = $false
$podmanPath = $null

# 先檢查 PATH 中是否有 podman
try {
    $podmanCmd = Get-Command podman -ErrorAction SilentlyContinue
    if ($podmanCmd) {
        $podmanFound = $true
        $podmanPath = $podmanCmd.Source
        $version = & podman --version
        Write-Success "✓ Podman 已安裝且在 PATH 中"
        Write-Host "  路徑: $podmanPath" -ForegroundColor Gray
        Write-Host "  版本: $version" -ForegroundColor Gray
    }
} catch {
    # Podman not in PATH
}

# 如果不在 PATH 中,檢查常見安裝位置
if (-not $podmanFound) {
    foreach ($path in $podmanPaths) {
        if (Test-Path $path) {
            $podmanFound = $true
            $podmanPath = $path
            $version = & $path --version
            Write-Warning "⚠ Podman 已安裝但不在 PATH 中"
            Write-Host "  路徑: $podmanPath" -ForegroundColor Gray
            Write-Host "  版本: $version" -ForegroundColor Gray
            break
        }
    }
}

if (-not $podmanFound) {
    Write-Error "✗ Podman 未安裝"
    Write-Host ""
    Write-Info "請選擇安裝方式:"
    Write-Host ""
    Write-Host "方式 1: 使用 Podman Desktop (推薦)" -ForegroundColor Yellow
    Write-Host "  1. 下載: https://podman-desktop.io/downloads/windows" -ForegroundColor Gray
    Write-Host "  2. 執行安裝程式" -ForegroundColor Gray
    Write-Host "  3. 重新開啟 PowerShell" -ForegroundColor Gray
    Write-Host ""
    Write-Host "方式 2: 使用 WinGet" -ForegroundColor Yellow
    Write-Host "  winget install RedHat.Podman-Desktop" -ForegroundColor Gray
    Write-Host ""
    Write-Host "方式 3: 使用 Chocolatey" -ForegroundColor Yellow
    Write-Host "  choco install podman-desktop" -ForegroundColor Gray
    Write-Host ""
    
    $response = Read-Host "是否要開啟 Podman Desktop 下載頁面? (Y/N)"
    if ($response -eq 'Y' -or $response -eq 'y') {
        Start-Process "https://podman-desktop.io/downloads/windows"
    }
    
    exit 1
}

# 檢查 Podman Machine
Write-Info "`n檢查 Podman Machine 狀態..."
try {
    $machines = & podman machine ls --format json 2>$null | ConvertFrom-Json
    
    if ($machines) {
        $runningMachine = $machines | Where-Object { $_.Running -eq $true }
        
        if ($runningMachine) {
            Write-Success "✓ Podman Machine 正在運行: $($runningMachine.Name)"
        } else {
            Write-Warning "⚠ Podman Machine 已存在但未運行"
            $defaultMachine = $machines | Select-Object -First 1
            Write-Info "啟動 Podman Machine: $($defaultMachine.Name)..."
            
            & podman machine start $defaultMachine.Name
            
            if ($LASTEXITCODE -eq 0) {
                Write-Success "✓ Podman Machine 已啟動"
            } else {
                Write-Error "✗ Podman Machine 啟動失敗"
                exit 1
            }
        }
    } else {
        Write-Warning "⚠ 未找到 Podman Machine"
        Write-Info "初始化 Podman Machine..."
        
        & podman machine init
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "✓ Podman Machine 已初始化"
            Write-Info "啟動 Podman Machine..."
            
            & podman machine start
            
            if ($LASTEXITCODE -eq 0) {
                Write-Success "✓ Podman Machine 已啟動"
            } else {
                Write-Error "✗ Podman Machine 啟動失敗"
                exit 1
            }
        } else {
            Write-Error "✗ Podman Machine 初始化失敗"
            exit 1
        }
    }
} catch {
    Write-Error "✗ 檢查 Podman Machine 時發生錯誤: $_"
    exit 1
}

# 測試 Podman 連接
Write-Info "`n測試 Podman 連接..."
try {
    $version = & podman version
    if ($LASTEXITCODE -eq 0) {
        Write-Success "✓ Podman 運行正常"
    } else {
        Write-Error "✗ Podman 連接失敗"
        exit 1
    }
} catch {
    Write-Error "✗ Podman 測試失敗: $_"
    exit 1
}

# 檢查 Podman Compose
Write-Info "`n檢查 Podman Compose..."
try {
    & podman compose version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Success "✓ podman compose 可用"
    } else {
        Write-Warning "⚠ podman compose 不可用,將使用 podman-compose"
    }
} catch {
    Write-Warning "⚠ podman compose 檢查失敗"
}

# 顯示系統資訊
Write-Info "`n系統資訊:"
try {
    $info = & podman info --format json | ConvertFrom-Json
    Write-Host "  OS:      $($info.host.os)" -ForegroundColor Gray
    Write-Host "  Arch:    $($info.host.arch)" -ForegroundColor Gray
    Write-Host "  CPUs:    $($info.host.cpus)" -ForegroundColor Gray
    Write-Host "  Memory:  $([math]::Round($info.host.memTotal / 1GB, 2)) GB" -ForegroundColor Gray
} catch {
    Write-Warning "無法取得系統資訊"
}

# 完成
Write-Host ""
Write-Success "╔══════════════════════════════════════════════════════════════╗"
Write-Success "║  ✓ Podman 環境檢查完成,系統已準備就緒!                     ║"
Write-Success "╚══════════════════════════════════════════════════════════════╝"
Write-Host ""
Write-Info "下一步: 執行啟動腳本"
Write-Host "  .\scripts\start-with-podman.ps1" -ForegroundColor Yellow
Write-Host ""
