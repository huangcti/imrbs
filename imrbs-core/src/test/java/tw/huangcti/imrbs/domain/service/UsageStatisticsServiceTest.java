package tw.huangcti.imrbs.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 使用率統計服務單元測試 (TDD Red Phase)
 * 
 * 測試場景:
 * 1. 單一會議室使用率計算
 * 2. 多會議室使用率彙總
 * 3. 時段分析 (熱門/冷門時段)
 * 4. 日/週/月報告
 * 5. 排除已取消的預約
 * 6. 邊界情況處理
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsageStatisticsService 使用率統計服務測試")
class UsageStatisticsServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private UsageStatisticsService usageStatisticsService;

    private Room testRoom1;
    private Room testRoom2;
    private LocalDate testDate;
    private LocalDateTime startOfDay;
    private LocalDateTime endOfDay;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2025, 12, 1);
        startOfDay = testDate.atStartOfDay();
        endOfDay = testDate.atTime(LocalTime.MAX);

        testRoom1 = Room.builder()
                .id(1L)
                .name("會議室 A")
                .building("總部大樓")
                .floor("3F")
                .locationDescription("301 室")
                .capacity(10)
                .status(Room.RoomStatus.AVAILABLE)
                .build();

        testRoom2 = Room.builder()
                .id(2L)
                .name("會議室 B")
                .building("總部大樓")
                .floor("3F")
                .locationDescription("302 室")
                .capacity(20)
                .status(Room.RoomStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("單一會議室使用率計算")
    class SingleRoomUsageTests {

        @Test
        @DisplayName("當會議室無預約時，使用率應為 0%")
        void shouldReturnZeroUsageWhenNoReservations() {
            // Given
            when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            double usageRate = usageStatisticsService.calculateRoomUsageRate(1L, testDate, testDate);

            // Then
            assertThat(usageRate).isEqualTo(0.0);
        }

        @Test
        @DisplayName("當會議室全天預約時，使用率應為 100%")
        void shouldReturnFullUsageWhenFullyBooked() {
            // Given - 假設營業時間 8:00-18:00 (10小時)
            when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
            Reservation fullDayReservation = createReservation(1L, 1L,
                    testDate.atTime(8, 0), testDate.atTime(18, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(fullDayReservation));

            // When
            double usageRate = usageStatisticsService.calculateRoomUsageRate(1L, testDate, testDate);

            // Then
            assertThat(usageRate).isCloseTo(100.0, within(0.01));
        }

        @Test
        @DisplayName("當會議室有 5 小時預約 (營業時間 10 小時)，使用率應為 50%")
        void shouldCalculateCorrectUsageRateForPartialBooking() {
            // Given - 5 小時預約
            when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
            Reservation partialReservation = createReservation(1L, 1L,
                    testDate.atTime(9, 0), testDate.atTime(14, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(partialReservation));

            // When
            double usageRate = usageStatisticsService.calculateRoomUsageRate(1L, testDate, testDate);

            // Then
            assertThat(usageRate).isCloseTo(50.0, within(0.01));
        }

        @Test
        @DisplayName("應排除已取消的預約")
        void shouldExcludeCancelledReservations() {
            // Given
            when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
            Reservation confirmedReservation = createReservation(1L, 1L,
                    testDate.atTime(9, 0), testDate.atTime(11, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            Reservation cancelledReservation = createReservation(2L, 1L,
                    testDate.atTime(14, 0), testDate.atTime(17, 0),
                    Reservation.ReservationStatus.CANCELLED);

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(confirmedReservation, cancelledReservation));

            // When
            double usageRate = usageStatisticsService.calculateRoomUsageRate(1L, testDate, testDate);

            // Then - 只計算 confirmed 的 2 小時 (2/10 = 20%)
            assertThat(usageRate).isCloseTo(20.0, within(0.01));
        }

        @Test
        @DisplayName("應正確處理跨多天的日期範圍")
        void shouldCalculateUsageForMultipleDays() {
            // Given - 2 天，每天 5 小時預約
            when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
            LocalDate endDate = testDate.plusDays(1);
            
            Reservation day1Reservation = createReservation(1L, 1L,
                    testDate.atTime(9, 0), testDate.atTime(14, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            Reservation day2Reservation = createReservation(2L, 1L,
                    endDate.atTime(9, 0), endDate.atTime(14, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(day1Reservation, day2Reservation));

            // When
            double usageRate = usageStatisticsService.calculateRoomUsageRate(1L, testDate, endDate);

            // Then - 10 小時預約 / 20 小時總營業時間 = 50%
            assertThat(usageRate).isCloseTo(50.0, within(0.01));
        }
    }

    @Nested
    @DisplayName("多會議室使用率彙總")
    class MultipleRoomsUsageTests {

        @Test
        @DisplayName("應計算所有會議室的平均使用率")
        void shouldCalculateAverageUsageAcrossAllRooms() {
            // Given
            when(roomRepository.findAll()).thenReturn(Arrays.asList(testRoom1, testRoom2));
            when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
            when(roomRepository.findById(2L)).thenReturn(Optional.of(testRoom2));

            // Room 1: 50% 使用率 (5 小時)
            Reservation room1Reservation = createReservation(1L, 1L,
                    testDate.atTime(9, 0), testDate.atTime(14, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            // Room 2: 30% 使用率 (3 小時)
            Reservation room2Reservation = createReservation(2L, 2L,
                    testDate.atTime(10, 0), testDate.atTime(13, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(room1Reservation));

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(room2Reservation));

            // When
            double averageUsage = usageStatisticsService.calculateOverallUsageRate(testDate, testDate);

            // Then - (50% + 30%) / 2 = 40%
            assertThat(averageUsage).isCloseTo(40.0, within(0.01));
        }

        @Test
        @DisplayName("應返回每個會議室的個別使用率")
        void shouldReturnUsagePerRoom() {
            // Given
            when(roomRepository.findAll()).thenReturn(Arrays.asList(testRoom1, testRoom2));
            when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom1));
            when(roomRepository.findById(2L)).thenReturn(Optional.of(testRoom2));

            Reservation room1Reservation = createReservation(1L, 1L,
                    testDate.atTime(9, 0), testDate.atTime(14, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(room1Reservation));

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            var usageByRoom = usageStatisticsService.calculateUsageByRoom(testDate, testDate);

            // Then
            assertThat(usageByRoom).hasSize(2);
            assertThat(usageByRoom.get(1L)).isCloseTo(50.0, within(0.01));
            assertThat(usageByRoom.get(2L)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("使用時數統計")
    class UsageHoursTests {

        @Test
        @DisplayName("應計算單一會議室的總使用時數")
        void shouldCalculateTotalUsageHoursForRoom() {
            // Given
            Reservation reservation1 = createReservation(1L, 1L,
                    testDate.atTime(9, 0), testDate.atTime(11, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            Reservation reservation2 = createReservation(2L, 1L,
                    testDate.atTime(14, 0), testDate.atTime(16, 30),
                    Reservation.ReservationStatus.CONFIRMED);

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(reservation1, reservation2));

            // When
            double usageHours = usageStatisticsService.calculateUsageHours(1L, testDate, testDate);

            // Then - 2 小時 + 2.5 小時 = 4.5 小時
            assertThat(usageHours).isCloseTo(4.5, within(0.01));
        }

        @Test
        @DisplayName("應計算所有會議室的總使用時數")
        void shouldCalculateTotalUsageHoursForAllRooms() {
            // Given
            when(roomRepository.findAll()).thenReturn(Arrays.asList(testRoom1, testRoom2));

            Reservation room1Reservation = createReservation(1L, 1L,
                    testDate.atTime(9, 0), testDate.atTime(12, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            Reservation room2Reservation = createReservation(2L, 2L,
                    testDate.atTime(10, 0), testDate.atTime(15, 0),
                    Reservation.ReservationStatus.CONFIRMED);

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(room1Reservation));

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(room2Reservation));

            // When
            double totalHours = usageStatisticsService.calculateTotalUsageHours(testDate, testDate);

            // Then - 3 小時 + 5 小時 = 8 小時
            assertThat(totalHours).isCloseTo(8.0, within(0.01));
        }
    }

    @Nested
    @DisplayName("預約次數統計")
    class ReservationCountTests {

        @Test
        @DisplayName("應計算單一會議室的預約次數")
        void shouldCountReservationsForRoom() {
            // Given
            List<Reservation> reservations = Arrays.asList(
                    createReservation(1L, 1L, testDate.atTime(9, 0), testDate.atTime(10, 0),
                            Reservation.ReservationStatus.CONFIRMED),
                    createReservation(2L, 1L, testDate.atTime(11, 0), testDate.atTime(12, 0),
                            Reservation.ReservationStatus.CONFIRMED),
                    createReservation(3L, 1L, testDate.atTime(14, 0), testDate.atTime(15, 0),
                            Reservation.ReservationStatus.CANCELLED)
            );

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(reservations);

            // When
            int count = usageStatisticsService.countReservations(1L, testDate, testDate);

            // Then - 排除已取消的，應為 2
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("應計算所有會議室的總預約次數")
        void shouldCountTotalReservations() {
            // Given
            when(roomRepository.findAll()).thenReturn(Arrays.asList(testRoom1, testRoom2));

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(
                            createReservation(1L, 1L, testDate.atTime(9, 0), testDate.atTime(10, 0),
                                    Reservation.ReservationStatus.CONFIRMED),
                            createReservation(2L, 1L, testDate.atTime(11, 0), testDate.atTime(12, 0),
                                    Reservation.ReservationStatus.CONFIRMED)
                    ));

            when(reservationRepository.findByRoomIdAndTimeRange(
                    eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(
                            createReservation(3L, 2L, testDate.atTime(14, 0), testDate.atTime(16, 0),
                                    Reservation.ReservationStatus.CONFIRMED)
                    ));

            // When
            int totalCount = usageStatisticsService.countTotalReservations(testDate, testDate);

            // Then
            assertThat(totalCount).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("邊界情況測試")
    class EdgeCaseTests {

        @Test
        @DisplayName("當日期範圍為空時應返回 0")
        void shouldReturnZeroForInvalidDateRange() {
            // Given - endDate < startDate
            LocalDate invalidEndDate = testDate.minusDays(1);

            // When
            double usageRate = usageStatisticsService.calculateRoomUsageRate(
                    1L, testDate, invalidEndDate);

            // Then
            assertThat(usageRate).isEqualTo(0.0);
        }

        @Test
        @DisplayName("當會議室不存在時應返回 0")
        void shouldReturnZeroForNonExistentRoom() {
            // Given
            when(roomRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            double usageRate = usageStatisticsService.calculateRoomUsageRate(
                    999L, testDate, testDate);

            // Then
            assertThat(usageRate).isEqualTo(0.0);
        }

        @Test
        @DisplayName("當沒有會議室時，整體使用率應為 0")
        void shouldReturnZeroWhenNoRooms() {
            // Given
            when(roomRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            double overallUsage = usageStatisticsService.calculateOverallUsageRate(testDate, testDate);

            // Then
            assertThat(overallUsage).isEqualTo(0.0);
        }
    }

    // Helper method
    private Reservation createReservation(Long id, Long roomId, LocalDateTime startTime,
                                           LocalDateTime endTime, Reservation.ReservationStatus status) {
        return Reservation.builder()
                .id(id)
                .roomId(roomId)
                .userId(1L)
                .meetingTitle("Test Meeting")
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .build();
    }
}
