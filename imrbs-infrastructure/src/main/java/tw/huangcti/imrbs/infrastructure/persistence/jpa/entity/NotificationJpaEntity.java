package tw.huangcti.imrbs.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.huangcti.imrbs.domain.model.Notification;

import java.time.LocalDateTime;

/**
 * NotificationJpaEntity - Notification 的 JPA 實體
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_reservation", columnList = "reservation_id"),
        @Index(name = "idx_notification_status", columnList = "status"),
        @Index(name = "idx_notification_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "reservation_id")
    private Long reservationId;
    
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private Notification.NotificationType notificationType;
    
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;
    
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Notification.NotificationStatus status;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Notification.NotificationStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
    
    public Notification toDomain() {
        return Notification.builder()
                .id(id)
                .reservationId(reservationId)
                .recipientEmail(recipientEmail)
                .notificationType(notificationType)
                .subject(subject)
                .body(body)
                .sentAt(sentAt)
                .status(status)
                .errorMessage(errorMessage)
                .retryCount(retryCount)
                .createdAt(createdAt)
                .build();
    }
    
    public static NotificationJpaEntity fromDomain(Notification notification) {
        return NotificationJpaEntity.builder()
                .id(notification.getId())
                .reservationId(notification.getReservationId())
                .recipientEmail(notification.getRecipientEmail())
                .notificationType(notification.getNotificationType())
                .subject(notification.getSubject())
                .body(notification.getBody())
                .sentAt(notification.getSentAt())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
