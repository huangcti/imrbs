/**
 * T079 [P] [US2] 撰寫前端我的預約頁面 E2E 測試
 * 
 * 測試情境: 員工登入 → 查看我的預約 → 選擇預約 → 修改或取消 → 系統驗證規則 → 發送通知
 * 
 * 驗收標準:
 * 1. 使用者可以查看個人預約清單 (進行中、已完成、已取消)
 * 2. 使用者可以查看預約詳情
 * 3. 使用者可以修改預約 (時間、參與者)
 * 4. 使用者可以取消預約 (需驗證 24 小時規則)
 * 5. 系統顯示操作成功訊息
 */

describe('US2: 員工修改與取消預約', () => {
  beforeEach(() => {
    // 設置 mock login
    cy.login()
    
    // Mock API responses
    cy.mockReservationAPI()
    
    // 訪問我的預約頁面
    cy.visit('/my-reservations')
  })

  describe('預約清單功能', () => {
    it('應該顯示個人預約清單', () => {
      // 等待 API 回應
      cy.wait('@getMyReservations')

      // 驗證預約清單顯示
      cy.get('[data-testid="reservation-list"]').should('exist')
      cy.get('[data-testid="reservation-item"]').should('have.length.at.least', 1)
    })

    it('應該支援按狀態篩選預約', () => {
      // 等待初始載入
      cy.wait('@getMyReservations')

      // 點擊進行中標籤
      cy.get('[data-testid="filter-upcoming"]').click()
      cy.get('[data-testid="reservation-item"]').each(($el) => {
        cy.wrap($el).find('[data-testid="reservation-status"]')
          .should('contain', '進行中')
      })

      // 點擊已完成標籤
      cy.get('[data-testid="filter-completed"]').click()
      cy.get('[data-testid="reservation-item"]').each(($el) => {
        cy.wrap($el).find('[data-testid="reservation-status"]')
          .should('contain', '已完成')
      })

      // 點擊已取消標籤
      cy.get('[data-testid="filter-cancelled"]').click()
      cy.get('[data-testid="reservation-item"]').each(($el) => {
        cy.wrap($el).find('[data-testid="reservation-status"]')
          .should('contain', '已取消')
      })
    })

    it('應該顯示預約的基本資訊', () => {
      cy.wait('@getMyReservations')

      // 驗證第一筆預約資訊
      cy.get('[data-testid="reservation-item"]').first().within(() => {
        cy.get('[data-testid="room-name"]').should('exist')
        cy.get('[data-testid="reservation-date"]').should('exist')
        cy.get('[data-testid="reservation-time"]').should('exist')
        cy.get('[data-testid="reservation-status"]').should('exist')
      })
    })
  })

  describe('預約詳情功能', () => {
    it('應該顯示預約詳細資訊', () => {
      cy.wait('@getMyReservations')

      // 點擊第一筆預約
      cy.get('[data-testid="reservation-item"]').first().click()

      // 等待詳情載入
      cy.wait('@getReservationDetail')

      // 驗證詳情頁面顯示
      cy.get('[data-testid="reservation-detail"]').should('exist')
      cy.get('[data-testid="room-name"]').should('exist')
      cy.get('[data-testid="reservation-date"]').should('exist')
      cy.get('[data-testid="start-time"]').should('exist')
      cy.get('[data-testid="end-time"]').should('exist')
      cy.get('[data-testid="purpose"]').should('exist')
      cy.get('[data-testid="participants"]').should('exist')
    })

    it('應該顯示修改和取消按鈕', () => {
      cy.wait('@getMyReservations')
      cy.get('[data-testid="reservation-item"]').first().click()
      cy.wait('@getReservationDetail')

      // 驗證操作按鈕存在
      cy.get('[data-testid="edit-button"]').should('exist')
      cy.get('[data-testid="cancel-button"]').should('exist')
    })
  })

  describe('修改預約功能', () => {
    beforeEach(() => {
      cy.wait('@getMyReservations')
      cy.get('[data-testid="reservation-item"]').first().click()
      cy.wait('@getReservationDetail')
      cy.get('[data-testid="edit-button"]').click()
    })

    it('應該顯示預約編輯表單', () => {
      // 驗證編輯表單元素存在
      cy.get('[data-testid="edit-reservation-form"]').should('exist')
      cy.get('[data-testid="date-input"]').should('exist')
      cy.get('[data-testid="start-time-input"]').should('exist')
      cy.get('[data-testid="end-time-input"]').should('exist')
      cy.get('[data-testid="purpose-input"]').should('exist')
      cy.get('[data-testid="participants-input"]').should('exist')
      cy.get('[data-testid="save-button"]').should('exist')
      cy.get('[data-testid="cancel-edit-button"]').should('exist')
    })

    it('應該成功修改預約時間', () => {
      // 修改預約時間
      cy.get('[data-testid="start-time-input"]').clear().type('10:00')
      cy.get('[data-testid="end-time-input"]').clear().type('11:00')

      // 提交修改
      cy.get('[data-testid="save-button"]').click()

      // 等待 API 回應
      cy.wait('@updateReservation')

      // 驗證成功訊息
      cy.get('[data-testid="success-message"]')
        .should('be.visible')
        .and('contain', '預約已更新')
    })

    it('應該成功修改參與者', () => {
      // 修改參與者
      cy.get('[data-testid="participants-input"]').clear().type('張三, 李四, 王五')

      // 提交修改
      cy.get('[data-testid="save-button"]').click()

      // 等待 API 回應
      cy.wait('@updateReservation')

      // 驗證成功訊息
      cy.get('[data-testid="success-message"]')
        .should('be.visible')
        .and('contain', '預約已更新')
    })

    it('應該驗證時間衝突', () => {
      // 修改為衝突的時間
      cy.get('[data-testid="start-time-input"]').clear().type('14:00')
      cy.get('[data-testid="end-time-input"]').clear().type('15:00')

      // 提交修改
      cy.get('[data-testid="save-button"]').click()

      // 等待 API 回應 (模擬衝突錯誤)
      cy.wait('@updateReservation')

      // 驗證錯誤訊息
      cy.get('[data-testid="error-message"]')
        .should('be.visible')
        .and('contain', '時間衝突')
    })

    it('應該可以取消編輯', () => {
      // 修改資料
      cy.get('[data-testid="start-time-input"]').clear().type('10:00')

      // 取消編輯
      cy.get('[data-testid="cancel-edit-button"]').click()

      // 驗證返回詳情頁面
      cy.get('[data-testid="reservation-detail"]').should('exist')
      cy.get('[data-testid="edit-reservation-form"]').should('not.exist')
    })
  })

  describe('取消預約功能', () => {
    it('應該顯示取消確認對話框', () => {
      cy.wait('@getMyReservations')
      cy.get('[data-testid="reservation-item"]').first().click()
      cy.wait('@getReservationDetail')

      // 點擊取消按鈕
      cy.get('[data-testid="cancel-button"]').click()

      // 驗證確認對話框顯示
      cy.get('[data-testid="cancel-confirmation-modal"]').should('be.visible')
      cy.get('[data-testid="confirm-cancel-button"]').should('exist')
      cy.get('[data-testid="cancel-modal-close-button"]').should('exist')
    })

    it('應該成功取消預約 (符合 24 小時規則)', () => {
      cy.wait('@getMyReservations')
      
      // 選擇一個距離開始時間超過 24 小時的預約
      cy.get('[data-testid="reservation-item"]').eq(0).click()
      cy.wait('@getReservationDetail')

      // 點擊取消按鈕
      cy.get('[data-testid="cancel-button"]').click()

      // 確認取消
      cy.get('[data-testid="confirm-cancel-button"]').click()

      // 等待 API 回應
      cy.wait('@cancelReservation')

      // 驗證成功訊息
      cy.get('[data-testid="success-message"]')
        .should('be.visible')
        .and('contain', '預約已取消')
    })

    it('應該阻止在 24 小時內取消預約', () => {
      cy.wait('@getMyReservations')
      
      // Mock 一個距離開始時間不足 24 小時的預約
      cy.intercept('GET', '/api/reservations/*', {
        statusCode: 200,
        body: {
          id: 1,
          roomName: '會議室 A',
          startTime: new Date(Date.now() + 12 * 60 * 60 * 1000).toISOString(), // 12 小時後
          endTime: new Date(Date.now() + 13 * 60 * 60 * 1000).toISOString(),
          status: 'CONFIRMED'
        }
      }).as('getRecentReservation')

      cy.get('[data-testid="reservation-item"]').eq(1).click()
      cy.wait('@getRecentReservation')

      // 點擊取消按鈕
      cy.get('[data-testid="cancel-button"]').click()

      // 確認取消
      cy.get('[data-testid="confirm-cancel-button"]').click()

      // 等待 API 回應 (應該返回 400 錯誤)
      cy.wait('@cancelReservation')

      // 驗證錯誤訊息
      cy.get('[data-testid="error-message"]')
        .should('be.visible')
        .and('contain', '24 小時')
    })

    it('應該可以關閉取消確認對話框', () => {
      cy.wait('@getMyReservations')
      cy.get('[data-testid="reservation-item"]').first().click()
      cy.wait('@getReservationDetail')

      // 點擊取消按鈕
      cy.get('[data-testid="cancel-button"]').click()

      // 關閉對話框
      cy.get('[data-testid="cancel-modal-close-button"]').click()

      // 驗證對話框已關閉
      cy.get('[data-testid="cancel-confirmation-modal"]').should('not.exist')
    })
  })

  describe('預約狀態變更', () => {
    it('應該更新預約狀態為已取消', () => {
      cy.wait('@getMyReservations')
      cy.get('[data-testid="reservation-item"]').first().click()
      cy.wait('@getReservationDetail')

      // 取消預約
      cy.get('[data-testid="cancel-button"]').click()
      cy.get('[data-testid="confirm-cancel-button"]').click()
      cy.wait('@cancelReservation')

      // 返回列表
      cy.visit('/my-reservations')
      cy.wait('@getMyReservations')

      // 切換到已取消標籤
      cy.get('[data-testid="filter-cancelled"]').click()

      // 驗證預約出現在已取消列表中
      cy.get('[data-testid="reservation-item"]').should('have.length.at.least', 1)
    })
  })

  describe('錯誤處理', () => {
    it('應該處理載入失敗', () => {
      // Mock API 錯誤
      cy.intercept('GET', '/api/reservations/my', {
        statusCode: 500,
        body: { message: '伺服器錯誤' }
      }).as('getMyReservationsError')

      cy.visit('/my-reservations')
      cy.wait('@getMyReservationsError')

      // 驗證錯誤訊息顯示
      cy.get('[data-testid="error-message"]')
        .should('be.visible')
        .and('contain', '載入失敗')
    })

    it('應該處理更新失敗', () => {
      cy.wait('@getMyReservations')
      cy.get('[data-testid="reservation-item"]').first().click()
      cy.wait('@getReservationDetail')
      cy.get('[data-testid="edit-button"]').click()

      // Mock API 錯誤
      cy.intercept('PUT', '/api/reservations/*', {
        statusCode: 500,
        body: { message: '更新失敗' }
      }).as('updateReservationError')

      // 提交修改
      cy.get('[data-testid="save-button"]').click()
      cy.wait('@updateReservationError')

      // 驗證錯誤訊息
      cy.get('[data-testid="error-message"]')
        .should('be.visible')
        .and('contain', '更新失敗')
    })
  })

  describe('響應式設計', () => {
    it('應該在行動裝置上正常顯示', () => {
      cy.viewport('iphone-x')
      cy.wait('@getMyReservations')

      // 驗證列表在行動裝置上顯示
      cy.get('[data-testid="reservation-list"]').should('be.visible')
      cy.get('[data-testid="reservation-item"]').should('be.visible')
    })

    it('應該在平板上正常顯示', () => {
      cy.viewport('ipad-2')
      cy.wait('@getMyReservations')

      // 驗證列表在平板上顯示
      cy.get('[data-testid="reservation-list"]').should('be.visible')
      cy.get('[data-testid="reservation-item"]').should('be.visible')
    })
  })
})
