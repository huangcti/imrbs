package tw.huangcti.imrbs.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tw.huangcti.imrbs.application.usecase.GenerateUsageReportUseCase;
import tw.huangcti.imrbs.web.dto.UsageReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 報告 API 合約測試 (TDD Red Phase)
 * 
 * 測試端點: GET /api/v1/admin/reports/usage
 * 
 * 測試場景:
 * 1. 成功獲取使用率報告 (ROOM_ADMIN 權限)
 * 2. 支援日期範圍過濾
 * 3. 支援會議室 ID 過濾
 * 4. 支援匯出格式 (JSON/Excel)
 * 5. 權限控制 (EMPLOYEE 無法訪問)
 * 6. 參數驗證
 */
@WebMvcTest(ReportController.class)
@DisplayName("ReportController 報告 API 合約測試")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GenerateUsageReportUseCase generateUsageReportUseCase;

    private static final String BASE_URL = "/api/v1/admin/reports/usage";

    @Nested
    @DisplayName("GET /api/v1/admin/reports/usage - 獲取使用率報告")
    class GetUsageReportTests {

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("管理員應能成功獲取使用率報告")
        void shouldReturnUsageReportForAdmin() throws Exception {
            // Given
            UsageReportResponse response = UsageReportResponse.builder()
                    .startDate(LocalDate.of(2025, 11, 1))
                    .endDate(LocalDate.of(2025, 11, 30))
                    .overallUsageRate(65.5)
                    .totalUsageHours(1200.0)
                    .totalReservations(150)
                    .roomUsages(List.of(
                            UsageReportResponse.RoomUsage.builder()
                                    .roomId(1L)
                                    .roomName("會議室 A")
                                    .usageRate(70.0)
                                    .usageHours(140.0)
                                    .reservationCount(35)
                                    .build(),
                            UsageReportResponse.RoomUsage.builder()
                                    .roomId(2L)
                                    .roomName("會議室 B")
                                    .usageRate(61.0)
                                    .usageHours(122.0)
                                    .reservationCount(28)
                                    .build()
                    ))
                    .build();

            when(generateUsageReportUseCase.execute(any())).thenReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.startDate").value("2025-11-01"))
                    .andExpect(jsonPath("$.endDate").value("2025-11-30"))
                    .andExpect(jsonPath("$.overallUsageRate").value(65.5))
                    .andExpect(jsonPath("$.totalUsageHours").value(1200.0))
                    .andExpect(jsonPath("$.totalReservations").value(150))
                    .andExpect(jsonPath("$.roomUsages").isArray())
                    .andExpect(jsonPath("$.roomUsages.length()").value(2))
                    .andExpect(jsonPath("$.roomUsages[0].roomId").value(1))
                    .andExpect(jsonPath("$.roomUsages[0].roomName").value("會議室 A"))
                    .andExpect(jsonPath("$.roomUsages[0].usageRate").value(70.0));
        }

        @Test
        @WithMockUser(roles = "SYSTEM_ADMIN")
        @DisplayName("系統管理員應能獲取使用率報告")
        void shouldReturnUsageReportForSystemAdmin() throws Exception {
            // Given
            UsageReportResponse response = UsageReportResponse.builder()
                    .startDate(LocalDate.of(2025, 11, 1))
                    .endDate(LocalDate.of(2025, 11, 30))
                    .overallUsageRate(50.0)
                    .totalUsageHours(500.0)
                    .totalReservations(50)
                    .roomUsages(List.of())
                    .build();

            when(generateUsageReportUseCase.execute(any())).thenReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.overallUsageRate").value(50.0));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("一般員工應被拒絕訪問")
        void shouldDenyAccessForEmployee() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("未認證請求應返回 401")
        void shouldReturn401ForUnauthenticatedRequest() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("日期範圍過濾")
    class DateRangeFilterTests {

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("應使用預設日期範圍（當月）當未提供參數時")
        void shouldUseDefaultDateRangeWhenNotProvided() throws Exception {
            // Given
            UsageReportResponse response = createEmptyResponse();
            when(generateUsageReportUseCase.execute(any())).thenReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL).with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("應拒絕無效的日期格式")
        void shouldRejectInvalidDateFormat() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "invalid-date")
                            .param("end_date", "2025-11-30")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("應拒絕結束日期早於開始日期")
        void shouldRejectEndDateBeforeStartDate() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-30")
                            .param("end_date", "2025-11-01")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("INVALID_DATE_RANGE"));
        }

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("應限制日期範圍最大為 365 天")
        void shouldRejectDateRangeExceeding365Days() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2024-01-01")
                            .param("end_date", "2025-12-31")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("DATE_RANGE_TOO_LARGE"));
        }
    }

    @Nested
    @DisplayName("會議室過濾")
    class RoomFilterTests {

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("應支援按會議室 ID 過濾")
        void shouldFilterByRoomId() throws Exception {
            // Given
            UsageReportResponse response = UsageReportResponse.builder()
                    .startDate(LocalDate.of(2025, 11, 1))
                    .endDate(LocalDate.of(2025, 11, 30))
                    .overallUsageRate(70.0)
                    .totalUsageHours(140.0)
                    .totalReservations(35)
                    .roomUsages(List.of(
                            UsageReportResponse.RoomUsage.builder()
                                    .roomId(1L)
                                    .roomName("會議室 A")
                                    .usageRate(70.0)
                                    .usageHours(140.0)
                                    .reservationCount(35)
                                    .build()
                    ))
                    .build();

            when(generateUsageReportUseCase.execute(any())).thenReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30")
                            .param("room_id", "1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomUsages.length()").value(1))
                    .andExpect(jsonPath("$.roomUsages[0].roomId").value(1));
        }

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("應支援多個會議室 ID 過濾")
        void shouldFilterByMultipleRoomIds() throws Exception {
            // Given
            UsageReportResponse response = UsageReportResponse.builder()
                    .startDate(LocalDate.of(2025, 11, 1))
                    .endDate(LocalDate.of(2025, 11, 30))
                    .overallUsageRate(65.0)
                    .totalUsageHours(260.0)
                    .totalReservations(60)
                    .roomUsages(List.of(
                            UsageReportResponse.RoomUsage.builder().roomId(1L).build(),
                            UsageReportResponse.RoomUsage.builder().roomId(2L).build()
                    ))
                    .build();

            when(generateUsageReportUseCase.execute(any())).thenReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30")
                            .param("room_id", "1", "2")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomUsages.length()").value(2));
        }
    }

    @Nested
    @DisplayName("匯出格式")
    class ExportFormatTests {

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("預設應返回 JSON 格式")
        void shouldReturnJsonByDefault() throws Exception {
            // Given
            when(generateUsageReportUseCase.execute(any())).thenReturn(createEmptyResponse());

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("應支援 Excel 匯出格式")
        void shouldSupportExcelExport() throws Exception {
            // Given
            when(generateUsageReportUseCase.execute(any())).thenReturn(createEmptyResponse());

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30")
                            .param("format", "excel")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .andExpect(header().string("Content-Disposition", 
                            org.hamcrest.Matchers.containsString("usage-report")));
        }

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("應拒絕不支援的匯出格式")
        void shouldRejectUnsupportedFormat() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30")
                            .param("format", "pdf")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_FORMAT"));
        }
    }

    @Nested
    @DisplayName("響應結構驗證")
    class ResponseStructureTests {

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("響應應包含熱門時段資訊")
        void shouldIncludePopularTimeSlotsInResponse() throws Exception {
            // Given
            UsageReportResponse response = UsageReportResponse.builder()
                    .startDate(LocalDate.of(2025, 11, 1))
                    .endDate(LocalDate.of(2025, 11, 30))
                    .overallUsageRate(65.0)
                    .totalUsageHours(500.0)
                    .totalReservations(100)
                    .roomUsages(List.of())
                    .popularTimeSlots(List.of(
                            UsageReportResponse.TimeSlotUsage.builder()
                                    .hour(10)
                                    .usageCount(45)
                                    .build(),
                            UsageReportResponse.TimeSlotUsage.builder()
                                    .hour(14)
                                    .usageCount(38)
                                    .build()
                    ))
                    .build();

            when(generateUsageReportUseCase.execute(any())).thenReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-30")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.popularTimeSlots").isArray())
                    .andExpect(jsonPath("$.popularTimeSlots[0].hour").value(10))
                    .andExpect(jsonPath("$.popularTimeSlots[0].usageCount").value(45));
        }

        @Test
        @WithMockUser(roles = "ROOM_ADMIN")
        @DisplayName("響應應包含每日使用趨勢")
        void shouldIncludeDailyTrendInResponse() throws Exception {
            // Given
            UsageReportResponse response = UsageReportResponse.builder()
                    .startDate(LocalDate.of(2025, 11, 1))
                    .endDate(LocalDate.of(2025, 11, 3))
                    .overallUsageRate(60.0)
                    .totalUsageHours(180.0)
                    .totalReservations(30)
                    .roomUsages(List.of())
                    .dailyTrend(List.of(
                            UsageReportResponse.DailyUsage.builder()
                                    .date(LocalDate.of(2025, 11, 1))
                                    .usageRate(55.0)
                                    .reservationCount(10)
                                    .build(),
                            UsageReportResponse.DailyUsage.builder()
                                    .date(LocalDate.of(2025, 11, 2))
                                    .usageRate(65.0)
                                    .reservationCount(12)
                                    .build()
                    ))
                    .build();

            when(generateUsageReportUseCase.execute(any())).thenReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                            .param("start_date", "2025-11-01")
                            .param("end_date", "2025-11-03")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dailyTrend").isArray())
                    .andExpect(jsonPath("$.dailyTrend[0].date").value("2025-11-01"))
                    .andExpect(jsonPath("$.dailyTrend[0].usageRate").value(55.0));
        }
    }

    // Helper method
    private UsageReportResponse createEmptyResponse() {
        return UsageReportResponse.builder()
                .startDate(LocalDate.now().withDayOfMonth(1))
                .endDate(LocalDate.now())
                .overallUsageRate(0.0)
                .totalUsageHours(0.0)
                .totalReservations(0)
                .roomUsages(List.of())
                .build();
    }
}
