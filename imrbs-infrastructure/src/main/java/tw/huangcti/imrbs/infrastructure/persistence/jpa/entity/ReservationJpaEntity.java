package tw.huangcti.imrbs.infrastructure.persistence.jpa.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import tw.huangcti.imrbs.domain.model.Reservation;

import java.time.LocalDateTime;

/**
 * ReservationJpaEntity - Reservation 的 JPA 實體
 * 
 * 描述: 對應資料庫 reservations 表
 * 
 * 設計: Clean Architecture - Infrastructure Layer 實作
 * 職責: 資料庫持久化，處理時間重疊約束
 */
@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservation_room_time", columnList = "room_id, start_time, end_time"),
        @Index(name = "idx_reservation_user", columnList = "user_id"),
        @Index(name = "idx_reservation_status", columnList = "status"),
        @Index(name = "idx_reservation_start_time", columnList = "start_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", nullable = false)
    private Long roomId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "meeting_title", nullable = false, length = 200)
    private String meetingTitle;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "participants", columnDefinition = "TEXT")
    private String participants;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Reservation.ReservationStatus status;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    @Column(name = "external_meeting_link", length = 500)
    private String externalMeetingLink;
    
    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;
    
    @Type(JsonBinaryType.class)
    @Column(name = "recurring_rule", columnDefinition = "jsonb")
    private Reservation.RecurringRule recurringRule;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Reservation.ReservationStatus.CONFIRMED;
        }
        if (isRecurring == null) {
            isRecurring = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 轉換為 Domain Model
     * 
     * @return Reservation domain model
     */
    public Reservation toDomain() {
        return Reservation.builder()
                .id(id)
                .roomId(roomId)
                .userId(userId)
                .meetingTitle(meetingTitle)
                .startTime(startTime)
                .endTime(endTime)
                .participants(participants)
                .description(description)
                .status(status)
                .cancellationReason(cancellationReason)
                .externalMeetingLink(externalMeetingLink)
                .isRecurring(isRecurring)
                .recurringRule(recurringRule)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .cancelledAt(cancelledAt)
                .build();
    }
    
    /**
     * 從 Domain Model 轉換
     * 
     * @param reservation domain model
     * @return ReservationJpaEntity
     */
    public static ReservationJpaEntity fromDomain(Reservation reservation) {
        return ReservationJpaEntity.builder()
                .id(reservation.getId())
                .roomId(reservation.getRoomId())
                .userId(reservation.getUserId())
                .meetingTitle(reservation.getMeetingTitle())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .participants(reservation.getParticipants())
                .description(reservation.getDescription())
                .status(reservation.getStatus())
                .cancellationReason(reservation.getCancellationReason())
                .externalMeetingLink(reservation.getExternalMeetingLink())
                .isRecurring(reservation.getIsRecurring())
                .recurringRule(reservation.getRecurringRule())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .cancelledAt(reservation.getCancelledAt())
                .build();
    }
}
