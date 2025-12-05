# US3 整合測試指南

## 📋 測試環境狀態

### ✅ 已啟動服務

| 服務 | 狀態 | 地址 | 容器 |
|------|------|------|------|
| **PostgreSQL** | ✅ 運行中 | localhost:5432 | imrbs-postgres |
| **Redis** | ✅ 運行中 | localhost:6379 | imrbs-redis |
| **RabbitMQ** | ✅ 運行中 | localhost:5672 | imrbs-rabbitmq |
| **RabbitMQ Management** | ✅ 運行中 | http://localhost:15672 | imrbs-rabbitmq |
| **後端 API** | ✅ 運行中 | http://localhost:8080 | Spring Boot |
| **前端** | ✅ 運行中 | http://localhost:3000 | Vite Dev Server |

### 🔐 服務憑證

```
PostgreSQL:
  - Database: imrbs
  - Username: imrbs_user
  - Password: imrbs_pass

Redis:
  - Password: imrbs_redis_pass

RabbitMQ:
  - Username: imrbs_user
  - Password: imrbs_pass
  - Virtual Host: /imrbs (預設)
  - Management UI: http://localhost:15672

JWT:
  - Secret: imrbs-super-secret-jwt-key-for-development-only-min-32-chars
  - Access Token TTL: 15 分鐘 (900 秒)
  - Refresh Token TTL: 24 小時 (86400 秒)
```

---

## 🧪 測試場景

### 測試 1: 後端健康檢查 ✅

**目的**: 確認後端 API 正常運行

**步驟**:
```powershell
# 測試 Actuator Health Endpoint
curl http://localhost:8080/actuator/health

# 預期輸出:
# {"status":"UP"}
```

**驗證**:
- HTTP 200 OK
- JSON 回應包含 `"status":"UP"`

---

### 測試 2: API 文件訪問 ✅

**目的**: 確認 Swagger UI 可訪問

**步驟**:
1. 開啟瀏覽器
2. 訪問: http://localhost:8080/swagger-ui.html

**預期結果**:
- 顯示 Swagger UI 介面
- 可看到所有 API 端點分類:
  - Auth Controller (認證相關)
  - Reservation Controller (預約相關)
  - Room Controller (會議室相關)

---

### 測試 3: 前端首頁訪問 ⏸️ **需要手動測試**

**目的**: 驗證前端路由守衛

**步驟**:
1. 開啟瀏覽器
2. 訪問: http://localhost:3000

**預期行為**:
- ❌ **無法直接訪問首頁** (未登入)
- ✅ **自動重定向到登入頁面**: `http://localhost:3000/login`
- ✅ 顯示 "使用 SSO 登入" 按鈕
- ✅ 顯示系統功能列表
- ✅ 漸變背景動畫

**驗證點**:
- [ ] URL 自動變更為 `/login`
- [ ] 登入按鈕可見
- [ ] 無控制台錯誤

---

### 測試 4: OAuth 登入流程 ⚠️ **部分功能**

**目的**: 驗證 OAuth 2.0 流程 (無 Keycloak 時的行為)

**步驟**:
1. 訪問 http://localhost:3000/login
2. 點擊 "使用 SSO 登入" 按鈕

**預期行為** (當前配置):
- ❌ **無法完成完整 OAuth 流程** (Keycloak 未配置)
- ⚠️ **重定向到 Keycloak 會失敗**
- 預期錯誤: "無法連接到授權伺服器" 或類似訊息

**已配置的 OAuth 端點**:
```
Authorization Endpoint: http://localhost:8080/oauth2/authorize
Token Endpoint: http://localhost:8080/api/v1/auth/login
User Info Endpoint: http://localhost:8080/api/v1/auth/me
```

**⚠️ 限制**: 
- Keycloak 尚未部署 (需要獨立配置)
- 可以測試前端 OAuth 流程邏輯,但無法獲得真實 Token

---

### 測試 5: 手動 Token 測試 ✅ **可執行**

**目的**: 繞過 OAuth,直接測試 JWT 功能

**步驟 A: 使用 Swagger UI**
1. 訪問 http://localhost:8080/swagger-ui.html
2. 找到 `POST /api/v1/auth/login` 端點
3. 點擊 "Try it out"
4. **問題**: 此端點需要 OAuth `code`,無法直接調用

**步驟 B: 創建測試腳本** (推薦)

創建 `test-auth.ps1`:
```powershell
# 生成測試 JWT Token (需要後端提供測試端點)
# 或直接使用 JwtService 測試類

# 1. 檢查後端 API 健康狀態
$health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
Write-Host "Backend Health: $($health.status)"

# 2. 嘗試訪問受保護的端點 (應該返回 401)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/me"
} catch {
    Write-Host "Expected 401 Unauthorized: $($_.Exception.Response.StatusCode)"
}
```

