package tw.huangcti.imrbs.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 維護時段 DTO
 * 用於 API 回應與請求
 */
@Builder
public record MaintenanceScheduleDTO(
        Long id,
        Long roomId,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startTime,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endTime,
        
        String reason,
        String notes,
        Long createdBy,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
