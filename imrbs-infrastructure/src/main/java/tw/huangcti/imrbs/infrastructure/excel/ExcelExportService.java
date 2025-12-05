package tw.huangcti.imrbs.infrastructure.excel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.application.usecase.GenerateUsageReportUseCase;
import tw.huangcti.imrbs.domain.service.PopularTimeSlotsService;
import tw.huangcti.imrbs.domain.service.UsageStatisticsService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel 匯出服務
 * 職責: 將使用報告匯出為 Excel 格式
 * 
 * 使用 Apache POI 庫生成 .xlsx 檔案
 * 包含多個工作表: 摘要、會議室統計、時段分析
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 將使用報告匯出為 Excel
     * 
     * @param report 使用報告
     * @return Excel 檔案資源
     * @throws IOException 如果匯出失敗
     */
    public Resource exportToExcel(GenerateUsageReportUseCase.UsageReport report) throws IOException {
        log.info("開始匯出 Excel 報告: {} 至 {}", report.getStartDate(), report.getEndDate());

        try (Workbook workbook = new XSSFWorkbook()) {
            // 建立樣式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // 建立工作表
            createSummarySheet(workbook, report, titleStyle, headerStyle, numberStyle, percentStyle);
            createRoomStatsSheet(workbook, report, titleStyle, headerStyle, numberStyle, percentStyle);
            createTimeSlotAnalysisSheet(workbook, report, titleStyle, headerStyle, numberStyle);
            createDayAnalysisSheet(workbook, report, titleStyle, headerStyle, numberStyle);

            // 寫入位元組陣列
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("Excel 報告匯出完成");
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    /**
     * 建立摘要工作表
     */
    private void createSummarySheet(Workbook workbook, GenerateUsageReportUseCase.UsageReport report,
                                    CellStyle titleStyle, CellStyle headerStyle,
                                    CellStyle numberStyle, CellStyle percentStyle) {
        Sheet sheet = workbook.createSheet("摘要");

        int rowNum = 0;

        // 標題
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("會議室使用率報告");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        // 空行
        rowNum++;

        // 報告期間
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("報告期間:");
        periodRow.createCell(1).setCellValue(
                report.getStartDate().format(DATE_FORMATTER) + " 至 " + 
                report.getEndDate().format(DATE_FORMATTER));

        // 空行
        rowNum++;

        // 整體統計標題
        Row statsHeaderRow = sheet.createRow(rowNum++);
        statsHeaderRow.createCell(0).setCellValue("項目");
        statsHeaderRow.getCell(0).setCellStyle(headerStyle);
        statsHeaderRow.createCell(1).setCellValue("數值");
        statsHeaderRow.getCell(1).setCellStyle(headerStyle);

        // 整體使用率
        Row usageRateRow = sheet.createRow(rowNum++);
        usageRateRow.createCell(0).setCellValue("整體使用率");
        Cell usageRateCell = usageRateRow.createCell(1);
        usageRateCell.setCellValue(report.getOverallUsageRate() / 100.0);
        usageRateCell.setCellStyle(percentStyle);

        // 總使用時數
        Row usageHoursRow = sheet.createRow(rowNum++);
        usageHoursRow.createCell(0).setCellValue("總使用時數");
        Cell usageHoursCell = usageHoursRow.createCell(1);
        usageHoursCell.setCellValue(report.getTotalUsageHours());
        usageHoursCell.setCellStyle(numberStyle);

        // 總預約次數
        Row reservationsRow = sheet.createRow(rowNum++);
        reservationsRow.createCell(0).setCellValue("總預約次數");
        Cell reservationsCell = reservationsRow.createCell(1);
        reservationsCell.setCellValue(report.getTotalReservations());

        // 空行
        rowNum++;

        // 尖峰時段
        Row peakRow = sheet.createRow(rowNum++);
        peakRow.createCell(0).setCellValue("尖峰時段:");
        peakRow.createCell(1).setCellValue(String.join(", ", report.getPeakHours()));

        // 離峰時段
        Row offPeakRow = sheet.createRow(rowNum++);
        offPeakRow.createCell(0).setCellValue("離峰時段:");
        offPeakRow.createCell(1).setCellValue(String.join(", ", report.getOffPeakHours()));

        // 自動調整欄寬
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 建立會議室統計工作表
     */
    private void createRoomStatsSheet(Workbook workbook, GenerateUsageReportUseCase.UsageReport report,
                                      CellStyle titleStyle, CellStyle headerStyle,
                                      CellStyle numberStyle, CellStyle percentStyle) {
        Sheet sheet = workbook.createSheet("會議室統計");

        int rowNum = 0;

        // 標題
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("各會議室使用統計");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 空行
        rowNum++;

        // 表頭
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"會議室名稱", "位置", "使用率", "使用時數", "預約次數"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 資料列
        List<UsageStatisticsService.RoomUsageStats> roomStats = report.getRoomStats();
        if (roomStats != null) {
            for (UsageStatisticsService.RoomUsageStats stats : roomStats) {
                Row dataRow = sheet.createRow(rowNum++);
                
                dataRow.createCell(0).setCellValue(stats.getRoomName());
                dataRow.createCell(1).setCellValue(stats.getLocation() != null ? stats.getLocation() : "-");
                
                Cell usageRateCell = dataRow.createCell(2);
                usageRateCell.setCellValue(stats.getUsageRate() / 100.0);
                usageRateCell.setCellStyle(percentStyle);
                
                Cell usageHoursCell = dataRow.createCell(3);
                usageHoursCell.setCellValue(stats.getUsageHours());
                usageHoursCell.setCellStyle(numberStyle);
                
                dataRow.createCell(4).setCellValue(stats.getReservationCount());
            }
        }

        // 自動調整欄寬
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 建立時段分析工作表
     */
    private void createTimeSlotAnalysisSheet(Workbook workbook, GenerateUsageReportUseCase.UsageReport report,
                                             CellStyle titleStyle, CellStyle headerStyle,
                                             CellStyle numberStyle) {
        Sheet sheet = workbook.createSheet("時段分析");

        int rowNum = 0;

        // 標題
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("每小時預約分析");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        // 空行
        rowNum++;

        // 表頭
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"時段", "預約次數", "是否尖峰"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 資料列
        List<PopularTimeSlotsService.TimeSlotStats> hourlyStats = report.getHourlyStats();
        if (hourlyStats != null) {
            for (PopularTimeSlotsService.TimeSlotStats stats : hourlyStats) {
                Row dataRow = sheet.createRow(rowNum++);
                
                dataRow.createCell(0).setCellValue(stats.getTimeSlot());
                dataRow.createCell(1).setCellValue(stats.getBookingCount());
                dataRow.createCell(2).setCellValue(stats.isPeak() ? "是" : "否");
            }
        }

        // 自動調整欄寬
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 建立星期分析工作表
     */
    private void createDayAnalysisSheet(Workbook workbook, GenerateUsageReportUseCase.UsageReport report,
                                        CellStyle titleStyle, CellStyle headerStyle,
                                        CellStyle numberStyle) {
        Sheet sheet = workbook.createSheet("星期分析");

        int rowNum = 0;

        // 標題
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("每週預約分析");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        // 空行
        rowNum++;

        // 表頭
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"星期", "預約次數"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 資料列
        List<PopularTimeSlotsService.DayStats> dayStats = report.getDayStats();
        if (dayStats != null) {
            for (PopularTimeSlotsService.DayStats stats : dayStats) {
                Row dataRow = sheet.createRow(rowNum++);
                
                dataRow.createCell(0).setCellValue(stats.getDayName());
                dataRow.createCell(1).setCellValue(stats.getBookingCount());
            }
        }

        // 自動調整欄寬
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // 樣式建立方法

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("yyyy/mm/dd"));
        return style;
    }
}
