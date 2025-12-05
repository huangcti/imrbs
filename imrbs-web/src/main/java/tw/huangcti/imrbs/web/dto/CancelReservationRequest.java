package tw.huangcti.imrbs.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * CancelReservationRequest - 取消預約請求 DTO
 * 
 * @param cancellationReason 取消原因
 */
public record CancelReservationRequest(
        @NotBlank(message = "取消原因不能為空")
        String cancellationReason
) {
}
