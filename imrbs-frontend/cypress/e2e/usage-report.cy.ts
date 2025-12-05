/// <reference types="cypress" />

/**
 * 使用率報告頁面 E2E 測試 (TDD Red Phase)
 * 
 * 頁面: /admin/reports/usage
 * 
 * 測試場景:
 * 1. 頁面載入與權限控制
 * 2. 日期範圍選擇
 * 3. 會議室過濾
 * 4. 報告數據顯示
 * 5. 圖表視覺化
 * 6. Excel 匯出功能
 */

describe('使用率報告頁面 (Usage Report Page)', () => {
  const baseUrl = '/admin/reports/usage';

  // Mock 報告數據
  const mockReportData = {
    startDate: '2025-11-01',
    endDate: '2025-11-30',
    overallUsageRate: 65.5,
    totalUsageHours: 1200.0,
    totalReservations: 150,
    roomUsages: [
      {
        roomId: 1,
        roomName: '會議室 A',
        location: '3F-301',
        usageRate: 70.0,
        usageHours: 140.0,
        reservationCount: 35
      },
      {
        roomId: 2,
        roomName: '會議室 B',
        location: '3F-302',
        usageRate: 61.0,
        usageHours: 122.0,
        reservationCount: 28
      },
      {
        roomId: 3,
        roomName: '會議室 C',
        location: '4F-401',
        usageRate: 55.0,
        usageHours: 110.0,
        reservationCount: 22
      }
    ],
    popularTimeSlots: [
      { hour: 10, usageCount: 45 },
      { hour: 14, usageCount: 38 },
      { hour: 11, usageCount: 35 },
      { hour: 15, usageCount: 30 }
    ],
    dailyTrend: [
      { date: '2025-11-01', usageRate: 55.0, reservationCount: 10 },
      { date: '2025-11-02', usageRate: 65.0, reservationCount: 12 },
      { date: '2025-11-03', usageRate: 70.0, reservationCount: 15 }
    ]
  };

  beforeEach(() => {
    // 攔截 API 請求
    cy.intercept('GET', '/api/v1/admin/reports/usage*', {
      statusCode: 200,
      body: mockReportData
    }).as('getUsageReport');
  });

  describe('權限控制', () => {
    it('應拒絕未登入用戶訪問', () => {
      // Given - 未登入狀態
      cy.clearCookies();

      // When
      cy.visit(baseUrl, { failOnStatusCode: false });

      // Then - 應重導向至登入頁面
      cy.url().should('include', '/login');
    });

    it('應拒絕一般員工訪問', () => {
      // Given - 以 EMPLOYEE 角色登入
      cy.login('employee@example.com', 'password', 'EMPLOYEE');

      // When
      cy.visit(baseUrl, { failOnStatusCode: false });

      // Then - 應顯示權限不足訊息
      cy.contains('權限不足').should('be.visible');
    });

    it('ROOM_ADMIN 應能訪問報告頁面', () => {
      // Given - 以 ROOM_ADMIN 角色登入
      cy.login('admin@example.com', 'password', 'ROOM_ADMIN');

      // When
      cy.visit(baseUrl);

      // Then - 應顯示報告頁面
      cy.wait('@getUsageReport');
      cy.contains('使用率報告').should('be.visible');
    });

    it('SYSTEM_ADMIN 應能訪問報告頁面', () => {
      // Given - 以 SYSTEM_ADMIN 角色登入
      cy.login('sysadmin@example.com', 'password', 'SYSTEM_ADMIN');

      // When
      cy.visit(baseUrl);

      // Then
      cy.wait('@getUsageReport');
      cy.contains('使用率報告').should('be.visible');
    });
  });

  describe('頁面載入', () => {
    beforeEach(() => {
      cy.login('admin@example.com', 'password', 'ROOM_ADMIN');
    });

    it('應顯示載入中狀態', () => {
      // Given - 延遲 API 回應
      cy.intercept('GET', '/api/v1/admin/reports/usage*', {
        delay: 1000,
        statusCode: 200,
        body: mockReportData
      }).as('getUsageReportDelayed');

      // When
      cy.visit(baseUrl);

      // Then - 應顯示載入中指示器
      cy.get('[data-testid="loading-spinner"]').should('be.visible');
      cy.wait('@getUsageReportDelayed');
      cy.get('[data-testid="loading-spinner"]').should('not.exist');
    });

    it('應預設顯示當月報告', () => {
      // Given
      const today = new Date();
      const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
        .toISOString().split('T')[0];

      // When
      cy.visit(baseUrl);
      cy.wait('@getUsageReport');

      // Then - 日期選擇器應顯示當月範圍
      cy.get('[data-testid="start-date-picker"]')
        .should('have.value', firstDayOfMonth);
    });

    it('應處理 API 錯誤', () => {
      // Given - API 返回錯誤
      cy.intercept('GET', '/api/v1/admin/reports/usage*', {
        statusCode: 500,
        body: { error: { code: 'SERVER_ERROR', message: '伺服器錯誤' } }
      }).as('getUsageReportError');

      // When
      cy.visit(baseUrl);
      cy.wait('@getUsageReportError');

      // Then - 應顯示錯誤訊息
      cy.contains('載入報告失敗').should('be.visible');
      cy.get('[data-testid="retry-button"]').should('be.visible');
    });
  });

  describe('日期範圍選擇', () => {
    beforeEach(() => {
      cy.login('admin@example.com', 'password', 'ROOM_ADMIN');
      cy.visit(baseUrl);
      cy.wait('@getUsageReport');
    });

    it('應能選擇自訂日期範圍', () => {
      // Given
      cy.intercept('GET', '/api/v1/admin/reports/usage*', (req) => {
        expect(req.query.start_date).to.equal('2025-10-01');
        expect(req.query.end_date).to.equal('2025-10-31');
        req.reply(mockReportData);
      }).as('getCustomDateReport');

      // When - 選擇日期範圍
      cy.get('[data-testid="start-date-picker"]').clear().type('2025-10-01');
      cy.get('[data-testid="end-date-picker"]').clear().type('2025-10-31');
      cy.get('[data-testid="apply-filter-button"]').click();

      // Then
      cy.wait('@getCustomDateReport');
    });

    it('應提供快捷日期選項', () => {
      // Given
      cy.get('[data-testid="date-preset-dropdown"]').click();

      // Then - 應顯示快捷選項
      cy.contains('今日').should('be.visible');
      cy.contains('本週').should('be.visible');
      cy.contains('本月').should('be.visible');
      cy.contains('上月').should('be.visible');
      cy.contains('最近 7 天').should('be.visible');
      cy.contains('最近 30 天').should('be.visible');
    });

    it('選擇「本月」應自動填入正確日期', () => {
      // Given
      const today = new Date();
      const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
        .toISOString().split('T')[0];
      const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0)
        .toISOString().split('T')[0];

      // When
      cy.get('[data-testid="date-preset-dropdown"]').click();
      cy.contains('本月').click();

      // Then
      cy.get('[data-testid="start-date-picker"]')
        .should('have.value', firstDayOfMonth);
      cy.get('[data-testid="end-date-picker"]')
        .should('have.value', lastDayOfMonth);
    });

    it('應驗證日期範圍 (結束日期不能早於開始日期)', () => {
      // When - 輸入無效日期範圍
      cy.get('[data-testid="start-date-picker"]').clear().type('2025-11-30');
      cy.get('[data-testid="end-date-picker"]').clear().type('2025-11-01');
      cy.get('[data-testid="apply-filter-button"]').click();

      // Then - 應顯示錯誤訊息
      cy.contains('結束日期不能早於開始日期').should('be.visible');
    });
  });

  describe('會議室過濾', () => {
    beforeEach(() => {
      cy.login('admin@example.com', 'password', 'ROOM_ADMIN');
      cy.visit(baseUrl);
      cy.wait('@getUsageReport');
    });

    it('應能按會議室過濾報告', () => {
      // Given
      cy.intercept('GET', '/api/v1/admin/reports/usage*', (req) => {
        expect(req.query.room_id).to.include('1');
        req.reply({
          ...mockReportData,
          roomUsages: [mockReportData.roomUsages[0]]
        });
      }).as('getFilteredReport');

      // When - 選擇特定會議室
      cy.get('[data-testid="room-filter-dropdown"]').click();
      cy.get('[data-testid="room-option-1"]').click();
      cy.get('[data-testid="apply-filter-button"]').click();

      // Then
      cy.wait('@getFilteredReport');
      cy.get('[data-testid="room-usage-table"] tbody tr').should('have.length', 1);
    });

    it('應能選擇多個會議室', () => {
      // Given
      cy.intercept('GET', '/api/v1/admin/reports/usage*', (req) => {
        expect(req.query.room_id).to.include('1');
        expect(req.query.room_id).to.include('2');
        req.reply({
          ...mockReportData,
          roomUsages: [mockReportData.roomUsages[0], mockReportData.roomUsages[1]]
        });
      }).as('getMultiRoomReport');

      // When - 選擇多個會議室
      cy.get('[data-testid="room-filter-dropdown"]').click();
      cy.get('[data-testid="room-option-1"]').click();
      cy.get('[data-testid="room-option-2"]').click();
      cy.get('[data-testid="apply-filter-button"]').click();

      // Then
      cy.wait('@getMultiRoomReport');
    });

    it('應能清除會議室過濾', () => {
      // Given - 先選擇會議室
      cy.get('[data-testid="room-filter-dropdown"]').click();
      cy.get('[data-testid="room-option-1"]').click();

      // When - 清除過濾
      cy.get('[data-testid="clear-room-filter"]').click();

      // Then - 應顯示所有會議室標籤
      cy.get('[data-testid="room-filter-dropdown"]')
        .should('contain', '所有會議室');
    });
  });

  describe('報告數據顯示', () => {
    beforeEach(() => {
      cy.login('admin@example.com', 'password', 'ROOM_ADMIN');
      cy.visit(baseUrl);
      cy.wait('@getUsageReport');
    });

    it('應顯示整體統計卡片', () => {
      // Then
      cy.get('[data-testid="overall-usage-card"]')
        .should('contain', '65.5%')
        .and('contain', '整體使用率');

      cy.get('[data-testid="total-hours-card"]')
        .should('contain', '1,200')
        .and('contain', '總使用時數');

      cy.get('[data-testid="total-reservations-card"]')
        .should('contain', '150')
        .and('contain', '總預約次數');
    });

    it('應顯示會議室使用率表格', () => {
      // Then
      cy.get('[data-testid="room-usage-table"]').should('be.visible');
      cy.get('[data-testid="room-usage-table"] thead th')
        .should('contain', '會議室')
        .and('contain', '地點')
        .and('contain', '使用率')
        .and('contain', '使用時數')
        .and('contain', '預約次數');

      // 驗證表格數據
      cy.get('[data-testid="room-usage-table"] tbody tr').should('have.length', 3);
      cy.get('[data-testid="room-usage-table"] tbody tr').first()
        .should('contain', '會議室 A')
        .and('contain', '3F-301')
        .and('contain', '70%')
        .and('contain', '140');
    });

    it('應支援表格排序', () => {
      // When - 點擊「使用率」欄位排序
      cy.get('[data-testid="sort-by-usage-rate"]').click();

      // Then - 應按使用率降序排列
      cy.get('[data-testid="room-usage-table"] tbody tr').first()
        .should('contain', '會議室 A'); // 70% 最高
    });

    it('應顯示熱門時段列表', () => {
      // Then
      cy.get('[data-testid="popular-time-slots"]').should('be.visible');
      cy.get('[data-testid="popular-time-slot-item"]').should('have.length', 4);
      cy.get('[data-testid="popular-time-slot-item"]').first()
        .should('contain', '10:00')
        .and('contain', '45 次');
    });
  });

  describe('圖表視覺化', () => {
    beforeEach(() => {
      cy.login('admin@example.com', 'password', 'ROOM_ADMIN');
      cy.visit(baseUrl);
      cy.wait('@getUsageReport');
    });

    it('應顯示每日使用趨勢圖', () => {
      // Then
      cy.get('[data-testid="daily-trend-chart"]').should('be.visible');
      cy.get('[data-testid="daily-trend-chart"] canvas').should('exist');
    });

    it('應顯示會議室使用率比較圖', () => {
      // Then
      cy.get('[data-testid="room-comparison-chart"]').should('be.visible');
    });

    it('應顯示時段分布圖', () => {
      // Then
      cy.get('[data-testid="time-distribution-chart"]').should('be.visible');
    });

    it('應能切換圖表類型', () => {
      // When
      cy.get('[data-testid="chart-type-toggle"]').click();
      cy.contains('折線圖').click();

      // Then - 圖表應更新
      cy.get('[data-testid="daily-trend-chart"]')
        .should('have.attr', 'data-chart-type', 'line');
    });
  });

  describe('Excel 匯出功能', () => {
    beforeEach(() => {
      cy.login('admin@example.com', 'password', 'ROOM_ADMIN');
      cy.visit(baseUrl);
      cy.wait('@getUsageReport');
    });

    it('應能匯出 Excel 報告', () => {
      // Given
      cy.intercept('GET', '/api/v1/admin/reports/usage*format=excel*', {
        statusCode: 200,
        headers: {
          'Content-Type': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          'Content-Disposition': 'attachment; filename=usage-report-2025-11.xlsx'
        },
        body: new ArrayBuffer(0) // Mock empty Excel file
      }).as('downloadExcel');

      // When
      cy.get('[data-testid="export-excel-button"]').click();

      // Then
      cy.wait('@downloadExcel');
      // 注意: Cypress 無法直接驗證文件下載，但可以驗證 API 被調用
    });

    it('匯出按鈕應顯示載入狀態', () => {
      // Given
      cy.intercept('GET', '/api/v1/admin/reports/usage*format=excel*', {
        delay: 1000,
        statusCode: 200,
        body: new ArrayBuffer(0)
      }).as('downloadExcelDelayed');

      // When
      cy.get('[data-testid="export-excel-button"]').click();

      // Then
      cy.get('[data-testid="export-excel-button"]')
        .should('be.disabled')
        .and('contain', '匯出中');
    });

    it('匯出失敗應顯示錯誤提示', () => {
      // Given
      cy.intercept('GET', '/api/v1/admin/reports/usage*format=excel*', {
        statusCode: 500,
        body: { error: { code: 'EXPORT_FAILED', message: '匯出失敗' } }
      }).as('downloadExcelError');

      // When
      cy.get('[data-testid="export-excel-button"]').click();
      cy.wait('@downloadExcelError');

      // Then
      cy.contains('匯出失敗').should('be.visible');
    });
  });

  describe('響應式設計', () => {
    beforeEach(() => {
      cy.login('admin@example.com', 'password', 'ROOM_ADMIN');
    });

    it('在行動裝置上應正確顯示', () => {
      // Given
      cy.viewport('iphone-x');

      // When
      cy.visit(baseUrl);
      cy.wait('@getUsageReport');

      // Then - 統計卡片應垂直堆疊
      cy.get('[data-testid="stats-cards-container"]')
        .should('have.css', 'flex-direction', 'column');

      // 表格應可橫向滾動
      cy.get('[data-testid="room-usage-table-container"]')
        .should('have.css', 'overflow-x', 'auto');
    });

    it('在平板上應顯示兩欄佈局', () => {
      // Given
      cy.viewport('ipad-2');

      // When
      cy.visit(baseUrl);
      cy.wait('@getUsageReport');

      // Then
      cy.get('[data-testid="stats-cards-container"]')
        .should('have.css', 'display', 'grid');
    });
  });
});
