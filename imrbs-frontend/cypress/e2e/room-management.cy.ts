/**
 * T113 [P] [US4] 撰寫前端會議室管理頁面 E2E 測試
 * 
 * 測試情境: 管理員登入 → 會議室管理 → 新增會議室 → 上傳照片 → 設定維護 → 員工查詢可見
 * 
 * 驗收標準:
 * 1. 管理員可以查看會議室列表
 * 2. 管理員可以新增會議室
 * 3. 管理員可以編輯會議室資訊
 * 4. 管理員可以刪除會議室
 * 5. 管理員可以上傳會議室照片
 * 6. 管理員可以設定維護時段
 * 7. 員工可以查詢到新增的會議室
 */

describe('US4: 會議室管理功能', () => {
  beforeEach(() => {
    // 設置 mock login with ROOM_ADMIN role
    cy.loginAsAdmin()
    
    // Mock API responses
    cy.mockRoomManagementAPI()
    
    // 訪問會議室管理頁面
    cy.visit('/admin/rooms')
  })

  describe('會議室列表功能', () => {
    it('應該顯示會議室管理列表', () => {
      // 等待 API 回應
      cy.wait('@getRoomsForAdmin')

      // 驗證列表顯示
      cy.get('[data-testid="room-management-list"]').should('exist')
      cy.get('[data-testid="room-management-item"]').should('have.length.at.least', 1)
    })

    it('應該顯示新增會議室按鈕', () => {
      cy.get('[data-testid="add-room-button"]').should('exist').and('be.visible')
    })

    it('應該顯示每個會議室的操作按鈕', () => {
      cy.wait('@getRoomsForAdmin')

      // 驗證每個會議室都有編輯和刪除按鈕
      cy.get('[data-testid="room-management-item"]').first().within(() => {
        cy.get('[data-testid="edit-room-button"]').should('exist')
        cy.get('[data-testid="delete-room-button"]').should('exist')
        cy.get('[data-testid="manage-maintenance-button"]').should('exist')
      })
    })
  })

  describe('新增會議室功能', () => {
    beforeEach(() => {
      cy.get('[data-testid="add-room-button"]').click()
    })

    it('應該顯示新增會議室表單', () => {
      // 驗證表單元素存在
      cy.get('[data-testid="room-form"]').should('exist')
      cy.get('[data-testid="room-name-input"]').should('exist')
      cy.get('[data-testid="building-input"]').should('exist')
      cy.get('[data-testid="floor-input"]').should('exist')
      cy.get('[data-testid="capacity-input"]').should('exist')
      cy.get('[data-testid="equipment-input"]').should('exist')
      cy.get('[data-testid="features-input"]').should('exist')
      cy.get('[data-testid="save-room-button"]').should('exist')
      cy.get('[data-testid="cancel-button"]').should('exist')
    })

    it('應該成功新增會議室', () => {
      // 填寫表單
      cy.get('[data-testid="room-name-input"]').type('測試會議室 Z')
      cy.get('[data-testid="building-input"]').type('總部大樓')
      cy.get('[data-testid="floor-input"]').type('10F')
      cy.get('[data-testid="capacity-input"]').clear().type('25')
      cy.get('[data-testid="location-description-input"]').type('電梯右側')
      
      // 選擇設備
      cy.get('[data-testid="equipment-input"]').type('投影機{enter}白板{enter}視訊設備{enter}')
      
      // 選擇特色
      cy.get('[data-testid="features-input"]').type('視訊會議{enter}無線投影{enter}')

      // 提交表單
      cy.get('[data-testid="save-room-button"]').click()

      // 等待 API 回應
      cy.wait('@createRoom')

      // 驗證成功訊息
      cy.get('[data-testid="success-message"]')
        .should('be.visible')
        .and('contain', '會議室已新增')

      // 驗證返回列表頁面
      cy.url().should('include', '/admin/rooms')
    })

    it('應該驗證必填欄位', () => {
      // 不填寫任何欄位，直接提交
      cy.get('[data-testid="save-room-button"]').click()

      // 驗證錯誤訊息
      cy.get('[data-testid="room-name-error"]').should('contain', '請輸入會議室名稱')
      cy.get('[data-testid="floor-error"]').should('contain', '請輸入樓層')
      cy.get('[data-testid="capacity-error"]').should('contain', '請輸入容納人數')
    })

    it('應該驗證容量為正整數', () => {
      cy.get('[data-testid="capacity-input"]').clear().type('-5')
      cy.get('[data-testid="save-room-button"]').click()

      cy.get('[data-testid="capacity-error"]').should('contain', '容量必須大於 0')
    })

    it('應該可以取消新增', () => {
      // 填寫部分資料
      cy.get('[data-testid="room-name-input"]').type('測試會議室')

      // 取消
      cy.get('[data-testid="cancel-button"]').click()

      // 驗證返回列表頁面
      cy.url().should('include', '/admin/rooms')
      cy.get('[data-testid="room-form"]').should('not.exist')
    })
  })

  describe('編輯會議室功能', () => {
    beforeEach(() => {
      cy.wait('@getRoomsForAdmin')
      cy.get('[data-testid="room-management-item"]').first()
        .find('[data-testid="edit-room-button"]').click()
    })

    it('應該顯示編輯會議室表單並預填資料', () => {
      // 驗證表單顯示
      cy.get('[data-testid="room-form"]').should('exist')

      // 驗證欄位已預填
      cy.get('[data-testid="room-name-input"]').should('have.value', '會議室 A')
      cy.get('[data-testid="building-input"]').should('have.value', '總部大樓')
      cy.get('[data-testid="floor-input"]').should('have.value', '3F')
      cy.get('[data-testid="capacity-input"]').should('have.value', '10')
    })

    it('應該成功更新會議室資訊', () => {
      // 修改資料
      cy.get('[data-testid="room-name-input"]').clear().type('會議室 A (已更新)')
      cy.get('[data-testid="capacity-input"]').clear().type('15')

      // 提交表單
      cy.get('[data-testid="save-room-button"]').click()

      // 等待 API 回應
      cy.wait('@updateRoom')

      // 驗證成功訊息
      cy.get('[data-testid="success-message"]')
        .should('be.visible')
        .and('contain', '會議室已更新')
    })

    it('應該可以取消編輯', () => {
      // 修改資料
      cy.get('[data-testid="room-name-input"]').clear().type('會議室 A (已更新)')

      // 取消
      cy.get('[data-testid="cancel-button"]').click()

      // 驗證返回列表頁面
      cy.url().should('include', '/admin/rooms')
    })
  })

  describe('刪除會議室功能', () => {
    it('應該顯示刪除確認對話框', () => {
      cy.wait('@getRoomsForAdmin')

      // 點擊刪除按鈕
      cy.get('[data-testid="room-management-item"]').first()
        .find('[data-testid="delete-room-button"]').click()

      // 驗證確認對話框顯示
      cy.get('[data-testid="delete-confirmation-modal"]').should('be.visible')
      cy.get('[data-testid="confirm-delete-button"]').should('exist')
      cy.get('[data-testid="cancel-delete-button"]').should('exist')
    })

    it('應該成功刪除會議室', () => {
      cy.wait('@getRoomsForAdmin')

      // 點擊刪除按鈕
      cy.get('[data-testid="room-management-item"]').last()
        .find('[data-testid="delete-room-button"]').click()

      // 確認刪除
      cy.get('[data-testid="confirm-delete-button"]').click()

      // 等待 API 回應
      cy.wait('@deleteRoom')

      // 驗證成功訊息
      cy.get('[data-testid="success-message"]')
        .should('be.visible')
        .and('contain', '會議室已刪除')
    })

    it('應該阻止刪除有現有預約的會議室', () => {
      cy.wait('@getRoomsForAdmin')

      // Mock API 錯誤
      cy.intercept('DELETE', '/api/v1/rooms/*', {
        statusCode: 409,
        body: { message: '會議室有現有預約，無法刪除' }
      }).as('deleteRoomError')

      // 點擊刪除按鈕
      cy.get('[data-testid="room-management-item"]').first()
        .find('[data-testid="delete-room-button"]').click()

      // 確認刪除
      cy.get('[data-testid="confirm-delete-button"]').click()

      // 等待 API 回應
      cy.wait('@deleteRoomError')

      // 驗證錯誤訊息
      cy.get('[data-testid="error-message"]')
        .should('be.visible')
        .and('contain', '無法刪除')
    })

    it('應該可以取消刪除', () => {
      cy.wait('@getRoomsForAdmin')

      // 點擊刪除按鈕
      cy.get('[data-testid="room-management-item"]').first()
        .find('[data-testid="delete-room-button"]').click()

      // 取消刪除
      cy.get('[data-testid="cancel-delete-button"]').click()

      // 驗證對話框已關閉
      cy.get('[data-testid="delete-confirmation-modal"]').should('not.exist')
    })
  })

  describe('上傳照片功能', () => {
    beforeEach(() => {
      cy.wait('@getRoomsForAdmin')
      cy.get('[data-testid="room-management-item"]').first()
        .find('[data-testid="edit-room-button"]').click()
    })

    it('應該顯示照片上傳區域', () => {
      cy.get('[data-testid="photo-upload-area"]').should('exist')
      cy.get('[data-testid="upload-photo-button"]').should('exist')
    })

    it('應該成功上傳照片', () => {
      // 模擬檔案上傳
      const fileName = 'room-photo.jpg'
      cy.get('[data-testid="photo-upload-input"]').attachFile(fileName)

      // 等待上傳完成
      cy.wait('@uploadPhoto')

      // 驗證照片預覽顯示
      cy.get('[data-testid="photo-preview"]').should('be.visible')
      cy.get('[data-testid="success-message"]')
        .should('contain', '照片已上傳')
    })

    it('應該可以刪除已上傳的照片', () => {
      // 假設已有照片
      cy.get('[data-testid="photo-preview"]').should('exist')

      // 點擊刪除按鈕
      cy.get('[data-testid="delete-photo-button"]').first().click()

      // 確認刪除
      cy.get('[data-testid="confirm-delete-photo-button"]').click()

      // 驗證照片已移除
      cy.get('[data-testid="success-message"]')
        .should('contain', '照片已刪除')
    })

    it('應該限制上傳檔案類型', () => {
      // 嘗試上傳非圖片檔案
      cy.get('[data-testid="photo-upload-input"]').attachFile('document.pdf')

      // 驗證錯誤訊息
      cy.get('[data-testid="error-message"]')
        .should('contain', '僅支援圖片格式')
    })
  })

  describe('設定維護時段功能', () => {
    beforeEach(() => {
      cy.wait('@getRoomsForAdmin')
      cy.get('[data-testid="room-management-item"]').first()
        .find('[data-testid="manage-maintenance-button"]').click()
    })

    it('應該顯示維護時段設定表單', () => {
      cy.get('[data-testid="maintenance-form"]').should('exist')
      cy.get('[data-testid="maintenance-start-time-input"]').should('exist')
      cy.get('[data-testid="maintenance-end-time-input"]').should('exist')
      cy.get('[data-testid="maintenance-reason-input"]').should('exist')
      cy.get('[data-testid="save-maintenance-button"]').should('exist')
    })

    it('應該成功新增維護時段', () => {
      // 填寫表單
      cy.get('[data-testid="maintenance-start-time-input"]')
        .type('2025-12-01T09:00')
      cy.get('[data-testid="maintenance-end-time-input"]')
        .type('2025-12-01T17:00')
      cy.get('[data-testid="maintenance-reason-input"]')
        .type('定期設備檢修')
      cy.get('[data-testid="maintenance-description-input"]')
        .type('檢查投影機與視訊設備')

      // 提交表單
      cy.get('[data-testid="save-maintenance-button"]').click()

      // 等待 API 回應
      cy.wait('@createMaintenance')

      // 驗證成功訊息
      cy.get('[data-testid="success-message"]')
        .should('be.visible')
        .and('contain', '維護時段已設定')
    })

    it('應該驗證時間範圍有效性', () => {
      // 填寫無效的時間範圍（結束早於開始）
      cy.get('[data-testid="maintenance-start-time-input"]')
        .type('2025-12-01T17:00')
      cy.get('[data-testid="maintenance-end-time-input"]')
        .type('2025-12-01T09:00')

      // 提交表單
      cy.get('[data-testid="save-maintenance-button"]').click()

      // 驗證錯誤訊息
      cy.get('[data-testid="error-message"]')
        .should('contain', '結束時間必須晚於開始時間')
    })

    it('應該顯示現有維護時段列表', () => {
      cy.get('[data-testid="maintenance-schedule-list"]').should('exist')
      cy.get('[data-testid="maintenance-schedule-item"]')
        .should('have.length.at.least', 0)
    })
  })

  describe('員工查詢新增的會議室', () => {
    it('員工應該可以查詢到新增的會議室', () => {
      // 登出管理員
      cy.logout()

      // 以一般員工登入
      cy.loginAsEmployee()

      // 訪問會議室搜尋頁面
      cy.visit('/rooms')

      // Mock API responses
      cy.mockRoomAPI()
      cy.wait('@getRooms')

      // 驗證可以看到新增的會議室
      cy.get('[data-testid="room-card"]').should('contain', '測試會議室 Z')
    })
  })

  describe('響應式設計', () => {
    it('應該在行動裝置上正常顯示', () => {
      cy.viewport('iphone-x')
      cy.wait('@getRoomsForAdmin')

      // 驗證列表在行動裝置上顯示
      cy.get('[data-testid="room-management-list"]').should('be.visible')
    })

    it('應該在平板上正常顯示', () => {
      cy.viewport('ipad-2')
      cy.wait('@getRoomsForAdmin')

      // 驗證列表在平板上顯示
      cy.get('[data-testid="room-management-list"]').should('be.visible')
    })
  })
})
