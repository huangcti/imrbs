package tw.huangcti.imrbs.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tw.huangcti.imrbs.application.usecase.GenerateUsageReportUseCase;
import tw.huangcti.imrbs.infrastructure.excel.ExcelExportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 報告 API 控制器
 * 提供會議室使用率統計報告相關的 REST API
 * 
 * 權限要求: ADMIN 角色
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "會議室使用率報告 API")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final GenerateUsageReportUseCase generateUsageReportUseCase;
    private final ExcelExportService excelExportService;

    /**
     * 取得使用率報告
     * 
     * @param startDate 開始日期 (YYYY-MM-DD)
     * @param endDate 結束日期 (YYYY-MM-DD)
     * @param roomIds 會議室 ID 列表 (可選)
     * @param periodType 報告類型: daily, weekly, monthly, custom (預設)
     * @return 使用率報告
     */
    @GetMapping("/usage")
    @Operation(summary = "取得使用率報告", description = "根據日期範圍和篩選條件生成會議室使用率報告")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功取得報告",
                    content = @Content(schema = @Schema(implementation = GenerateUsageReportUseCase.UsageReport.class))),
            @ApiResponse(responseCode = "400", description = "無效的請求參數"),
            @ApiResponse(responseCode = "401", description = "未認證"),
            @ApiResponse(responseCode = "403", description = "權限不足")
    })
    public ResponseEntity<GenerateUsageReportUseCase.UsageReport> getUsageReport(
            @Parameter(description = "開始日期 (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "結束日期 (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "會議室 ID 列表 (逗號分隔)")
            @RequestParam(required = false) List<Long> roomIds,
            
            @Parameter(description = "報告類型: daily, weekly, monthly, custom")
            @RequestParam(defaultValue = "custom") String periodType) {
        
        log.info("取得使用率報告: {} 至 {}, 類型: {}, 會議室: {}", 
                startDate, endDate, periodType, roomIds);

        // 驗證日期
        if (endDate.isBefore(startDate)) {
            log.warn("無效的日期範圍: {} 至 {}", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }

        // 根據報告類型調整日期範圍 (如果需要)
        GenerateUsageReportUseCase.UsageReport report = 
                switch (periodType.toLowerCase()) {
                    case "daily" -> generateUsageReportUseCase.generateDailyReport(startDate);
                    case "weekly" -> generateUsageReportUseCase.generateWeeklyReport(startDate);
                    case "monthly" -> generateUsageReportUseCase.generateMonthlyReport(
                            startDate.getYear(), startDate.getMonthValue());
                    default -> generateUsageReportUseCase.generateReport(startDate, endDate, roomIds);
                };

        return ResponseEntity.ok(report);
    }

    /**
     * 取得報告摘要
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 報告摘要
     */
    @GetMapping("/usage/summary")
    @Operation(summary = "取得報告摘要", description = "取得關鍵指標的快速摘要")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功取得摘要",
                    content = @Content(schema = @Schema(implementation = GenerateUsageReportUseCase.ReportSummary.class))),
            @ApiResponse(responseCode = "400", description = "無效的請求參數"),
            @ApiResponse(responseCode = "401", description = "未認證"),
            @ApiResponse(responseCode = "403", description = "權限不足")
    })
    public ResponseEntity<GenerateUsageReportUseCase.ReportSummary> getReportSummary(
            @Parameter(description = "開始日期 (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "結束日期 (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("取得報告摘要: {} 至 {}", startDate, endDate);

        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().build();
        }

        GenerateUsageReportUseCase.ReportSummary summary = 
                generateUsageReportUseCase.generateSummary(startDate, endDate);

        return ResponseEntity.ok(summary);
    }

    /**
     * 匯出 Excel 報告
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @param roomIds 會議室 ID 列表 (可選)
     * @return Excel 檔案
     */
    @PostMapping("/export/excel")
    @Operation(summary = "匯出 Excel 報告", description = "匯出使用率報告為 Excel 檔案")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功匯出 Excel 檔案",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "400", description = "無效的請求參數"),
            @ApiResponse(responseCode = "401", description = "未認證"),
            @ApiResponse(responseCode = "403", description = "權限不足"),
            @ApiResponse(responseCode = "500", description = "匯出失敗")
    })
    public ResponseEntity<Resource> exportExcel(
            @Parameter(description = "開始日期 (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "結束日期 (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "會議室 ID 列表")
            @RequestParam(required = false) List<Long> roomIds) {
        
        log.info("匯出 Excel 報告: {} 至 {}, 會議室: {}", startDate, endDate, roomIds);

        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 生成報告
            GenerateUsageReportUseCase.UsageReport report = 
                    generateUsageReportUseCase.generateReport(startDate, endDate, roomIds);

            // 匯出為 Excel
            Resource excelResource = excelExportService.exportToExcel(report);

            // 生成檔名
            String filename = String.format("usage-report_%s_%s.xlsx",
                    startDate.format(DateTimeFormatter.BASIC_ISO_DATE),
                    endDate.format(DateTimeFormatter.BASIC_ISO_DATE));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelResource);

        } catch (Exception e) {
            log.error("匯出 Excel 失敗", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 取得今日報告
     */
    @GetMapping("/usage/today")
    @Operation(summary = "取得今日報告", description = "取得今天的使用率報告")
    public ResponseEntity<GenerateUsageReportUseCase.UsageReport> getTodayReport() {
        log.info("取得今日報告");
        GenerateUsageReportUseCase.UsageReport report = 
                generateUsageReportUseCase.generateDailyReport();
        return ResponseEntity.ok(report);
    }

    /**
     * 取得本週報告
     */
    @GetMapping("/usage/this-week")
    @Operation(summary = "取得本週報告", description = "取得本週的使用率報告")
    public ResponseEntity<GenerateUsageReportUseCase.UsageReport> getThisWeekReport() {
        log.info("取得本週報告");
        GenerateUsageReportUseCase.UsageReport report = 
                generateUsageReportUseCase.generateWeeklyReport();
        return ResponseEntity.ok(report);
    }

    /**
     * 取得本月報告
     */
    @GetMapping("/usage/this-month")
    @Operation(summary = "取得本月報告", description = "取得本月的使用率報告")
    public ResponseEntity<GenerateUsageReportUseCase.UsageReport> getThisMonthReport() {
        log.info("取得本月報告");
        GenerateUsageReportUseCase.UsageReport report = 
                generateUsageReportUseCase.generateMonthlyReport();
        return ResponseEntity.ok(report);
    }
}
