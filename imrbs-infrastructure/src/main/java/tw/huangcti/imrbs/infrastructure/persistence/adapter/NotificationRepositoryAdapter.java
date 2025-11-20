package tw.huangcti.imrbs.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.model.Notification;
import tw.huangcti.imrbs.domain.repository.NotificationRepository;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.NotificationJpaEntity;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.repository.NotificationJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * NotificationRepositoryAdapter - Notification Repository 的 JPA 實作
 */
@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {
    
    private final NotificationJpaRepository jpaRepository;
    
    private static final int MAX_RETRY_COUNT = 3;
    
    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = NotificationJpaEntity.fromDomain(notification);
        NotificationJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
    
    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id)
                .map(NotificationJpaEntity::toDomain);
    }
    
    @Override
    public List<Notification> findByReservationId(Long reservationId) {
        return jpaRepository.findByReservationId(reservationId).stream()
                .map(NotificationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Notification> findByRecipientEmail(String recipientEmail) {
        return jpaRepository.findByRecipientEmail(recipientEmail).stream()
                .map(NotificationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Notification> findByStatus(Notification.NotificationStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(NotificationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Notification> findByNotificationType(Notification.NotificationType notificationType) {
        return jpaRepository.findByNotificationType(notificationType).stream()
                .map(NotificationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    public List<Notification> findPendingNotificationsForRetry() {
        return jpaRepository.findPendingNotificationsForRetry(
                Notification.NotificationStatus.FAILED, 
                MAX_RETRY_COUNT).stream()
                .map(NotificationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    public List<Notification> findRecentNotificationsByEmail(String email, int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return jpaRepository.findRecentNotificationsByEmail(email, fromDate).stream()
                .map(NotificationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Notification> findAll() {
        return jpaRepository.findAll().stream()
                .map(NotificationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findPendingNotifications() {
        return findByStatus(Notification.NotificationStatus.PENDING);
    }

    @Override
    public List<Notification> findNotificationsForRetry() {
        return findPendingNotificationsForRetry();
    }

    @Override
    public List<Notification> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.findAll().stream()
                .map(NotificationJpaEntity::toDomain)
                .filter(n -> n.getCreatedAt().isAfter(startTime) && n.getCreatedAt().isBefore(endTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findFailedNotifications() {
        return findByStatus(Notification.NotificationStatus.FAILED);
    }

    @Override
    public int deleteOldNotifications(LocalDateTime beforeDate) {
        List<NotificationJpaEntity> oldNotifications = jpaRepository.findAll().stream()
                .filter(n -> n.getStatus() == Notification.NotificationStatus.SENT)
                .filter(n -> n.getCreatedAt().isBefore(beforeDate))
                .collect(Collectors.toList());
        jpaRepository.deleteAll(oldNotifications);
        return oldNotifications.size();
    }
}
