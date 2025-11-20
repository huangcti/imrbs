package tw.huangcti.imrbs.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.model.Notification;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.NotificationJpaEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationJpaRepository - Notification 的 Spring Data JPA Repository
 */
@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {
    
    List<NotificationJpaEntity> findByReservationId(Long reservationId);
    
    List<NotificationJpaEntity> findByRecipientEmail(String recipientEmail);
    
    List<NotificationJpaEntity> findByStatus(Notification.NotificationStatus status);
    
    List<NotificationJpaEntity> findByNotificationType(Notification.NotificationType notificationType);
    
    @Query("SELECT n FROM NotificationJpaEntity n " +
           "WHERE n.status = :status " +
           "AND n.retryCount < :maxRetries " +
           "ORDER BY n.createdAt ASC")
    List<NotificationJpaEntity> findPendingNotificationsForRetry(
            @Param("status") Notification.NotificationStatus status,
            @Param("maxRetries") int maxRetries);
    
    @Query("SELECT n FROM NotificationJpaEntity n " +
           "WHERE n.recipientEmail = :email " +
           "AND n.createdAt >= :fromDate " +
           "ORDER BY n.createdAt DESC")
    List<NotificationJpaEntity> findRecentNotificationsByEmail(
            @Param("email") String email,
            @Param("fromDate") LocalDateTime fromDate);
}
