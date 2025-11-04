package tw.huangcti.imrbs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tw.huangcti.imrbs.domain.Reservation;
import tw.huangcti.imrbs.exception.ConflictException;
import tw.huangcti.imrbs.exception.NotFoundException;
import tw.huangcti.imrbs.exception.ValidationException;
import tw.huangcti.imrbs.repository.json.JsonReservationRepository;
import tw.huangcti.imrbs.service.email.InMemoryEmailSender;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReservationService 整合測試
 */
class ReservationServiceIT {

    @TempDir
    Path tempDir;

    private ReservationService service;
    private JsonReservationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonReservationRepository();
        // 使用反射或 setter 設定臨時檔案路徑
        try {
            var field = JsonReservationRepository.class.getDeclaredField("dataFilePath");
            field.setAccessible(true);
            field.set(repository, tempDir.resolve("reservations.json").toString());
            repository.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConflictChecker conflictChecker = new ConflictChecker();
        InMemoryEmailSender emailService = new InMemoryEmailSender();
        service = new ReservationService(repository, conflictChecker, emailService);
    }

    @Test
    void testCreateReservation_Success() {
        Reservation reservation = buildReservation("room1", LocalDate.of(2025, 11, 4),
            LocalTime.of(9, 0), LocalTime.of(10, 0));

        Reservation created = service.createReservation(reservation);

        assertNotNull(created.getId());
        assertEquals(Reservation.ReservationStatus.CONFIRMED, created.getStatus());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    void testCreateReservation_Conflict() {
        Reservation first = buildReservation("room1", LocalDate.of(2025, 11, 4),
            LocalTime.of(9, 0), LocalTime.of(10, 0));
        service.createReservation(first);

        Reservation second = buildReservation("room1", LocalDate.of(2025, 11, 4),
            LocalTime.of(9, 30), LocalTime.of(10, 30));

        assertThrows(ConflictException.class, () -> service.createReservation(second));
    }

    @Test
    void testCreateReservation_Validation() {
        Reservation invalid = buildReservation("room1", LocalDate.of(2025, 11, 4),
            LocalTime.of(10, 0), LocalTime.of(9, 0)); // 結束時間在開始前

        assertThrows(ValidationException.class, () -> service.createReservation(invalid));
    }

    @Test
    void testUpdateReservation_Success() {
        Reservation reservation = buildReservation("room1", LocalDate.of(2025, 11, 4),
            LocalTime.of(9, 0), LocalTime.of(10, 0));
        Reservation created = service.createReservation(reservation);

        created.setTitle("Updated Title");
        Reservation updated = service.updateReservation(created.getId(), created);

        assertEquals("Updated Title", updated.getTitle());
    }

    @Test
    void testUpdateReservation_NotFound() {
        Reservation reservation = buildReservation("room1", LocalDate.of(2025, 11, 4),
            LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThrows(NotFoundException.class, () ->
            service.updateReservation("nonexistent", reservation));
    }

    @Test
    void testCancelReservation_Success() {
        Reservation reservation = buildReservation("room1", LocalDate.of(2025, 11, 4),
            LocalTime.of(9, 0), LocalTime.of(10, 0));
        Reservation created = service.createReservation(reservation);

        Reservation cancelled = service.cancelReservation(created.getId());

        assertEquals(Reservation.ReservationStatus.CANCELLED, cancelled.getStatus());
    }

    private Reservation buildReservation(String roomId, LocalDate date,
                                          LocalTime start, LocalTime end) {
        return Reservation.builder()
            .roomId(roomId)
            .date(date)
            .startTime(start)
            .endTime(end)
            .title("Test Meeting")
            .organizerEmail("organizer@test.com")
            .participants(Arrays.asList("user1@test.com", "user2@test.com"))
            .build();
    }
}
