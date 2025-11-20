package tw.huangcti.imrbs.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.huangcti.imrbs.domain.model.GuestReservationRequest;

import java.time.LocalDateTime;

/**
 * GuestReservationRequestJpaEntity - GuestReservationRequest 的 JPA 實體
 */
@Entity
@Table(name = "guest_reservation_requests", indexes = {
        @Index(name = "idx_guest_request_status", columnList = "status"),
        @Index(name = "idx_guest_request_room", columnList = "room_id"),
        @Index(name = "idx_guest_request_email", columnList = "guest_email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestReservationRequestJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "guest_name", nullable = false, length = 100)
    private String guestName;
    
    @Column(name = "guest_company", nullable = false, length = 100)
    private String guestCompany;
    
    @Column(name = "guest_email", nullable = false, length = 255)
    private String guestEmail;
    
    @Column(name = "guest_phone", length = 20)
    private String guestPhone;
    
    @Column(name = "room_id", nullable = false)
    private Long roomId;
    
    @Column(name = "requested_start_time", nullable = false)
    private LocalDateTime requestedStartTime;
    
    @Column(name = "requested_end_time", nullable = false)
    private LocalDateTime requestedEndTime;
    
    @Column(name = "meeting_title", nullable = false, length = 200)
    private String meetingTitle;
    
    @Column(name = "meeting_purpose", nullable = false, columnDefinition = "TEXT")
    private String meetingPurpose;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GuestReservationRequest.RequestStatus status;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "reservation_id", unique = true)
    private Long reservationId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = GuestReservationRequest.RequestStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public GuestReservationRequest toDomain() {
        return GuestReservationRequest.builder()
                .id(id)
                .guestName(guestName)
                .guestCompany(guestCompany)
                .guestEmail(guestEmail)
                .guestPhone(guestPhone)
                .roomId(roomId)
                .requestedStartTime(requestedStartTime)
                .requestedEndTime(requestedEndTime)
                .meetingTitle(meetingTitle)
                .meetingPurpose(meetingPurpose)
                .status(status)
                .reviewedBy(reviewedBy)
                .reviewedAt(reviewedAt)
                .rejectionReason(rejectionReason)
                .reservationId(reservationId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
    
    public static GuestReservationRequestJpaEntity fromDomain(GuestReservationRequest request) {
        return GuestReservationRequestJpaEntity.builder()
                .id(request.getId())
                .guestName(request.getGuestName())
                .guestCompany(request.getGuestCompany())
                .guestEmail(request.getGuestEmail())
                .guestPhone(request.getGuestPhone())
                .roomId(request.getRoomId())
                .requestedStartTime(request.getRequestedStartTime())
                .requestedEndTime(request.getRequestedEndTime())
                .meetingTitle(request.getMeetingTitle())
                .meetingPurpose(request.getMeetingPurpose())
                .status(request.getStatus())
                .reviewedBy(request.getReviewedBy())
                .reviewedAt(request.getReviewedAt())
                .rejectionReason(request.getRejectionReason())
                .reservationId(request.getReservationId())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
