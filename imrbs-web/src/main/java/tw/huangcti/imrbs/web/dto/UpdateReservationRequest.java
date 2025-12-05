package tw.huangcti.imrbs.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * UpdateReservationRequest - 修改預約請求 DTO
 * 
 * @param meetingTitle 會議標題
 * @param startTime 開始時間
 * @param endTime 結束時間
 * @param participants 參與者 Email 清單 (逗號分隔)
 */
public record UpdateReservationRequest(
        @NotBlank(message = "會議標題不能為空")
        String meetingTitle,
        
        @NotNull(message = "開始時間不能為空")
        LocalDateTime startTime,
        
        @NotNull(message = "結束時間不能為空")
        LocalDateTime endTime,
        
        String participants
) {
}
