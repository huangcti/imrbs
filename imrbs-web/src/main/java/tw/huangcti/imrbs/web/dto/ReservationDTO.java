package tw.huangcti.imrbs.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 預約回應 DTO
 */
@Builder
public record ReservationDTO(
        Long id,
        Long roomId,
        String roomName,
        Long userId,
        String userName,
        String meetingTitle,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startTime,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endTime,
        
        String participants,
        String status,
        Boolean isRecurring,
        String recurringRule,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
