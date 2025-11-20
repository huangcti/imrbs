package tw.huangcti.imrbs.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 創建預約請求 DTO
 * Record 類型會自動生成構造函數
 */
public record CreateReservationRequest(
        @NotNull(message = "會議室 ID 不能為空") Long roomId,
        @NotNull(message = "使用者 ID 不能為空") Long userId,
        @NotBlank(message = "會議主題不能為空") String meetingTitle,
        @NotNull(message = "開始時間不能為空") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
        @NotNull(message = "結束時間不能為空") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime,
        String participants,
        String recurringRule
) {}
