# Keycloak 自動配置腳本
# 用途: 快速配置 IMRBS Realm、Client 和測試使用者

param(
    [string]$KeycloakUrl = "http://localhost:8180",
    [string]$AdminUser = "admin",
    [string]$AdminPassword = "admin"
)

Write-Host "`n=== Keycloak 自動配置腳本 ===" -ForegroundColor Cyan
Write-Host "Keycloak URL: $KeycloakUrl`n" -ForegroundColor Yellow

# 1. 取得 Admin Token
Write-Host "步驟 1: 取得管理員 Token..." -ForegroundColor Yellow
try {
    $tokenResponse = Invoke-RestMethod -Uri "$KeycloakUrl/realms/master/protocol/openid-connect/token" `
        -Method POST `
        -ContentType "application/x-www-form-urlencoded" `
        -Body @{
            username = $AdminUser
            password = $AdminPassword
            grant_type = "password"
            client_id = "admin-cli"
        }
    
    $adminToken = $tokenResponse.access_token
    Write-Host "✅ 已取得管理員 Token" -ForegroundColor Green
} catch {
    Write-Host "❌ 無法取得 Token: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
}

# 2. 創建 IMRBS Realm
Write-Host "`n步驟 2: 創建 IMRBS Realm..." -ForegroundColor Yellow
$realmConfig = @{
    realm = "imrbs"
    enabled = $true
    displayName = "IMRBS 會議室預約系統"
    loginWithEmailAllowed = $true
    registrationAllowed = $false
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$KeycloakUrl/admin/realms" `
        -Method POST `
        -Headers $headers `
        -Body $realmConfig `
        -ErrorAction Stop | Out-Null
    Write-Host "✅ Realm 'imrbs' 已創建" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "⚠️  Realm 'imrbs' 已存在,跳過" -ForegroundColor Yellow
    } else {
        Write-Host "❌ 創建 Realm 失敗: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 3. 創建 Client (imrbs-web)
Write-Host "`n步驟 3: 創建 Client 'imrbs-web'..." -ForegroundColor Yellow
$clientConfig = @{
    clientId = "imrbs-web"
    name = "IMRBS Web Application"
    description = "IMRBS 前端應用程式"
    enabled = $true
    publicClient = $true
    protocol = "openid-connect"
    directAccessGrantsEnabled = $false
    standardFlowEnabled = $true
    implicitFlowEnabled = $false
    redirectUris = @(
        "http://localhost:3000/*"
        "http://localhost:5173/*"
    )
    webOrigins = @(
        "http://localhost:3000"
        "http://localhost:5173"
    )
    attributes = @{
        "post.logout.redirect.uris" = "http://localhost:3000##http://localhost:5173"
    }
} | ConvertTo-Json -Depth 10

try {
    Invoke-RestMethod -Uri "$KeycloakUrl/admin/realms/imrbs/clients" `
        -Method POST `
        -Headers $headers `
        -Body $clientConfig `
        -ErrorAction Stop | Out-Null
    Write-Host "✅ Client 'imrbs-web' 已創建" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "⚠️  Client 'imrbs-web' 已存在,跳過" -ForegroundColor Yellow
    } else {
        Write-Host "❌ 創建 Client 失敗: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4. 創建測試使用者
Write-Host "`n步驟 4: 創建測試使用者..." -ForegroundColor Yellow

$users = @(
    @{
        username = "test.employee"
        email = "test.employee@example.com"
        firstName = "測試"
        lastName = "員工"
        enabled = $true
        emailVerified = $true
        credentials = @(
            @{
                type = "password"
                value = "password123"
                temporary = $false
            }
        )
    },
    @{
        username = "admin.user"
        email = "admin.user@example.com"
        firstName = "系統"
        lastName = "管理員"
        enabled = $true
        emailVerified = $true
        credentials = @(
            @{
                type = "password"
                value = "admin123"
                temporary = $false
            }
        )
    }
)

foreach ($user in $users) {
    $userJson = $user | ConvertTo-Json -Depth 10
    try {
        Invoke-RestMethod -Uri "$KeycloakUrl/admin/realms/imrbs/users" `
            -Method POST `
            -Headers $headers `
            -Body $userJson `
            -ErrorAction Stop | Out-Null
        Write-Host "✅ 使用者 '$($user.username)' 已創建" -ForegroundColor Green
    } catch {
        if ($_.Exception.Response.StatusCode -eq 409) {
            Write-Host "⚠️  使用者 '$($user.username)' 已存在,跳過" -ForegroundColor Yellow
        } else {
            Write-Host "❌ 創建使用者 '$($user.username)' 失敗: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

# 完成
Write-Host "`n=== 配置完成 ===" -ForegroundColor Green
Write-Host "`n測試帳號:" -ForegroundColor Cyan
Write-Host "  1. 員工帳號:" -ForegroundColor White
Write-Host "     Username: test.employee" -ForegroundColor Yellow
Write-Host "     Password: password123" -ForegroundColor Yellow
Write-Host "`n  2. 管理員帳號:" -ForegroundColor White
Write-Host "     Username: admin.user" -ForegroundColor Yellow
Write-Host "     Password: admin123" -ForegroundColor Yellow
Write-Host "`n前端應用: http://localhost:3000" -ForegroundColor Cyan
Write-Host "Keycloak 管理: $KeycloakUrl/admin" -ForegroundColor Cyan
Write-Host ""
