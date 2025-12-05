package tw.huangcti.imrbs.domain.service;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 使用率統計服務
 * 職責: 計算會議室使用率、使用時數、預約次數等統計數據
 * 
 * 計算規則:
 * - 營業時間: 08:00 - 18:00 (10 小時/天)
 * - 使用率 = 實際使用時數 / 可用時數 * 100%
 * - 排除已取消的預約
 */
@RequiredArgsConstructor
public class UsageStatisticsService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    // 營業時間配置
    private static final LocalTime BUSINESS_START = LocalTime.of(8, 0);
    private static final LocalTime BUSINESS_END = LocalTime.of(18, 0);
    private static final int BUSINESS_HOURS_PER_DAY = 10; // 18:00 - 08:00 = 10 小時

    /**
     * 計算單一會議室的使用率
     * 
     * @param roomId 會議室 ID
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 使用率 (0.0 - 100.0)
     */
    public double calculateRoomUsageRate(Long roomId, LocalDate startDate, LocalDate endDate) {
        // 驗證日期範圍
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return 0.0;
        }

        // 檢查會議室是否存在
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            return 0.0;
        }

        // 計算使用時數
        double usageHours = calculateUsageHours(roomId, startDate, endDate);
        
        // 計算可用時數 (營業時間)
        long totalDays = startDate.datesUntil(endDate.plusDays(1)).count();
        double availableHours = totalDays * BUSINESS_HOURS_PER_DAY;

        if (availableHours == 0) {
            return 0.0;
        }

        double usageRate = (usageHours / availableHours) * 100.0;

        return Math.min(usageRate, 100.0); // 確保不超過 100%
    }

    /**
     * 計算所有會議室的整體使用率 (平均值)
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 整體使用率 (0.0 - 100.0)
     */
    public double calculateOverallUsageRate(LocalDate startDate, LocalDate endDate) {
        List<Room> rooms = roomRepository.findAll();
        
        if (rooms.isEmpty()) {
            return 0.0;
        }

        double totalUsageRate = rooms.stream()
                .mapToDouble(room -> calculateRoomUsageRate(room.getId(), startDate, endDate))
                .sum();

        return totalUsageRate / rooms.size();
    }

    /**
     * 計算每個會議室的使用率
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return Map<roomId, usageRate>
     */
    public Map<Long, Double> calculateUsageByRoom(LocalDate startDate, LocalDate endDate) {
        List<Room> rooms = roomRepository.findAll();
        Map<Long, Double> usageByRoom = new HashMap<>();

        for (Room room : rooms) {
            double usageRate = calculateRoomUsageRate(room.getId(), startDate, endDate);
            usageByRoom.put(room.getId(), usageRate);
        }

        return usageByRoom;
    }

    /**
     * 計算單一會議室的使用時數
     * 
     * @param roomId 會議室 ID
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 使用時數
     */
    public double calculateUsageHours(Long roomId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime queryStart = startDate.atTime(BUSINESS_START);
        LocalDateTime queryEnd = endDate.atTime(BUSINESS_END);

        List<Reservation> reservations = reservationRepository.findByRoomIdAndTimeRange(
                roomId, queryStart, queryEnd);

        // 過濾掉已取消的預約
        List<Reservation> activeReservations = reservations.stream()
                .filter(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED)
                .toList();

        // 計算總使用時數
        double totalHours = 0.0;
        for (Reservation reservation : activeReservations) {
            // 只計算營業時間內的部分
            LocalDateTime effectiveStart = maxDateTime(reservation.getStartTime(), queryStart);
            LocalDateTime effectiveEnd = minDateTime(reservation.getEndTime(), queryEnd);

            if (effectiveStart.isBefore(effectiveEnd)) {
                Duration duration = Duration.between(effectiveStart, effectiveEnd);
                totalHours += duration.toMinutes() / 60.0;
            }
        }

        return totalHours;
    }

    /**
     * 計算所有會議室的總使用時數
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 總使用時數
     */
    public double calculateTotalUsageHours(LocalDate startDate, LocalDate endDate) {
        List<Room> rooms = roomRepository.findAll();
        
        return rooms.stream()
                .mapToDouble(room -> calculateUsageHours(room.getId(), startDate, endDate))
                .sum();
    }

    /**
     * 計算單一會議室的預約次數 (排除已取消)
     * 
     * @param roomId 會議室 ID
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 預約次數
     */
    public int countReservations(Long roomId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime queryStart = startDate.atTime(BUSINESS_START);
        LocalDateTime queryEnd = endDate.atTime(BUSINESS_END);

        List<Reservation> reservations = reservationRepository.findByRoomIdAndTimeRange(
                roomId, queryStart, queryEnd);

        return (int) reservations.stream()
                .filter(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED)
                .count();
    }

    /**
     * 計算所有會議室的總預約次數
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 總預約次數
     */
    public int countTotalReservations(LocalDate startDate, LocalDate endDate) {
        List<Room> rooms = roomRepository.findAll();
        
        return rooms.stream()
                .mapToInt(room -> countReservations(room.getId(), startDate, endDate))
                .sum();
    }

    /**
     * 取得每個會議室的詳細統計資料
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 會議室統計資料列表
     */
    public List<RoomUsageStats> calculateRoomUsageStats(LocalDate startDate, LocalDate endDate) {
        List<Room> rooms = roomRepository.findAll();
        
        return rooms.stream()
                .map(room -> RoomUsageStats.builder()
                        .roomId(room.getId())
                        .roomName(room.getName())
                        .location(formatLocation(room))
                        .usageRate(calculateRoomUsageRate(room.getId(), startDate, endDate))
                        .usageHours(calculateUsageHours(room.getId(), startDate, endDate))
                        .reservationCount(countReservations(room.getId(), startDate, endDate))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 計算指定會議室列表的統計資料
     * 
     * @param roomIds 會議室 ID 列表
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 會議室統計資料列表
     */
    public List<RoomUsageStats> calculateRoomUsageStats(
            List<Long> roomIds, LocalDate startDate, LocalDate endDate) {
        
        return roomIds.stream()
                .map(roomId -> roomRepository.findById(roomId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(room -> RoomUsageStats.builder()
                        .roomId(room.getId())
                        .roomName(room.getName())
                        .location(formatLocation(room))
                        .usageRate(calculateRoomUsageRate(room.getId(), startDate, endDate))
                        .usageHours(calculateUsageHours(room.getId(), startDate, endDate))
                        .reservationCount(countReservations(room.getId(), startDate, endDate))
                        .build())
                .collect(Collectors.toList());
    }

    // Helper methods
    
    /**
     * 格式化會議室位置
     */
    private String formatLocation(Room room) {
        StringBuilder location = new StringBuilder();
        if (room.getBuilding() != null && !room.getBuilding().isEmpty()) {
            location.append(room.getBuilding());
        }
        if (room.getFloor() != null && !room.getFloor().isEmpty()) {
            if (location.length() > 0) {
                location.append(" ");
            }
            location.append(room.getFloor());
        }
        return location.toString();
    }
    
    private LocalDateTime maxDateTime(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDateTime minDateTime(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    /**
     * 會議室使用統計資料 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class RoomUsageStats {
        private Long roomId;
        private String roomName;
        private String location;
        private double usageRate;
        private double usageHours;
        private int reservationCount;
    }
}
