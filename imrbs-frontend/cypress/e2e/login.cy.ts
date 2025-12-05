/// <reference types="cypress" />

/**
 * 登入頁面 E2E 測試
 * 
 * 測試場景:
 * 1. 顯示 SSO 登入按鈕
 * 2. 點擊 SSO 登入按鈕跳轉至 OAuth Provider
 * 3. OAuth 回調成功後自動登入並跳轉首頁
 * 4. 登入後顯示使用者姓名與角色
 * 5. Access Token 過期時自動使用 Refresh Token 更新
 * 6. Refresh Token 過期時重定向至登入頁
 * 7. 登出功能清除 Token 並返回登入頁
 * 8. 未登入時存取受保護路由應重定向至登入頁
 * 
 * @author IMRBS Team
 * @since 2025-11-24
 */

describe('登入頁面 E2E 測試', () => {
  const LOGIN_URL = '/login'
  const HOME_URL = '/'
  const OAUTH_CALLBACK_URL = '/auth/callback'
  const API_BASE_URL = (Cypress.env('VITE_API_BASE_URL') as string) || 'http://localhost:8080/api/v1'

  beforeEach(() => {
    // 清除所有 Cookie 和 LocalStorage
    cy.clearCookies()
    cy.clearLocalStorage()
    
    // 攔截 API 請求,使用 Mock Service Worker 模擬後端響應
    setupApiMocks()
  })

  /**
   * 測試場景 1: 顯示 SSO 登入按鈕
   * 
   * Given: 使用者訪問登入頁面
   * When: 頁面載入完成
   * Then: 顯示「使用公司帳號登入」按鈕
   */
  it('應顯示 SSO 登入按鈕', () => {
    cy.visit(LOGIN_URL)
    
    cy.get('[data-cy=sso-login-button]')
      .should('be.visible')
      .should('contain.text', '使用公司帳號登入')
    
    cy.get('[data-cy=login-description]')
      .should('contain.text', '會議室預約系統')
  })

  /**
   * 測試場景 2: 點擊 SSO 登入按鈕跳轉至 OAuth Provider
   * 
   * Given: 使用者在登入頁面
   * When: 點擊「使用公司帳號登入」按鈕
   * Then: 跳轉至 OAuth Provider 認證頁面 (模擬 Keycloak)
   */
  it('應跳轉至 OAuth Provider 認證頁面', () => {
    cy.visit(LOGIN_URL)
    
    // 攔截 OAuth 跳轉 (實際會跳轉到 Keycloak, 測試中模擬返回)
    cy.window().then((win) => {
      cy.stub(win, 'location').callsFake((url) => {
        expect(url).to.include('/oauth2/authorize')
        expect(url).to.include('client_id=')
        expect(url).to.include('redirect_uri=')
        expect(url).to.include('response_type=code')
        
        // 模擬 OAuth 回調,返回 Authorization Code
        cy.visit(`${OAUTH_CALLBACK_URL}?code=mock_auth_code_12345`)
      })
    })
    
    cy.get('[data-cy=sso-login-button]').click()
  })

  /**
   * 測試場景 3: OAuth 回調成功後自動登入並跳轉首頁
   * 
   * Given: OAuth Provider 返回有效的 Authorization Code
   * When: 前端調用 POST /api/v1/auth/login 交換 Token
   * Then: 儲存 Access Token, 跳轉至首頁
   */
  it('應在 OAuth 回調後自動登入並跳轉首頁', () => {
    // 模擬 OAuth 回調
    cy.visit(`${OAUTH_CALLBACK_URL}?code=mock_auth_code_12345`)
    
    // 等待 POST /auth/login 請求完成
    cy.wait('@loginRequest').its('response.statusCode').should('eq', 200)
    
    // 驗證跳轉至首頁
    cy.url().should('eq', Cypress.config().baseUrl + HOME_URL)
    
    // 驗證 Access Token 已儲存 (應存在 LocalStorage 或 Memory)
    cy.window().then((win) => {
      const authStore = win.__PINIA__.state.value.auth
      expect(authStore.accessToken).to.not.be.null
      expect(authStore.isAuthenticated).to.be.true
    })
  })

  /**
   * 測試場景 4: 登入後顯示使用者姓名與角色
   * 
   * Given: 使用者已成功登入
   * When: 訪問首頁
   * Then: 顯示使用者姓名「張三」與角色「員工」
   */
  it('應顯示使用者姓名與角色', () => {
    // 模擬已登入狀態
    loginWithMockToken()
    
    cy.visit(HOME_URL)
    
    // 等待 GET /auth/me 請求完成
    cy.wait('@getMeRequest')
    
    // 驗證顯示使用者資訊
    cy.get('[data-cy=user-name]').should('contain.text', '張三')
    cy.get('[data-cy=user-role]').should('contain.text', '員工')
    cy.get('[data-cy=user-avatar]').should('be.visible')
  })

  /**
   * 測試場景 5: Access Token 過期時自動使用 Refresh Token 更新
   * 
   * Given: Access Token 已過期, Refresh Token 仍有效
   * When: 調用需要認證的 API (如 GET /rooms)
   * Then: 自動調用 POST /auth/refresh, 獲取新 Token, API 請求成功
   */
  it('應在 Access Token 過期時自動刷新', () => {
    // 模擬已登入但 Token 即將過期
    loginWithMockToken({ expiresIn: 1 }) // 1 秒後過期
    
    cy.visit('/rooms')
    
    // 等待 1 秒讓 Token 過期
    cy.wait(1100)
    
    // 調用 API 觸發自動刷新
    cy.get('[data-cy=search-button]').click()
    
    // 驗證自動調用 POST /auth/refresh
    cy.wait('@refreshRequest').its('response.statusCode').should('eq', 200)
    
    // 驗證 GET /rooms 請求使用新 Token 成功
    cy.wait('@getRoomsRequest').its('response.statusCode').should('eq', 200)
    
    // 驗證新 Token 已儲存
    cy.window().then((win) => {
      const authStore = win.__PINIA__.state.value.auth
      expect(authStore.accessToken).to.not.equal('mock_expired_token')
    })
  })

  /**
   * 測試場景 6: Refresh Token 過期時重定向至登入頁
   * 
   * Given: Access Token 和 Refresh Token 皆已過期
   * When: 調用 POST /auth/refresh 返回 401
   * Then: 清除 Token, 顯示提示訊息, 重定向至登入頁
   */
  it('應在 Refresh Token 過期時重定向至登入頁', () => {
    // 模擬 Refresh Token 過期的響應
    cy.intercept('POST', `${API_BASE_URL}/auth/refresh`, {
      statusCode: 401,
      body: {
        error: 'token_expired',
        message: 'Refresh Token 已過期,請重新登入'
      }
    }).as('refreshExpiredRequest')
    
    loginWithMockToken({ expiresIn: 1 })
    cy.visit('/rooms')
    cy.wait(1100)
    
    cy.get('[data-cy=search-button]').click()
    
    // 等待 Refresh 失敗
    cy.wait('@refreshExpiredRequest')
    
    // 驗證顯示提示訊息
    cy.get('[data-cy=error-toast]')
      .should('be.visible')
      .should('contain.text', '登入已過期')
    
    // 驗證重定向至登入頁
    cy.url().should('include', LOGIN_URL)
    
    // 驗證 Token 已清除
    cy.window().then((win) => {
      const authStore = win.__PINIA__.state.value.auth
      expect(authStore.accessToken).to.be.null
      expect(authStore.isAuthenticated).to.be.false
    })
  })

  /**
   * 測試場景 7: 登出功能清除 Token 並返回登入頁
   * 
   * Given: 使用者已登入
   * When: 點擊「登出」按鈕
   * Then: 調用 POST /auth/logout, 清除 Token, 跳轉至登入頁
   */
  it('應成功登出並清除 Token', () => {
    loginWithMockToken()
    cy.visit(HOME_URL)
    
    // 點擊使用者選單
    cy.get('[data-cy=user-menu]').click()
    
    // 點擊登出按鈕
    cy.get('[data-cy=logout-button]').click()
    
    // 等待 POST /auth/logout 請求 (可選,用於撤銷 Refresh Token)
    cy.wait('@logoutRequest').its('response.statusCode').should('eq', 204)
    
    // 驗證跳轉至登入頁
    cy.url().should('include', LOGIN_URL)
    
    // 驗證 Token 已清除
    cy.window().then((win) => {
      const authStore = win.__PINIA__.state.value.auth
      expect(authStore.accessToken).to.be.null
      expect(authStore.user).to.be.null
    })
  })

  /**
   * 測試場景 8: 未登入時存取受保護路由應重定向至登入頁
   * 
   * Given: 使用者未登入
   * When: 直接訪問 /rooms 或 /reservations/my
   * Then: 自動重定向至登入頁, 並保留原始 URL (用於登入後跳轉)
   */
  it('應在未登入時重定向至登入頁', () => {
    cy.visit('/reservations/my')
    
    // 驗證重定向至登入頁
    cy.url().should('include', LOGIN_URL)
    cy.url().should('include', 'redirect=%2Freservations%2Fmy') // 保留原始 URL
    
    // 驗證顯示提示訊息
    cy.get('[data-cy=login-hint]')
      .should('be.visible')
      .should('contain.text', '請先登入以繼續')
  })

  // ==================== Helper Functions ====================

  /**
   * 設定 API Mock 攔截器
   */
  function setupApiMocks() {
    // POST /auth/login - 登入成功
    cy.intercept('POST', `${API_BASE_URL}/auth/login`, {
      statusCode: 200,
      body: {
        access_token: 'mock_jwt_access_token_eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9',
        expires_in: 900 // 15 分鐘
      }
    }).as('loginRequest')

    // POST /auth/refresh - 刷新 Token 成功
    cy.intercept('POST', `${API_BASE_URL}/auth/refresh`, {
      statusCode: 200,
      body: {
        access_token: 'mock_new_jwt_access_token_refreshed',
        expires_in: 900
      }
    }).as('refreshRequest')

    // GET /auth/me - 獲取使用者資訊
    cy.intercept('GET', `${API_BASE_URL}/auth/me`, {
      statusCode: 200,
      body: {
        id: 1001,
        name: '張三',
        email: 'zhangsan@company.com',
        role: 'EMPLOYEE',
        department: '資訊部'
      }
    }).as('getMeRequest')

    // POST /auth/logout - 登出
    cy.intercept('POST', `${API_BASE_URL}/auth/logout`, {
      statusCode: 204
    }).as('logoutRequest')

    // GET /rooms - 查詢會議室 (需認證)
    cy.intercept('GET', `${API_BASE_URL}/rooms`, {
      statusCode: 200,
      body: {
        data: [
          { id: 1, name: '301 會議室', capacity: 10, floor: '3F' }
        ],
        total: 1
      }
    }).as('getRoomsRequest')
  }

  /**
   * 使用 Mock Token 模擬登入狀態
   */
  function loginWithMockToken(options = {}) {
    const accessToken = options.accessToken || 'mock_jwt_access_token'
    const expiresIn = options.expiresIn || 900

    cy.window().then((win) => {
      // 直接設定 Pinia Store 狀態
      if (win.__PINIA__) {
        const authStore = win.__PINIA__.state.value.auth
        authStore.accessToken = accessToken
        authStore.expiresAt = Date.now() + expiresIn * 1000
        authStore.isAuthenticated = true
        authStore.user = {
          id: 1001,
          name: '張三',
          email: 'zhangsan@company.com',
          role: 'EMPLOYEE'
        }
      }
    })
  }
})
