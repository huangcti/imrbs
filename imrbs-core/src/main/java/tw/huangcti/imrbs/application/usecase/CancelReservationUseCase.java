package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.event.ReservationEventPublisher;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ForbiddenException;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.service.CancellationPolicyService;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * CancelReservationUseCase - 取消預約 Use Case
 * 
 * 負責取消預約,檢查:
 * 1. 預約是否存在
 * 2. 使用者是否有權限取消
 * 3. 是否符合 24 小時取消規則
 * 4. 預約狀態是否允許取消
 * 
 * Clean Architecture: Application Layer (框架無關)
 */
@RequiredArgsConstructor
public class CancelReservationUseCase {
    
    private final ReservationRepository reservationRepository;
    private final CancellationPolicyService cancellationPolicyService;
    private final Optional<ReservationEventPublisher> eventPublisher;
    
    /**
     * 執行取消預約
     * 
     * @param reservationId 預約 ID
     * @param userId 使用者 ID
     * @param cancellationReason 取消原因
     */
    public void execute(Long reservationId, Long userId, String cancellationReason) {
        // 1. 查詢預約
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("預約不存在"));
        
        // 2. 檢查權限 (只能取消自己的預約)
        if (!reservation.getUserId().equals(userId)) {
            throw new ForbiddenException("無權限取消此預約");
        }
        
        // 3. 驗證取消政策 (24 小時規則、狀態檢查)
        cancellationPolicyService.validateCancellation(reservation, LocalDateTime.now());
        
        // 4. 更新預約狀態為已取消
        LocalDateTime cancelledAt = LocalDateTime.now();
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setCancellationReason(cancellationReason);
        reservation.setUpdatedAt(cancelledAt);
        
        // 5. 儲存變更
        reservationRepository.save(reservation);
        
        // 6. 發布事件 (Optional - 如果有配置 RabbitMQ)
        eventPublisher.ifPresent(publisher -> 
            publisher.publishReservationCancelled(
                reservation.getId(), 
                reservation.getUserId(), 
                cancellationReason,
                cancelledAt
            )
        );
    }
}
