# PostgreSQL 資料庫設定腳本
# 此腳本將建立 IMRBS 所需的資料庫和用戶

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  IMRBS 資料庫設定程式" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# PostgreSQL 路徑
$psqlPath = "D:\Program Files\PostgreSQL\18\bin\psql.exe"

# 檢查 psql 是否存在
if (-not (Test-Path $psqlPath)) {
    Write-Host "錯誤: 找不到 psql.exe" -ForegroundColor Red
    Write-Host "請確認 PostgreSQL 安裝路徑: $psqlPath" -ForegroundColor Yellow
    exit 1
}

Write-Host "請輸入 PostgreSQL postgres 用戶的密碼:" -ForegroundColor Yellow
$postgresPassword = Read-Host -AsSecureString
$env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($postgresPassword)
)

Write-Host ""
Write-Host "正在連接 PostgreSQL..." -ForegroundColor Cyan

# 測試連接
$testResult = & $psqlPath -U postgres -c "SELECT version();" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "錯誤: 無法連接 PostgreSQL" -ForegroundColor Red
    Write-Host $testResult -ForegroundColor Red
    $env:PGPASSWORD = ""
    exit 1
}

Write-Host "✓ 連接成功" -ForegroundColor Green
Write-Host ""

# 執行資料庫建立腳本
Write-Host "正在建立資料庫..." -ForegroundColor Cyan
$result = & $psqlPath -U postgres -f "$PSScriptRoot\setup-db.sql" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "======================================" -ForegroundColor Green
    Write-Host "  ✓ 資料庫設定完成!" -ForegroundColor Green
    Write-Host "======================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "資料庫連線資訊:" -ForegroundColor Cyan
    Write-Host "  資料庫名稱: imrbs" -ForegroundColor White
    Write-Host "  用戶名稱:   imrbs_user" -ForegroundColor White
    Write-Host "  密碼:       imrbs_pass" -ForegroundColor White
    Write-Host "  主機:       localhost" -ForegroundColor White
    Write-Host "  連接埠:     5432" -ForegroundColor White
    Write-Host "  JDBC URL:   jdbc:postgresql://localhost:5432/imrbs" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "錯誤: 資料庫建立失敗" -ForegroundColor Red
    Write-Host $result -ForegroundColor Red
    $env:PGPASSWORD = ""
    exit 1
}

# 清除密碼
$env:PGPASSWORD = ""

Write-Host "按任意鍵退出..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
