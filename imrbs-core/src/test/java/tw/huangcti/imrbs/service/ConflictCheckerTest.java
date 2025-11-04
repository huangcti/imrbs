package tw.huangcti.imrbs.service;

import org.junit.jupiter.api.Test;
import tw.huangcti.imrbs.domain.Reservation;
import tw.huangcti.imrbs.exception.ConflictException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConflictChecker 單元測試
 */
class ConflictCheckerTest {

    private final ConflictChecker conflictChecker = new ConflictChecker();

    @Test
    void testNoConflict_EmptyList() {
        List<Reservation> existing = new ArrayList<>();
        assertDoesNotThrow(() ->
            conflictChecker.checkConflict(
                existing,
                "room1",
                LocalDate.of(2025, 11, 4),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                null
            )
        );
    }

    @Test
    void testNoConflict_DifferentTimes() {
        List<Reservation> existing = List.of(
            buildReservation("1", "room1", LocalDate.of(2025, 11, 4),
                LocalTime.of(9, 0), LocalTime.of(10, 0))
        );

        assertDoesNotThrow(() ->
            conflictChecker.checkConflict(
                existing,
                "room1",
                LocalDate.of(2025, 11, 4),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                null
            )
        );
    }

    @Test
    void testConflict_Overlapping() {
        List<Reservation> existing = List.of(
            buildReservation("1", "room1", LocalDate.of(2025, 11, 4),
                LocalTime.of(9, 0), LocalTime.of(10, 0))
        );

        ConflictException exception = assertThrows(ConflictException.class, () ->
            conflictChecker.checkConflict(
                existing,
                "room1",
                LocalDate.of(2025, 11, 4),
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                null
            )
        );

        assertTrue(exception.getMessage().contains("衝突"));
    }

    @Test
    void testNoConflict_ExcludeId() {
        List<Reservation> existing = List.of(
            buildReservation("1", "room1", LocalDate.of(2025, 11, 4),
                LocalTime.of(9, 0), LocalTime.of(10, 0))
        );

        // 更新時排除自己，不應衝突
        assertDoesNotThrow(() ->
            conflictChecker.checkConflict(
                existing,
                "room1",
                LocalDate.of(2025, 11, 4),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "1"
            )
        );
    }

    @Test
    void testNoConflict_CancelledReservation() {
        Reservation cancelled = buildReservation("1", "room1", LocalDate.of(2025, 11, 4),
            LocalTime.of(9, 0), LocalTime.of(10, 0));
        cancelled.setStatus(Reservation.ReservationStatus.CANCELLED);

        List<Reservation> existing = List.of(cancelled);

        // 已取消的預約不應造成衝突
        assertDoesNotThrow(() ->
            conflictChecker.checkConflict(
                existing,
                "room1",
                LocalDate.of(2025, 11, 4),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                null
            )
        );
    }

    private Reservation buildReservation(String id, String roomId, LocalDate date,
                                          LocalTime start, LocalTime end) {
        return Reservation.builder()
            .id(id)
            .roomId(roomId)
            .date(date)
            .startTime(start)
            .endTime(end)
            .status(Reservation.ReservationStatus.CONFIRMED)
            .build();
    }
}
