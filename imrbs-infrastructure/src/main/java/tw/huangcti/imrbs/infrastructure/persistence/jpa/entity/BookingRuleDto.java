package tw.huangcti.imrbs.infrastructure.persistence.jpa.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.huangcti.imrbs.domain.model.Room;

import java.util.ArrayList;
import java.util.List;

/**
 * BookingRuleDto - 用於 JSON 序列化/反序列化的 DTO
 * 
 * 描述: 處理資料庫中使用 snake_case 的 JSON 欄位
 * 與 Domain Model 的 BookingRule 進行轉換
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRuleDto {
    
    @JsonProperty("max_duration_hours")
    private Integer maxDurationHours;
    
    @JsonProperty("advance_booking_days")
    private Integer advanceBookingDays;
    
    @JsonProperty("min_booking_minutes")
    private Integer minBookingMinutes;
    
    @JsonProperty("booking_time_slots")
    @Builder.Default
    private List<String> bookingTimeSlots = new ArrayList<>();
    
    /**
     * 轉換為 Domain Model
     */
    public Room.BookingRule toDomain() {
        return Room.BookingRule.builder()
                .maxDurationHours(maxDurationHours)
                .advanceBookingDays(advanceBookingDays)
                .minBookingMinutes(minBookingMinutes)
                .bookingTimeSlots(bookingTimeSlots != null ? new ArrayList<>(bookingTimeSlots) : new ArrayList<>())
                .build();
    }
    
    /**
     * 從 Domain Model 轉換
     */
    public static BookingRuleDto fromDomain(Room.BookingRule bookingRule) {
        if (bookingRule == null) {
            return null;
        }
        return BookingRuleDto.builder()
                .maxDurationHours(bookingRule.getMaxDurationHours())
                .advanceBookingDays(bookingRule.getAdvanceBookingDays())
                .minBookingMinutes(bookingRule.getMinBookingMinutes())
                .bookingTimeSlots(bookingRule.getBookingTimeSlots() != null 
                        ? new ArrayList<>(bookingRule.getBookingTimeSlots()) 
                        : new ArrayList<>())
                .build();
    }
}
