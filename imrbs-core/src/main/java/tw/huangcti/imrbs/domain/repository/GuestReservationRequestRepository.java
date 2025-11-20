package tw.huangcti.imrbs.domain.repository;

import tw.huangcti.imrbs.domain.model.GuestReservationRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * GuestReservationRequestRepository 介面
 * 
 * 描述: 訪客預約申請資料存取介面 (Domain Layer)
 * 
 * 實作位置: Infrastructure Layer (imrbs-infrastructure)
 */
public interface GuestReservationRequestRepository {
    
    /**
     * 根據 ID 查詢訪客預約申請
     * 
     * @param id 申請 ID
     * @return Optional<GuestReservationRequest>
     */
    Optional<GuestReservationRequest> findById(Long id);
    
    /**
     * 根據狀態查詢訪客預約申請清單
     * 
     * @param status 申請狀態 (PENDING, APPROVED, REJECTED)
     * @return List<GuestReservationRequest>
     */
    List<GuestReservationRequest> findByStatus(GuestReservationRequest.RequestStatus status);
    
    /**
     * 查詢所有待審核的申請 (status = PENDING)
     * 
     * @return List<GuestReservationRequest>
     */
    List<GuestReservationRequest> findPendingRequests();
    
    /**
     * 根據會議室 ID 查詢訪客預約申請清單
     * 
     * @param roomId 會議室 ID
     * @return List<GuestReservationRequest>
     */
    List<GuestReservationRequest> findByRoomId(Long roomId);
    
    /**
     * 根據訪客 Email 查詢訪客預約申請清單
     * 
     * @param guestEmail 訪客 Email
     * @return List<GuestReservationRequest>
     */
    List<GuestReservationRequest> findByGuestEmail(String guestEmail);
    
    /**
     * 根據審核者 ID 查詢訪客預約申請清單
     * 
     * @param reviewedBy 審核者 ID
     * @return List<GuestReservationRequest>
     */
    List<GuestReservationRequest> findByReviewedBy(Long reviewedBy);
    
    /**
     * 根據預約 ID 查詢訪客預約申請
     * 
     * @param reservationId 預約 ID
     * @return Optional<GuestReservationRequest>
     */
    Optional<GuestReservationRequest> findByReservationId(Long reservationId);
    
    /**
     * 查詢指定時間範圍內的訪客預約申請
     * 
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return List<GuestReservationRequest>
     */
    List<GuestReservationRequest> findByRequestedTimeRange(
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    
    /**
     * 查詢已過期的待審核申請 (requestedStartTime < now, status = PENDING)
     * 
     * @return List<GuestReservationRequest>
     */
    List<GuestReservationRequest> findExpiredPendingRequests();
    
    /**
     * 儲存訪客預約申請 (新增或更新)
     * 
     * @param request 訪客預約申請實體
     * @return 儲存後的訪客預約申請實體
     */
    GuestReservationRequest save(GuestReservationRequest request);
    
    /**
     * 刪除訪客預約申請 (硬刪除)
     * 
     * @param id 申請 ID
     */
    void deleteById(Long id);
    
    /**
     * 查詢所有訪客預約申請
     * 
     * @return List<GuestReservationRequest>
     */
    List<GuestReservationRequest> findAll();
}
