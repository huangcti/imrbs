/// <reference types="cypress" />

/**
 * T165 [P] [US8] 前端語言切換 E2E 測試
 * 
 * 測試場景:
 * 1. 語言切換元件顯示在 Header 中
 * 2. 預設語言為繁體中文
 * 3. 切換為英文後介面文字變更
 * 4. 切換為繁體中文後介面文字變更
 * 5. 語言偏好儲存至 localStorage
 * 6. 重新載入頁面保持語言設定
 * 7. 登入後語言偏好同步至後端
 * 8. 瀏覽器語言自動偵測 (en-US)
 * 9. 瀏覽器語言自動偵測 (zh-TW)
 * 10. 未支援語言預設為繁體中文
 * 11. 語言切換後日期格式正確顯示
 * 12. 語言切換後 Toast 訊息正確顯示
 * 
 * @author IMRBS Team
 * @since 2025-01-24
 */

describe('語言切換 E2E 測試', () => {
  const HOME_URL = '/'
  const LOGIN_URL = '/login'
  const API_BASE_URL = (Cypress.env('VITE_API_BASE_URL') as string) || 'http://localhost:8080/api/v1'

  beforeEach(() => {
    // 清除所有 Cookie 和 LocalStorage
    cy.clearCookies()
    cy.clearLocalStorage()
    
    // 設定 API Mock
    setupApiMocks()
  })

  /**
   * 測試場景 1: 語言切換元件顯示在 Header 中
   */
  it('1. 應在 Header 中顯示語言切換元件', () => {
    cy.visit(HOME_URL)
    
    cy.get('[data-cy=language-switcher]')
      .should('be.visible')
    
    cy.get('[data-cy=language-switcher]')
      .should('contain.text', '繁體中文')
  })

  /**
   * 測試場景 2: 預設語言為繁體中文
   */
  it('2. 預設語言應為繁體中文', () => {
    cy.visit(HOME_URL)
    
    // 檢查頁面標題
    cy.get('[data-cy=page-title]')
      .should('contain.text', '會議室預約系統')
    
    // 檢查導航選單
    cy.get('[data-cy=nav-home]')
      .should('contain.text', '首頁')
    
    cy.get('[data-cy=nav-rooms]')
      .should('contain.text', '會議室')
    
    cy.get('[data-cy=nav-reservations]')
      .should('contain.text', '我的預約')
  })

  /**
   * 測試場景 3: 切換為英文後介面文字變更
   */
  it('3. 切換為英文後介面文字應變更', () => {
    cy.visit(HOME_URL)
    
    // 點擊語言切換元件
    cy.get('[data-cy=language-switcher]').click()
    
    // 選擇英文
    cy.get('[data-cy=lang-option-en]').click()
    
    // 驗證介面文字變為英文
    cy.get('[data-cy=page-title]')
      .should('contain.text', 'Meeting Room Booking')
    
    cy.get('[data-cy=nav-home]')
      .should('contain.text', 'Home')
    
    cy.get('[data-cy=nav-rooms]')
      .should('contain.text', 'Rooms')
    
    cy.get('[data-cy=nav-reservations]')
      .should('contain.text', 'My Reservations')
    
    // 語言切換元件顯示當前語言
    cy.get('[data-cy=language-switcher]')
      .should('contain.text', 'English')
  })

  /**
   * 測試場景 4: 切換為繁體中文後介面文字變更
   */
  it('4. 切換為繁體中文後介面文字應變更', () => {
    cy.visit(HOME_URL)
    
    // 先切換為英文
    cy.get('[data-cy=language-switcher]').click()
    cy.get('[data-cy=lang-option-en]').click()
    
    // 再切換回繁體中文
    cy.get('[data-cy=language-switcher]').click()
    cy.get('[data-cy=lang-option-zh-TW]').click()
    
    // 驗證介面文字變回繁體中文
    cy.get('[data-cy=page-title]')
      .should('contain.text', '會議室預約系統')
    
    cy.get('[data-cy=language-switcher]')
      .should('contain.text', '繁體中文')
  })

  /**
   * 測試場景 5: 語言偏好儲存至 localStorage
   */
  it('5. 語言偏好應儲存至 localStorage', () => {
    cy.visit(HOME_URL)
    
    // 切換為英文
    cy.get('[data-cy=language-switcher]').click()
    cy.get('[data-cy=lang-option-en]').click()
    
    // 驗證 localStorage
    cy.window().then((win) => {
      const lang = win.localStorage.getItem('locale')
      expect(lang).to.eq('en')
    })
    
    // 切換為繁體中文
    cy.get('[data-cy=language-switcher]').click()
    cy.get('[data-cy=lang-option-zh-TW]').click()
    
    cy.window().then((win) => {
      const lang = win.localStorage.getItem('locale')
      expect(lang).to.eq('zh-TW')
    })
  })

  /**
   * 測試場景 6: 重新載入頁面保持語言設定
   */
  it('6. 重新載入頁面應保持語言設定', () => {
    cy.visit(HOME_URL)
    
    // 切換為英文
    cy.get('[data-cy=language-switcher]').click()
    cy.get('[data-cy=lang-option-en]').click()
    
    // 重新載入頁面
    cy.reload()
    
    // 驗證語言仍為英文
    cy.get('[data-cy=page-title]')
      .should('contain.text', 'Meeting Room Booking')
    
    cy.get('[data-cy=language-switcher]')
      .should('contain.text', 'English')
  })

  /**
   * 測試場景 7: 登入後語言偏好同步至後端
   */
  it('7. 登入後語言偏好應同步至後端', () => {
    // 模擬登入
    mockAuthentication()
    
    cy.visit(HOME_URL)
    
    // 攔截語言更新 API
    cy.intercept('PUT', `${API_BASE_URL}/users/me/language`, {
      statusCode: 200,
      body: {
        success: true,
        data: { languagePreference: 'en' },
        message: 'Language preference updated'
      }
    }).as('updateLanguage')
    
    // 切換為英文
    cy.get('[data-cy=language-switcher]').click()
    cy.get('[data-cy=lang-option-en]').click()
    
    // 驗證 API 被呼叫
    cy.wait('@updateLanguage').its('request.body').should('deep.eq', {
      language: 'en'
    })
  })

  /**
   * 測試場景 8: 瀏覽器語言自動偵測 (en-US)
   */
  it('8. 應自動偵測瀏覽器語言 (en-US)', () => {
    // 模擬瀏覽器語言為 en-US
    cy.visit(HOME_URL, {
      onBeforeLoad(win) {
        Object.defineProperty(win.navigator, 'language', {
          value: 'en-US'
        })
        Object.defineProperty(win.navigator, 'languages', {
          value: ['en-US', 'en']
        })
      }
    })
    
    // 無 localStorage 設定時，應使用瀏覽器語言
    cy.get('[data-cy=page-title]')
      .should('contain.text', 'Meeting Room Booking')
  })

  /**
   * 測試場景 9: 瀏覽器語言自動偵測 (zh-TW)
   */
  it('9. 應自動偵測瀏覽器語言 (zh-TW)', () => {
    // 模擬瀏覽器語言為 zh-TW
    cy.visit(HOME_URL, {
      onBeforeLoad(win) {
        Object.defineProperty(win.navigator, 'language', {
          value: 'zh-TW'
        })
        Object.defineProperty(win.navigator, 'languages', {
          value: ['zh-TW', 'zh']
        })
      }
    })
    
    cy.get('[data-cy=page-title]')
      .should('contain.text', '會議室預約系統')
  })

  /**
   * 測試場景 10: 未支援語言預設為繁體中文
   */
  it('10. 未支援語言應預設為繁體中文', () => {
    // 模擬瀏覽器語言為日文
    cy.visit(HOME_URL, {
      onBeforeLoad(win) {
        Object.defineProperty(win.navigator, 'language', {
          value: 'ja-JP'
        })
      }
    })
    
    // 預設為繁體中文
    cy.get('[data-cy=page-title]')
      .should('contain.text', '會議室預約系統')
  })

  /**
   * 測試場景 11: 語言切換後日期格式正確顯示
   */
  it('11. 語言切換後日期格式應正確顯示', () => {
    mockAuthentication()
    mockReservationsWithDates()
    
    cy.visit('/reservations')
    
    // 繁體中文日期格式: 2025年1月24日
    cy.get('[data-cy=reservation-date]').first()
      .should('match', /\d{4}年\d{1,2}月\d{1,2}日/)
    
    // 切換為英文
    cy.get('[data-cy=language-switcher]').click()
    cy.get('[data-cy=lang-option-en]').click()
    
    // 英文日期格式: January 24, 2025
    cy.get('[data-cy=reservation-date]').first()
      .should('match', /[A-Z][a-z]+ \d{1,2}, \d{4}/)
  })

  /**
   * 測試場景 12: 語言切換後 Toast 訊息正確顯示
   */
  it('12. 語言切換後 Toast 訊息應正確顯示', () => {
    mockAuthentication()
    
    cy.visit(HOME_URL)
    
    // 切換為英文
    cy.get('[data-cy=language-switcher]').click()
    cy.get('[data-cy=lang-option-en]').click()
    
    // 驗證 Toast 訊息為英文
    cy.get('[data-cy=toast]')
      .should('be.visible')
      .should('contain.text', 'Language changed to English')
    
    // 切換為繁體中文
    cy.get('[data-cy=language-switcher]').click()
    cy.get('[data-cy=lang-option-zh-TW]').click()
    
    // 驗證 Toast 訊息為繁體中文
    cy.get('[data-cy=toast]')
      .should('be.visible')
      .should('contain.text', '語言已切換為繁體中文')
  })

  // ===== Helper Functions =====

  function setupApiMocks() {
    // Mock 健康檢查
    cy.intercept('GET', `${API_BASE_URL}/health`, {
      statusCode: 200,
      body: { status: 'UP' }
    })
    
    // Mock 會議室清單
    cy.intercept('GET', `${API_BASE_URL}/rooms*`, {
      statusCode: 200,
      body: {
        success: true,
        data: []
      }
    })
  }

  function mockAuthentication() {
    // 設定假的 Token
    cy.window().then((win) => {
      win.localStorage.setItem('access_token', 'mock-access-token')
      win.localStorage.setItem('refresh_token', 'mock-refresh-token')
    })
    
    // Mock 使用者資訊
    cy.intercept('GET', `${API_BASE_URL}/auth/me`, {
      statusCode: 200,
      body: {
        success: true,
        data: {
          id: 1,
          employeeId: 'E001',
          email: 'test@example.com',
          fullName: '測試使用者',
          role: 'EMPLOYEE',
          languagePreference: 'zh-TW'
        }
      }
    })
  }

  function mockReservationsWithDates() {
    cy.intercept('GET', `${API_BASE_URL}/reservations*`, {
      statusCode: 200,
      body: {
        success: true,
        data: [
          {
            id: 1,
            meetingTitle: '專案討論會議',
            startTime: '2025-01-24T14:00:00',
            endTime: '2025-01-24T15:00:00',
            roomName: 'A01 會議室',
            status: 'CONFIRMED'
          }
        ]
      }
    })
  }
})
