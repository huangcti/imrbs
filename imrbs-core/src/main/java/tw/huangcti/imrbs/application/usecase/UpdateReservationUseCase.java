package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.event.ReservationEventPublisher;
import tw.huangcti.imrbs.domain.exception.NotFoundException;
import tw.huangcti.imrbs.domain.exception.ForbiddenException;
import tw.huangcti.imrbs.domain.exception.ValidationException;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.service.ConflictDetectionService;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * UpdateReservationUseCase - 修改預約 Use Case
 * 
 * 負責修改現有預約的時間、標題、參與者等資訊
 * 需檢查:
 * 1. 預約是否存在
 * 2. 使用者是否有權限修改
 * 3. 新時段是否與其他預約衝突
 * 4. 結束時間是否晚於開始時間
 * 
 * Clean Architecture: Application Layer (框架無關)
 */
@RequiredArgsConstructor
public class UpdateReservationUseCase {
    
    private final ReservationRepository reservationRepository;
    private final ConflictDetectionService conflictDetectionService;
    private final Optional<ReservationEventPublisher> eventPublisher;
    
    /**
     * 修改預約請求 DTO
     */
    public record UpdateRequest(
            Long userId,
            String meetingTitle,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String participants
    ) {}
    
    /**
     * 執行修改預約
     * 
     * @param reservationId 預約 ID
     * @param request 修改請求資料
     * @return 更新後的預約
     */
    public Reservation execute(Long reservationId, UpdateRequest request) {
        // 1. 查詢預約
        Reservation existingReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("預約不存在"));
        
        // 2. 檢查權限 (只能修改自己的預約)
        if (!existingReservation.getUserId().equals(request.userId())) {
            throw new ForbiddenException("無權限修改此預約");
        }
        
        // 3. 驗證時間範圍
        if (request.endTime().isBefore(request.startTime()) || 
            request.endTime().equals(request.startTime())) {
            throw new ValidationException("結束時間必須晚於開始時間");
        }
        
        // 4. 如果修改了時間，檢查新時段是否衝突 (排除自己)
        if (!request.startTime().equals(existingReservation.getStartTime()) ||
            !request.endTime().equals(existingReservation.getEndTime())) {
            
            // checkConflict 會在有衝突時拋出 ConflictException
            conflictDetectionService.checkConflict(
                    existingReservation.getRoomId(),
                    request.startTime(),
                    request.endTime(),
                    reservationId // 排除自己
            );
        }
        
        // 5. 更新預約資訊
        existingReservation.setMeetingTitle(request.meetingTitle());
        existingReservation.setStartTime(request.startTime());
        existingReservation.setEndTime(request.endTime());
        existingReservation.setParticipants(request.participants());
        existingReservation.setUpdatedAt(LocalDateTime.now());
        
        // 6. 儲存
        Reservation updatedReservation = reservationRepository.save(existingReservation);
        
        // 7. 發布事件 (Optional - 如果有配置 RabbitMQ)
        eventPublisher.ifPresent(publisher -> 
            publisher.publishReservationUpdated(
                updatedReservation.getId(), 
                updatedReservation.getUserId(), 
                updatedReservation.getUpdatedAt()
            )
        );
        
        return updatedReservation;
    }
}
