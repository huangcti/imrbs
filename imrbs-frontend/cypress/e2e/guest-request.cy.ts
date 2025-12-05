/**
 * 訪客預約申請 E2E 測試
 * 
 * 測試場景:
 * 1. 訪客提交預約申請流程
 * 2. 管理員審核訪客申請流程
 * 3. 訪客查詢申請狀態
 */

describe('訪客預約申請流程', () => {
  beforeEach(() => {
    // 清除狀態
    cy.clearLocalStorage()
    cy.clearCookies()
  })

  describe('訪客提交預約申請', () => {
    beforeEach(() => {
      cy.visit('/guest/request')
    })

    it('應該顯示訪客預約表單', () => {
      cy.get('[data-testid="guest-request-form"]').should('be.visible')
      cy.get('[data-testid="guest-name-input"]').should('be.visible')
      cy.get('[data-testid="guest-email-input"]').should('be.visible')
      cy.get('[data-testid="guest-phone-input"]').should('be.visible')
      cy.get('[data-testid="guest-company-input"]').should('be.visible')
      cy.get('[data-testid="room-select"]').should('be.visible')
      cy.get('[data-testid="meeting-title-input"]').should('be.visible')
      cy.get('[data-testid="start-time-input"]').should('be.visible')
      cy.get('[data-testid="end-time-input"]').should('be.visible')
      cy.get('[data-testid="attendee-count-input"]').should('be.visible')
      cy.get('[data-testid="purpose-input"]').should('be.visible')
    })

    it('應該驗證必填欄位', () => {
      // 直接點擊提交
      cy.get('[data-testid="submit-button"]').click()

      // 驗證錯誤訊息
      cy.get('[data-testid="guest-name-error"]').should('be.visible')
      cy.get('[data-testid="guest-email-error"]').should('be.visible')
      cy.get('[data-testid="meeting-title-error"]').should('be.visible')
    })

    it('應該驗證 Email 格式', () => {
      cy.get('[data-testid="guest-email-input"]').type('invalid-email')
      cy.get('[data-testid="submit-button"]').click()
      
      cy.get('[data-testid="guest-email-error"]')
        .should('be.visible')
        .and('contain', '請輸入有效的電子郵件')
    })

    it('應該成功提交訪客預約申請', () => {
      // 填寫表單
      cy.get('[data-testid="guest-name-input"]').type('訪客張三')
      cy.get('[data-testid="guest-email-input"]').type('guest@external.com')
      cy.get('[data-testid="guest-phone-input"]').type('0912-345-678')
      cy.get('[data-testid="guest-company-input"]').type('外部公司 A')
      
      // 選擇會議室
      cy.get('[data-testid="room-select"]').click()
      cy.get('[data-testid="room-option-1"]').click()
      
      // 填寫會議資訊
      cy.get('[data-testid="meeting-title-input"]').type('客戶會議')
      
      // 選擇時間
      const tomorrow = new Date()
      tomorrow.setDate(tomorrow.getDate() + 1)
      const dateStr = tomorrow.toISOString().split('T')[0]
      
      cy.get('[data-testid="date-input"]').type(dateStr)
      cy.get('[data-testid="start-time-input"]').type('10:00')
      cy.get('[data-testid="end-time-input"]').type('12:00')
      
      cy.get('[data-testid="attendee-count-input"]').clear().type('5')
      cy.get('[data-testid="purpose-input"]').type('業務洽談')

      // Mock API 回應
      cy.intercept('POST', '/api/guest/requests', {
        statusCode: 201,
        body: {
          id: 1,
          guestName: '訪客張三',
          guestEmail: 'guest@external.com',
          status: 'PENDING',
          trackingNumber: 'GR-20240315-001'
        }
      }).as('createGuestRequest')

      // 提交
      cy.get('[data-testid="submit-button"]').click()

      // 驗證 API 呼叫
      cy.wait('@createGuestRequest')

      // 驗證成功訊息
      cy.get('[data-testid="success-message"]')
        .should('be.visible')
        .and('contain', '申請已提交成功')
      
      // 應該顯示追蹤號碼
      cy.get('[data-testid="tracking-number"]')
        .should('be.visible')
        .and('contain', 'GR-20240315-001')
    })

    it('時間衝突時應該顯示錯誤', () => {
      // 填寫表單
      cy.get('[data-testid="guest-name-input"]').type('訪客張三')
      cy.get('[data-testid="guest-email-input"]').type('guest@external.com')
      cy.get('[data-testid="room-select"]').click()
      cy.get('[data-testid="room-option-1"]').click()
      cy.get('[data-testid="meeting-title-input"]').type('客戶會議')
      cy.get('[data-testid="start-time-input"]').type('10:00')
      cy.get('[data-testid="end-time-input"]').type('12:00')

      // Mock API 回應 - 時間衝突
      cy.intercept('POST', '/api/guest/requests', {
        statusCode: 409,
        body: {
          message: '該時段已有預約'
        }
      }).as('createGuestRequest')

      cy.get('[data-testid="submit-button"]').click()
      cy.wait('@createGuestRequest')

      cy.get('[data-testid="error-message"]')
        .should('be.visible')
        .and('contain', '該時段已有預約')
    })
  })

  describe('訪客查詢申請狀態', () => {
    it('應該顯示申請狀態查詢表單', () => {
      cy.visit('/guest/status')
      
      cy.get('[data-testid="status-query-form"]').should('be.visible')
      cy.get('[data-testid="tracking-number-input"]').should('be.visible')
      cy.get('[data-testid="email-input"]').should('be.visible')
    })

    it('應該成功查詢申請狀態', () => {
      cy.visit('/guest/status')
      
      cy.intercept('GET', '/api/guest/requests/*', {
        statusCode: 200,
        body: {
          id: 1,
          guestName: '訪客張三',
          guestEmail: 'guest@external.com',
          status: 'PENDING',
          meetingTitle: '客戶會議',
          requestedStartTime: '2024-03-15T10:00:00',
          requestedEndTime: '2024-03-15T12:00:00',
          createdAt: '2024-03-14T15:30:00'
        }
      }).as('getRequestStatus')

      cy.get('[data-testid="tracking-number-input"]').type('GR-20240315-001')
      cy.get('[data-testid="email-input"]').type('guest@external.com')
      cy.get('[data-testid="query-button"]').click()

      cy.wait('@getRequestStatus')

      cy.get('[data-testid="status-result"]').should('be.visible')
      cy.get('[data-testid="status-badge"]')
        .should('be.visible')
        .and('contain', '審核中')
    })

    it('Email 不符時應該顯示錯誤', () => {
      cy.visit('/guest/status')
      
      cy.intercept('GET', '/api/guest/requests/*', {
        statusCode: 403,
        body: {
          message: 'Email 驗證失敗'
        }
      }).as('getRequestStatus')

      cy.get('[data-testid="tracking-number-input"]').type('GR-20240315-001')
      cy.get('[data-testid="email-input"]').type('wrong@email.com')
      cy.get('[data-testid="query-button"]').click()

      cy.wait('@getRequestStatus')

      cy.get('[data-testid="error-message"]')
        .should('be.visible')
        .and('contain', 'Email 驗證失敗')
    })
  })

  describe('管理員審核訪客申請', () => {
    beforeEach(() => {
      // 模擬管理員登入
      cy.window().then((win) => {
        win.localStorage.setItem('token', 'mock-admin-token')
        win.localStorage.setItem('user', JSON.stringify({
          id: 1,
          name: '系統管理員',
          email: 'admin@company.com',
          roles: ['ADMIN']
        }))
      })

      cy.intercept('GET', '/api/admin/guest-requests*', {
        statusCode: 200,
        body: [
          {
            id: 1,
            guestName: '訪客張三',
            guestEmail: 'guest@external.com',
            guestCompany: '外部公司 A',
            roomId: 1,
            roomName: '會議室 A',
            meetingTitle: '客戶會議',
            requestedStartTime: '2024-03-15T10:00:00',
            requestedEndTime: '2024-03-15T12:00:00',
            attendeeCount: 5,
            status: 'PENDING',
            createdAt: '2024-03-14T15:30:00'
          },
          {
            id: 2,
            guestName: '訪客李四',
            guestEmail: 'guest2@external.com',
            guestCompany: '外部公司 B',
            roomId: 2,
            roomName: '會議室 B',
            meetingTitle: '產品展示',
            requestedStartTime: '2024-03-16T14:00:00',
            requestedEndTime: '2024-03-16T16:00:00',
            attendeeCount: 10,
            status: 'PENDING',
            createdAt: '2024-03-14T16:00:00'
          }
        ]
      }).as('getGuestRequests')

      cy.visit('/admin/guest-approval')
    })

    it('應該顯示待審核申請清單', () => {
      cy.wait('@getGuestRequests')

      cy.get('[data-testid="guest-request-list"]').should('be.visible')
      cy.get('[data-testid="guest-request-item"]').should('have.length', 2)
      
      cy.get('[data-testid="guest-request-item"]').first()
        .should('contain', '訪客張三')
        .and('contain', '客戶會議')
        .and('contain', '待審核')
    })

    it('應該能篩選申請狀態', () => {
      cy.wait('@getGuestRequests')

      cy.get('[data-testid="status-filter"]').click()
      cy.get('[data-testid="filter-option-approved"]').click()

      cy.intercept('GET', '/api/admin/guest-requests?status=APPROVED', {
        statusCode: 200,
        body: []
      }).as('getApprovedRequests')

      cy.wait('@getApprovedRequests')
      cy.get('[data-testid="empty-state"]').should('be.visible')
    })

    it('應該成功批准訪客申請', () => {
      cy.wait('@getGuestRequests')

      cy.intercept('POST', '/api/admin/guest-requests/1/approve', {
        statusCode: 200,
        body: {
          id: 1,
          guestName: '訪客張三',
          status: 'APPROVED',
          approvedBy: 'admin',
          approvedAt: '2024-03-14T17:00:00'
        }
      }).as('approveRequest')

      // 點擊第一個申請的批准按鈕
      cy.get('[data-testid="guest-request-item"]').first()
        .find('[data-testid="approve-button"]')
        .click()

      // 確認批准對話框
      cy.get('[data-testid="confirm-modal"]').should('be.visible')
      cy.get('[data-testid="confirm-approve-button"]').click()

      cy.wait('@approveRequest')

      // 驗證成功訊息
      cy.get('[data-testid="success-toast"]')
        .should('be.visible')
        .and('contain', '已批准訪客申請')
    })

    it('應該成功拒絕訪客申請並填寫原因', () => {
      cy.wait('@getGuestRequests')

      cy.intercept('POST', '/api/admin/guest-requests/1/reject', {
        statusCode: 200,
        body: {
          id: 1,
          guestName: '訪客張三',
          status: 'REJECTED',
          rejectionReason: '會議室已滿'
        }
      }).as('rejectRequest')

      // 點擊第一個申請的拒絕按鈕
      cy.get('[data-testid="guest-request-item"]').first()
        .find('[data-testid="reject-button"]')
        .click()

      // 填寫拒絕原因
      cy.get('[data-testid="reject-modal"]').should('be.visible')
      cy.get('[data-testid="rejection-reason-input"]').type('會議室已滿')
      cy.get('[data-testid="confirm-reject-button"]').click()

      cy.wait('@rejectRequest')

      // 驗證成功訊息
      cy.get('[data-testid="success-toast"]')
        .should('be.visible')
        .and('contain', '已拒絕訪客申請')
    })

    it('拒絕時必須填寫原因', () => {
      cy.wait('@getGuestRequests')

      // 點擊拒絕按鈕
      cy.get('[data-testid="guest-request-item"]').first()
        .find('[data-testid="reject-button"]')
        .click()

      // 不填寫原因直接點擊確認
      cy.get('[data-testid="reject-modal"]').should('be.visible')
      cy.get('[data-testid="confirm-reject-button"]').click()

      // 驗證錯誤訊息
      cy.get('[data-testid="reason-error"]')
        .should('be.visible')
        .and('contain', '請填寫拒絕原因')
    })

    it('應該顯示申請詳情', () => {
      cy.wait('@getGuestRequests')

      // 點擊查看詳情
      cy.get('[data-testid="guest-request-item"]').first()
        .find('[data-testid="view-detail-button"]')
        .click()

      // 驗證詳情內容
      cy.get('[data-testid="request-detail-modal"]').should('be.visible')
      cy.get('[data-testid="detail-guest-name"]').should('contain', '訪客張三')
      cy.get('[data-testid="detail-guest-email"]').should('contain', 'guest@external.com')
      cy.get('[data-testid="detail-guest-company"]').should('contain', '外部公司 A')
      cy.get('[data-testid="detail-meeting-title"]').should('contain', '客戶會議')
      cy.get('[data-testid="detail-room-name"]').should('contain', '會議室 A')
      cy.get('[data-testid="detail-attendee-count"]').should('contain', '5')
    })
  })

  describe('通知與權限', () => {
    it('未登入用戶無法存取管理員審核頁面', () => {
      cy.visit('/admin/guest-approval')
      
      // 應該被重導向到登入頁面
      cy.url().should('include', '/login')
    })

    it('一般用戶無法存取管理員審核頁面', () => {
      // 模擬一般用戶登入
      cy.window().then((win) => {
        win.localStorage.setItem('token', 'mock-user-token')
        win.localStorage.setItem('user', JSON.stringify({
          id: 2,
          name: '一般員工',
          email: 'user@company.com',
          roles: ['USER']
        }))
      })

      cy.visit('/admin/guest-approval')
      
      // 應該顯示權限不足訊息或被重導向
      cy.get('[data-testid="access-denied"]').should('be.visible')
    })
  })
})
