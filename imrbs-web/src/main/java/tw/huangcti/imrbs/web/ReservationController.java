package tw.huangcti.imrbs.web;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.huangcti.imrbs.domain.Reservation;
import tw.huangcti.imrbs.service.ReservationService;
import tw.huangcti.imrbs.web.dto.CreateReservationRequest;
import tw.huangcti.imrbs.web.dto.ReservationResponse;
import tw.huangcti.imrbs.web.dto.UpdateReservationRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 預約控制器
 */
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * 建立新預約
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {
        
        log.info("建立預約請求: roomId={}, date={}, time={}-{}", 
            request.getRoomId(), request.getDate(), 
            request.getStartTime(), request.getEndTime());

        Reservation reservation = Reservation.builder()
            .roomId(request.getRoomId())
            .date(request.getDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .title(request.getTitle())
            .organizerEmail(request.getOrganizerEmail())
            .participants(request.getParticipants())
            .build();

        Reservation created = reservationService.createReservation(reservation);

        log.info("預約建立成功: id={}", created.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ReservationResponse.from(created));
    }

    /**
     * 查詢所有預約
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        List<Reservation> reservations = reservationService.findAll();
        List<ReservationResponse> responses = reservations.stream()
            .map(ReservationResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 根據 ID 查詢預約
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable String id) {
        Reservation reservation = reservationService.findById(id);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    /**
     * 更新預約
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable String id,
            @Valid @RequestBody UpdateReservationRequest request) {

        log.info("更新預約請求: id={}, roomId={}, date={}", id, request.getRoomId(), request.getDate());

        Reservation reservation = Reservation.builder()
            .roomId(request.getRoomId())
            .date(request.getDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .title(request.getTitle())
            .organizerEmail(request.getOrganizerEmail())
            .participants(request.getParticipants())
            .build();

        Reservation updated = reservationService.updateReservation(id, reservation);

        log.info("預約更新成功: id={}", updated.getId());

        return ResponseEntity.ok(ReservationResponse.from(updated));
    }

    /**
     * 取消預約
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable String id) {
        log.info("取消預約請求: id={}", id);

        Reservation cancelled = reservationService.cancelReservation(id);

        log.info("預約取消成功: id={}", cancelled.getId());

        return ResponseEntity.ok(ReservationResponse.from(cancelled));
    }
}