---

### 測試 6: 前端路由守衛 ✅ **可測試**

**目的**: 驗證路由保護機制

**測試案例**:

| 路由 | 未登入行為 | 已登入行為 |
|------|-----------|-----------|
| `/` | 重定向 → `/login` | 顯示首頁 |
| `/login` | 顯示登入頁 | 重定向 → `/` |
| `/rooms/search` | 重定向 → `/login?redirect=/rooms/search` | 顯示會議室搜尋 |
| `/reservations/my` | 重定向 → `/login?redirect=/reservations/my` | 顯示我的預約 |
| `/admin/rooms` | 重定向 → `/login` | 檢查 ROOM_ADMIN 角色 |
| `/admin/users` | 重定向 → `/login` | 檢查 SYSTEM_ADMIN 角色 |
| `/403` | 顯示 403 頁面 | 顯示 403 頁面 |

**手動測試步驟**:
1. 清除 localStorage: `localStorage.clear()`
2. 依次訪問上表中的路由
3. 觀察重定向行為

**驗證指令** (瀏覽器控制台):
```javascript
// 1. 檢查當前認證狀態
localStorage.getItem('imrbs_access_token')  // 應為 null

// 2. 檢查路由守衛
window.location.pathname  // 應為 '/login'

// 3. 嘗試直接訪問受保護路由
window.location.href = '/rooms/search'  // 應重定向回 /login
```

---

### 測試 7: Token 自動刷新機制 ⏸️ **需模擬**

**目的**: 驗證 Token 過期前自動刷新

**前置條件**: 需要有效的 Refresh Token

**測試邏輯**:
1. 登入取得 Token (過期時間 15 分鐘)
2. 等待 10 分鐘 (過期前 5 分鐘)
3. 前端自動檢測並刷新 Token
4. 新 Token 存儲到 Pinia Store

**模擬測試** (瀏覽器控制台):
```javascript
// 1. 注入假 Token 到 localStorage (過期時間設為 1 分鐘後)
const fakeToken = {
  accessToken: 'fake-access-token',
  refreshToken: 'fake-refresh-token',
  expiresAt: Date.now() + 60000,  // 1 分鐘後過期
  user: {
    id: 1,
    username: 'test',
    email: 'test@example.com',
    fullName: '測試使用者',
    department: '資訊部',
    roles: ['EMPLOYEE']
  }
}

localStorage.setItem('imrbs_access_token', fakeToken.accessToken)
localStorage.setItem('imrbs_refresh_token', fakeToken.refreshToken)
localStorage.setItem('imrbs_expires_at', fakeToken.expiresAt)
localStorage.setItem('imrbs_user', JSON.stringify(fakeToken.user))

// 2. 重新載入頁面
location.reload()

// 3. 等待 2 分鐘,觀察自動刷新
// (因為 Token 已過期,會觸發刷新邏輯)
```

**預期行為**:
- ⚠️ 刷新 API 會返回 401 (因為 Refresh Token 是假的)
- ✅ Axios 攔截器捕獲 401 錯誤
- ✅ 清除所有 Token
- ✅ 重定向到 `/login`

---

## 🔧 手動測試 JWT 功能

### 方法 1: 使用後端測試類

**步驟**:
```powershell
# 1. 運行 JwtServiceTest (後端單元測試)
cd d:\developer\repos\imrbs\imrbs-web
mvn test -Dtest=JwtServiceTest

# 預期輸出: 11/11 測試通過
```

### 方法 2: 創建測試端點 (開發用)

**建議**: 在 `AuthController` 中添加臨時測試端點

```java
// ⚠️ 僅供開發測試,生產環境必須移除
@PostMapping("/dev/test-token")
public ResponseEntity<LoginResponse> generateTestToken() {
    // 創建測試使用者
    User testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("test_employee");
    testUser.setEmail("test@example.com");
    testUser.setRoles(Set.of(UserRole.EMPLOYEE));
    
    // 生成 Token
    String accessToken = jwtService.generateAccessToken(testUser.getUsername(), testUser.getRoles());
    String refreshToken = jwtService.generateRefreshToken(testUser.getUsername());
    
    return ResponseEntity.ok(new LoginResponse(
        accessToken,
        refreshToken,
        900,  // 15 分鐘
        "Bearer"
    ));
}
```

**使用測試端點**:
```powershell
# 獲取測試 Token
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/dev/test-token" -Method POST
$accessToken = $response.accessToken

# 使用 Token 訪問受保護端點
$headers = @{ Authorization = "Bearer $accessToken" }
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/me" -Headers $headers
```

---

## 🎯 測試檢查清單

