/**
 * T054 [P] [US1] 撰寫前端預約表單 E2E 測試
 * 
 * 測試情境: 員工登入 → 查詢會議室 → 查看詳情 → 提交預約 → 收到確認
 * 
 * 驗收標準:
 * 1. 使用者可以篩選會議室 (日期、時間、容量)
 * 2. 使用者可以查看會議室詳情和可用時段
 * 3. 使用者可以填寫預約表單並提交
 * 4. 系統顯示預約成功訊息
 */

describe('US1: 員工查詢與預約會議室', () => {
  beforeEach(() => {
    // 設置 mock login
    cy.login()
    
    // Mock API responses
    cy.mockRoomAPI()
    
    // 訪問會議室搜尋頁面
    cy.visit('/rooms')
  })

  describe('會議室查詢功能', () => {
    it('應該顯示會議室篩選表單', () => {
      // 驗證篩選表單元素存在
      cy.get('[data-testid="date-input"]').should('exist')
      cy.get('[data-testid="start-time-input"]').should('exist')
      cy.get('[data-testid="end-time-input"]').should('exist')
      cy.get('[data-testid="capacity-input"]').should('exist')
      cy.get('[data-testid="search-button"]').should('exist')
    })

    it('應該根據篩選條件查詢會議室', () => {
      // 填寫篩選條件
      cy.get('[data-testid="date-input"]').type('2025-11-21')
      cy.get('[data-testid="start-time-input"]').type('09:00')
      cy.get('[data-testid="end-time-input"]').type('10:00')
      cy.get('[data-testid="capacity-input"]').clear().type('10')

      // 點擊搜尋按鈕
      cy.get('[data-testid="search-button"]').click()

      // 等待 API 回應
      cy.wait('@getRooms')

      // 驗證會議室列表顯示
      cy.get('[data-testid="room-card"]').should('have.length.at.least', 1)
      cy.get('[data-testid="room-card"]').first().should('contain', '會議室 A')
    })

    it('應該顯示「無可用會議室」訊息當沒有結果時', () => {
      // Mock 空結果
      cy.intercept('GET', '/api/v1/rooms*', {
        statusCode: 200,
        body: { data: [], total: 0 }
      }).as('getEmptyRooms')

      // 搜尋
      cy.get('[data-testid="date-input"]').type('2025-11-21')
      cy.get('[data-testid="start-time-input"]').type('09:00')
      cy.get('[data-testid="end-time-input"]').type('10:00')
      cy.get('[data-testid="search-button"]').click()

      cy.wait('@getEmptyRooms')

      // 驗證空狀態訊息
      cy.contains('無可用會議室').should('be.visible')
    })
  })

  describe('會議室詳情功能', () => {
    beforeEach(() => {
      // 執行搜尋
      cy.get('[data-testid="date-input"]').type('2025-11-21')
      cy.get('[data-testid="start-time-input"]').type('09:00')
      cy.get('[data-testid="end-time-input"]').type('10:00')
      cy.get('[data-testid="search-button"]').click()
      cy.wait('@getRooms')
    })

    it('應該顯示會議室基本資訊', () => {
      // 點擊會議室卡片
      cy.get('[data-testid="room-card"]').first().click()

      // 驗證詳情顯示
      cy.get('[data-testid="room-detail"]').should('be.visible')
      cy.get('[data-testid="room-name"]').should('contain', '會議室 A')
      cy.get('[data-testid="room-capacity"]').should('contain', '10')
      cy.get('[data-testid="room-equipment"]').should('contain', '投影機')
    })

    it('應該顯示會議室可用時段', () => {
      // 點擊會議室卡片
      cy.get('[data-testid="room-card"]').first().click()

      // 等待可用時段 API
      cy.wait('@getRoomAvailability')

      // 驗證時段列表
      cy.get('[data-testid="time-slot"]').should('have.length.at.least', 1)
      cy.get('[data-testid="time-slot"]').first().should('contain', '09:00')
    })
  })

  describe('預約創建功能', () => {
    beforeEach(() => {
      // 執行搜尋並選擇會議室
      cy.get('[data-testid="date-input"]').type('2025-11-21')
      cy.get('[data-testid="start-time-input"]').type('09:00')
      cy.get('[data-testid="end-time-input"]').type('10:00')
      cy.get('[data-testid="search-button"]').click()
      cy.wait('@getRooms')
      
      cy.get('[data-testid="room-card"]').first().click()
      cy.wait('@getRoomAvailability')
    })

    it('應該開啟預約表單', () => {
      // 選擇時段
      cy.get('[data-testid="time-slot"]').first().click()

      // 驗證預約表單顯示
      cy.get('[data-testid="reservation-form"]').should('be.visible')
      cy.get('[data-testid="purpose-input"]').should('exist')
      cy.get('[data-testid="participants-input"]').should('exist')
      cy.get('[data-testid="submit-button"]').should('exist')
    })

    it('應該成功提交預約 (完整流程)', () => {
      // 選擇時段
      cy.get('[data-testid="time-slot"]').first().click()

      // 填寫預約表單
      cy.get('[data-testid="purpose-input"]').type('團隊週會 - 討論 Q4 專案進度')
      cy.get('[data-testid="participants-input"]').type('member1@company.com\nmember2@company.com')

      // 提交預約
      cy.get('[data-testid="submit-button"]').click()

      // 等待 API 回應
      cy.wait('@createReservation')

      // 驗證成功訊息
      cy.contains('預約成功').should('be.visible')
      cy.contains('預約編號').should('be.visible')
    })

    it('應該驗證必填欄位', () => {
      // 選擇時段
      cy.get('[data-testid="time-slot"]').first().click()

      // 不填寫直接提交
      cy.get('[data-testid="submit-button"]').click()

      // 驗證錯誤訊息
      cy.get('[data-testid="purpose-input"]').should('have.attr', 'required')
    })

    it('應該驗證會議目的長度限制', () => {
      // 選擇時段
      cy.get('[data-testid="time-slot"]').first().click()

      // 填寫過短的會議目的
      cy.get('[data-testid="purpose-input"]').type('短')
      cy.get('[data-testid="submit-button"]').click()

      // 應該顯示錯誤或無法提交
      cy.get('[data-testid="purpose-input"]').should('have.attr', 'minlength', '5')
    })

    it('應該驗證參與者 Email 格式', () => {
      // 選擇時段
      cy.get('[data-testid="time-slot"]').first().click()

      // 填寫有效會議目的
      cy.get('[data-testid="purpose-input"]').type('團隊會議討論專案')
      
      // 填寫無效 email
      cy.get('[data-testid="participants-input"]').type('invalid-email')
      cy.get('[data-testid="submit-button"]').click()

      // 應該顯示驗證錯誤
      cy.contains(/email|格式|無效/i).should('be.visible')
    })

    it('應該處理預約衝突錯誤', () => {
      // Mock 衝突錯誤回應
      cy.intercept('POST', '/api/v1/reservations', {
        statusCode: 409,
        body: {
          error: 'CONFLICT',
          message: '此時段已被預約'
        }
      }).as('createConflictReservation')

      // 選擇時段並填寫表單
      cy.get('[data-testid="time-slot"]').first().click()
      cy.get('[data-testid="purpose-input"]').type('團隊會議')
      cy.get('[data-testid="submit-button"]').click()

      cy.wait('@createConflictReservation')

      // 驗證衝突錯誤訊息
      cy.contains(/衝突|已被預約/i).should('be.visible')
    })

    it('應該可以取消預約', () => {
      // 選擇時段
      cy.get('[data-testid="time-slot"]').first().click()

      // 點擊取消按鈕
      cy.get('[data-testid="cancel-button"]').click()

      // 表單應該關閉
      cy.get('[data-testid="reservation-form"]').should('not.exist')
    })
  })

  describe('使用者體驗驗證', () => {
    it('應該在 30 秒內完成預約流程', () => {
      const startTime = Date.now()

      // 1. 搜尋會議室
      cy.get('[data-testid="date-input"]').type('2025-11-21')
      cy.get('[data-testid="start-time-input"]').type('09:00')
      cy.get('[data-testid="end-time-input"]').type('10:00')
      cy.get('[data-testid="search-button"]').click()
      cy.wait('@getRooms')

      // 2. 選擇會議室
      cy.get('[data-testid="room-card"]').first().click()
      cy.wait('@getRoomAvailability')

      // 3. 選擇時段
      cy.get('[data-testid="time-slot"]').first().click()

      // 4. 填寫並提交
      cy.get('[data-testid="purpose-input"]').type('快速預約測試')
      cy.get('[data-testid="submit-button"]').click()
      cy.wait('@createReservation')

      // 5. 驗證完成時間
      cy.then(() => {
        const duration = (Date.now() - startTime) / 1000
        expect(duration).to.be.lessThan(30)
      })
    })

    it('應該顯示 Loading 狀態', () => {
      // 搜尋時應顯示 loading
      cy.get('[data-testid="search-button"]').click()
      cy.get('[data-testid="loading-indicator"]').should('be.visible')
      
      cy.wait('@getRooms')
      cy.get('[data-testid="loading-indicator"]').should('not.exist')
    })
  })
})
