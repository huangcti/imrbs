package tw.huangcti.imrbs.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 更新預約請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationRequest {

    @NotBlank(message = "會議室 ID 不得為空")
    private String roomId;

    @NotNull(message = "日期不得為空")
    private LocalDate date;

    @NotNull(message = "開始時間不得為空")
    private LocalTime startTime;

    @NotNull(message = "結束時間不得為空")
    private LocalTime endTime;

    @NotBlank(message = "會議主題不得為空")
    private String title;

    @NotBlank(message = "聯絡 email 不得為空")
    @Email(message = "Email 格式不正確")
    private String organizerEmail;

    private List<String> participants;
}
