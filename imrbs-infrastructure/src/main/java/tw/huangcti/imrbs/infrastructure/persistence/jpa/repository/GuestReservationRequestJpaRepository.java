package tw.huangcti.imrbs.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.model.GuestReservationRequest;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.GuestReservationRequestJpaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * GuestReservationRequestJpaRepository - GuestReservationRequest 的 Spring Data JPA Repository
 */
@Repository
public interface GuestReservationRequestJpaRepository extends JpaRepository<GuestReservationRequestJpaEntity, Long> {
    
    List<GuestReservationRequestJpaEntity> findByStatus(GuestReservationRequest.RequestStatus status);
    
    List<GuestReservationRequestJpaEntity> findByGuestEmail(String guestEmail);
    
    List<GuestReservationRequestJpaEntity> findByRoomId(Long roomId);
    
    Optional<GuestReservationRequestJpaEntity> findByReservationId(Long reservationId);
    
    @Query("SELECT g FROM GuestReservationRequestJpaEntity g " +
           "WHERE g.status = :status " +
           "ORDER BY g.createdAt ASC")
    List<GuestReservationRequestJpaEntity> findByStatusOrderByCreatedAt(
            @Param("status") GuestReservationRequest.RequestStatus status);
    
    @Query("SELECT g FROM GuestReservationRequestJpaEntity g " +
           "WHERE g.guestEmail = :email " +
           "AND g.createdAt >= :fromDate " +
           "ORDER BY g.createdAt DESC")
    List<GuestReservationRequestJpaEntity> findRecentRequestsByEmail(
            @Param("email") String email,
            @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT g FROM GuestReservationRequestJpaEntity g " +
           "WHERE g.roomId = :roomId " +
           "AND g.requestedStartTime < :endTime " +
           "AND g.requestedEndTime > :startTime " +
           "AND g.status = 'PENDING'")
    List<GuestReservationRequestJpaEntity> findPendingRequestsInTimeRange(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
