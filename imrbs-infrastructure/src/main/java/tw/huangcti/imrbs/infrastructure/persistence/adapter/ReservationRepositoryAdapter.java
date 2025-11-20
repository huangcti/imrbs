package tw.huangcti.imrbs.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.ReservationJpaEntity;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.repository.ReservationJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ReservationRepositoryAdapter - ReservationRepository 的實作
 * 
 * 描述: 連接 Domain Layer 與 Infrastructure Layer 的適配器
 * 
 * 設計: Clean Architecture - Adapter Pattern
 */
@Component
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepository {
    
    private final ReservationJpaRepository jpaRepository;
    
    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ReservationJpaEntity::toDomain);
    }
    
    @Override
    public List<Reservation> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(ReservationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status) {
        return jpaRepository.findByUserIdAndStatus(userId, status).stream()
                .map(ReservationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reservation> findByRoomId(Long roomId) {
        return jpaRepository.findByRoomId(roomId).stream()
                .map(ReservationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reservation> findByRoomIdAndTimeRange(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.findByRoomIdAndTimeRange(roomId, startTime, endTime).stream()
                .map(ReservationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId) {
        return jpaRepository.hasConflict(roomId, startTime, endTime, excludeReservationId);
    }
    
    @Override
    public List<Reservation> findUpcomingReservationsForReminder(int reminderMinutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusMinutes(reminderMinutes);
        return jpaRepository.findUpcomingReservationsForReminder(
                now,
                reminderTime,
                Reservation.ReservationStatus.CONFIRMED
        ).stream()
                .map(ReservationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reservation> findUpcomingReservationsByUserId(Long userId) {
        return jpaRepository.findUpcomingReservationsByUserId(
                userId,
                LocalDateTime.now(),
                Reservation.ReservationStatus.CONFIRMED
        ).stream()
                .map(ReservationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reservation> findPastReservationsByUserId(Long userId) {
        return jpaRepository.findPastReservationsByUserId(userId, LocalDateTime.now()).stream()
                .map(ReservationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Reservation> findPendingReservations() {
        return jpaRepository.findByStatus(Reservation.ReservationStatus.PENDING).stream()
                .map(ReservationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Reservation save(Reservation reservation) {
        ReservationJpaEntity entity = ReservationJpaEntity.fromDomain(reservation);
        ReservationJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public List<Reservation> findAll() {
        return jpaRepository.findAll().stream()
                .map(ReservationJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Reservation> findByExternalMeetingLink(String externalMeetingLink) {
        return jpaRepository.findByExternalMeetingLink(externalMeetingLink)
                .map(ReservationJpaEntity::toDomain);
    }
}
