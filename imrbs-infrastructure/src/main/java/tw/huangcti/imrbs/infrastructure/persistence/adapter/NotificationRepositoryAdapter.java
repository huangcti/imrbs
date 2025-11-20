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
    
    @Override
    public List<Notification> findPendingNotificationsForRetry() {
        return jpaRepository.findPendingNotificationsForRetry(
                Notification.NotificationStatus.FAILED, 
                MAX_RETRY_COUNT).stream()
                .map(NotificationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Notification> findRecentNotificationsByEmail(String email, int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return jpaRepository.findRecentNotificationsByEmail(email, fromDate).stream()
                .map(NotificationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
