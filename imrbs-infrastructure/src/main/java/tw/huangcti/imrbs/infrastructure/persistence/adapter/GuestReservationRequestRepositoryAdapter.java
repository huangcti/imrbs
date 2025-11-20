package tw.huangcti.imrbs.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.model.GuestReservationRequest;
import tw.huangcti.imrbs.domain.repository.GuestReservationRequestRepository;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.GuestReservationRequestJpaEntity;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.repository.GuestReservationRequestJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * GuestReservationRequestRepositoryAdapter - GuestReservationRequest Repository 的 JPA 實作
 */
@Repository
@RequiredArgsConstructor
public class GuestReservationRequestRepositoryAdapter implements GuestReservationRequestRepository {
    
    private final GuestReservationRequestJpaRepository jpaRepository;
    
    @Override
    public GuestReservationRequest save(GuestReservationRequest request) {
        GuestReservationRequestJpaEntity entity = GuestReservationRequestJpaEntity.fromDomain(request);
        GuestReservationRequestJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
    
    @Override
    public Optional<GuestReservationRequest> findById(Long id) {
        return jpaRepository.findById(id)
                .map(GuestReservationRequestJpaEntity::toDomain);
    }
    
    @Override
    public List<GuestReservationRequest> findByStatus(GuestReservationRequest.RequestStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(GuestReservationRequestJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GuestReservationRequest> findByGuestEmail(String guestEmail) {
        return jpaRepository.findByGuestEmail(guestEmail).stream()
                .map(GuestReservationRequestJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GuestReservationRequest> findByRoomId(Long roomId) {
        return jpaRepository.findByRoomId(roomId).stream()
                .map(GuestReservationRequestJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<GuestReservationRequest> findByReservationId(Long reservationId) {
        return jpaRepository.findByReservationId(reservationId)
                .map(GuestReservationRequestJpaEntity::toDomain);
    }
    
    @Override
    public List<GuestReservationRequest> findPendingRequests() {
        return jpaRepository.findByStatusOrderByCreatedAt(GuestReservationRequest.RequestStatus.PENDING).stream()
                .map(GuestReservationRequestJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GuestReservationRequest> findRecentRequestsByEmail(String email, int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return jpaRepository.findRecentRequestsByEmail(email, fromDate).stream()
                .map(GuestReservationRequestJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GuestReservationRequest> findPendingRequestsInTimeRange(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.findPendingRequestsInTimeRange(roomId, startTime, endTime).stream()
                .map(GuestReservationRequestJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
