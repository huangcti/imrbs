package tw.huangcti.imrbs.service;

import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.domain.Reservation;
import tw.huangcti.imrbs.domain.SchemaVersion;
import tw.huangcti.imrbs.exception.NotFoundException;
import tw.huangcti.imrbs.exception.ValidationException;
import tw.huangcti.imrbs.repository.ReservationRepository;
import tw.huangcti.imrbs.repository.RoomRepository;
import tw.huangcti.imrbs.service.email.EmailService;
import tw.huangcti.imrbs.util.TimeRangeValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 預約應用層服務
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ConflictChecker conflictChecker;
    private final EmailService emailService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ConflictChecker conflictChecker,
            EmailService emailService) {
        this.reservationRepository = reservationRepository;
        this.conflictChecker = conflictChecker;
        this.emailService = emailService;
    }

    /**
     * 建立新預約
     */
    public Reservation createReservation(Reservation reservation) {
        validateReservation(reservation);
        
        // 檢查衝突
        List<Reservation> existing = reservationRepository.findByRoomIdAndDate(
            reservation.getRoomId(),
            reservation.getDate()
        );
        conflictChecker.checkConflict(
            existing,
            reservation.getRoomId(),
            reservation.getDate(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            null
        );

        // 設定預設值
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation.setSchemaVersion(SchemaVersion.CURRENT);

        // 儲存
        Reservation saved = reservationRepository.save(reservation);

        // 發送確認 email
        emailService.sendReservationConfirmation(saved);

        return saved;
    }

    /**
     * 更新預約
     */
    public Reservation updateReservation(String id, Reservation updated) {
        Reservation existing = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("預約不存在: " + id));

        validateReservation(updated);

        // 如果時間有變更，需檢查衝突
        if (!existing.getStartTime().equals(updated.getStartTime()) ||
            !existing.getEndTime().equals(updated.getEndTime()) ||
            !existing.getDate().equals(updated.getDate())) {
            
            List<Reservation> allReservations = reservationRepository.findByRoomIdAndDate(
                updated.getRoomId(),
                updated.getDate()
            );
            conflictChecker.checkConflict(
                allReservations,
                updated.getRoomId(),
                updated.getDate(),
                updated.getStartTime(),
                updated.getEndTime(),
                id
            );
        }

        // 更新欄位
        existing.setRoomId(updated.getRoomId());
        existing.setDate(updated.getDate());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setTitle(updated.getTitle());
        existing.setOrganizerEmail(updated.getOrganizerEmail());
        existing.setParticipants(updated.getParticipants());
        existing.setUpdatedAt(LocalDateTime.now());

        Reservation saved = reservationRepository.save(existing);
        emailService.sendReservationUpdate(saved);

        return saved;
    }

    /**
     * 取消預約
     */
    public Reservation cancelReservation(String id) {
        Reservation existing = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("預約不存在: " + id));

        existing.setStatus(Reservation.ReservationStatus.CANCELLED);
        existing.setUpdatedAt(LocalDateTime.now());

        Reservation saved = reservationRepository.save(existing);
        emailService.sendReservationCancellation(saved);

        return saved;
    }

    /**
     * 根據 ID 查詢預約
     */
    public Reservation findById(String id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("預約不存在: " + id));
    }

    /**
     * 查詢所有預約
     */
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    private void validateReservation(Reservation reservation) {
        if (reservation.getRoomId() == null || reservation.getRoomId().isEmpty()) {
            throw new ValidationException("會議室 ID 不得為空");
        }
        if (reservation.getDate() == null) {
            throw new ValidationException("日期不得為空");
        }
        if (reservation.getStartTime() == null || reservation.getEndTime() == null) {
            throw new ValidationException("開始/結束時間不得為空");
        }
        if (!TimeRangeValidator.isValid(reservation.getStartTime(), reservation.getEndTime())) {
            throw new ValidationException("開始時間必須在結束時間之前");
        }
        if (reservation.getOrganizerEmail() == null || reservation.getOrganizerEmail().isEmpty()) {
            throw new ValidationException("聯絡 email 不得為空");
        }
        if (reservation.getTitle() == null || reservation.getTitle().isEmpty()) {
            throw new ValidationException("會議主題不得為空");
        }
    }
}