### 基礎設施 ✅
- [x] PostgreSQL 運行中
- [x] Redis 運行中
- [x] RabbitMQ 運行中
- [x] 後端 API 啟動成功
- [x] 前端 Dev Server 啟動成功

### 後端功能
- [x] Actuator Health Endpoint 可訪問
- [x] Swagger UI 可訪問
- [x] JPA 實體正確載入 (6 個 Repository)
- [x] Flyway Migration 完成 (7 個腳本)
- [x] JWT Service 初始化成功
- [ ] OAuth 2.0 端點可訪問 (需 Keycloak)

### 前端功能
- [ ] 首頁自動重定向到登入頁 ⏸️ **待測試**
- [ ] 登入頁面正確顯示 ⏸️ **待測試**
- [ ] 路由守衛正確攔截 ⏸️ **待測試**
- [ ] 403 頁面可訪問 ⏸️ **待測試**
- [ ] Auth Callback 頁面可訪問 ⏸️ **待測試**

### 整合測試
- [ ] OAuth 登入流程 ⚠️ **需 Keycloak**
- [ ] Token 刷新機制 ⚠️ **需模擬**
- [ ] 角色權限控制 ⚠️ **需模擬**
- [ ] API 請求自動附加 Token ⏸️ **需手動注入**

---

## 🚀 下一步建議

### 選項 A: 配置 Keycloak (完整測試)

**步驟**:
1. 啟動 Keycloak 容器
   ```powershell
   podman run -d --name keycloak \
     -p 8180:8080 \
     -e KEYCLOAK_ADMIN=admin \
     -e KEYCLOAK_ADMIN_PASSWORD=admin \
     quay.io/keycloak/keycloak:23.0 start-dev
   ```

2. 創建 Realm: `imrbs`
3. 創建 Client: `imrbs-client`
4. 配置 Redirect URI: `http://localhost:3000/auth/callback`
5. 創建測試使用者並分配角色

6. 更新 `.env.local`:
   ```
   OAUTH2_ISSUER_URI=http://localhost:8180/realms/imrbs
   ```

7. 重啟後端

### 選項 B: 創建測試端點 (快速驗證)

如上文「方法 2」所述,在 AuthController 中添加 `/dev/test-token` 端點

### 選項 C: 編寫 E2E 測試 (自動化)

創建 Cypress 測試:
```javascript
// cypress/e2e/auth/login-redirect.cy.ts
describe('Auth - Login Redirect', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
  })

  it('should redirect to login when accessing protected route', () => {
    cy.visit('/')
    cy.url().should('include', '/login')
  })

  it('should redirect to home when accessing login while authenticated', () => {
    // Mock token
    cy.window().then((win) => {
      win.localStorage.setItem('imrbs_access_token', 'fake-token')
      win.localStorage.setItem('imrbs_expires_at', Date.now() + 900000)
    })
    cy.visit('/login')
    cy.url().should('eq', Cypress.config().baseUrl + '/')
  })
})
```

---

## 📊 測試結果摘要

### ✅ 已驗證功能
1. 後端服務成功啟動 (Spring Boot + PostgreSQL + Redis + RabbitMQ)
2. Flyway Migration 完成 (Schema v7)
3. JPA Repository 正確配置 (6 個)
4. JWT Service 初始化成功 (Access Token: 900s, Refresh Token: 86400s)
5. Swagger UI 可訪問

### ⏸️ 待手動測試
1. 前端路由守衛 (需開啟瀏覽器測試)
2. 登入頁面 UI (需開啟瀏覽器測試)
3. 403 權限頁面 (需開啟瀏覽器測試)

### ⚠️ 需額外配置
1. OAuth 2.0 完整流程 (需 Keycloak)
2. Token 自動刷新 (需真實 Token)
3. 角色權限控制 (需真實使用者)

---

## 💡 測試提示

### 瀏覽器開發者工具使用

**網路標籤**:
- 觀察 API 請求
- 檢查 Authorization Header
- 查看 401/403 錯誤

**控制台標籤**:
- 檢查前端錯誤
- 執行測試指令
- 觀察 Axios 攔截器日誌

**應用程式標籤** (Application):
- 查看 localStorage 內容
- 檢查 Token 存儲
- 清除儲存測試

**Vue DevTools**:
- 查看 Pinia Store 狀態
- 觀察 Auth Store 變化
- 檢查 Computed Properties

---

**測試環境準備完成!** 🎉

**當前狀態**: 
- ✅ 後端 API 運行中 (http://localhost:8080)
- ✅ 前端運行中 (http://localhost:3000)
- ⏸️ 等待手動測試前端功能
- ⚠️ 完整 OAuth 流程需配置 Keycloak

**建議下一步**: 開啟瀏覽器訪問 http://localhost:3000,測試路由守衛功能
