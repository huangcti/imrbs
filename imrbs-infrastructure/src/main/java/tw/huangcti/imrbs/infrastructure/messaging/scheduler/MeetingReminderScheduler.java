package tw.huangcti.imrbs.infrastructure.messaging.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.infrastructure.integration.email.I18nEmailService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 會議提醒排程任務
 * 職責: 每 5 分鐘掃描即將開始的會議 (30 分鐘內), 發送提醒通知
 * 
 * 執行週期: 每 5 分鐘執行一次
 * 查詢範圍: 未來 30 分鐘內開始的會議 (且尚未發送提醒)
 * 
 * 設計考量:
 * 1. 使用 Scheduled 而非 RabbitMQ DLQ (簡化架構, 提高可靠性)
 * 2. reminderSent 旗標防止重複發送
 * 3. 異常隔離: 單一失敗不影響其他提醒
 * 4. 支援多語系: 根據用戶語言偏好發送
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(JavaMailSender.class)
public class MeetingReminderScheduler {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final I18nEmailService emailService;

    /**
     * 排程任務: 每 5 分鐘掃描並發送會議提醒
     * Cron 表達式: "0 0/5 * * * *" 每小時的第 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55 分執行
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void sendReminders() {
        log.info("開始執行會議提醒排程任務");
        
        try {
            // 查詢未來 30 分鐘內開始的會議 (且尚未發送提醒)
            List<Reservation> upcomingReservations = 
                    reservationRepository.findUpcomingReservationsForReminder(30);
            
            if (upcomingReservations.isEmpty()) {
                log.debug("無需發送提醒的會議");
                return;
            }
            
            log.info("找到 {} 筆需要發送提醒的會議", upcomingReservations.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            // 逐一處理每個預約 (異常隔離)
            for (Reservation reservation : upcomingReservations) {
                try {
                    sendReminderForReservation(reservation);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    log.error("發送會議提醒失敗: reservationId={}, error={}", 
                            reservation.getId(), e.getMessage(), e);
                    // 繼續處理下一筆,不中斷整個排程
                }
            }
            
            log.info("會議提醒排程任務完成: 成功={}, 失敗={}", successCount, failureCount);
            
        } catch (Exception e) {
            log.error("會議提醒排程任務執行失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 發送單筆會議提醒
     */
    private void sendReminderForReservation(Reservation reservation) {
        // 1. 檢查是否已發送提醒 (防止重複發送)
        if (Boolean.TRUE.equals(reservation.getReminderSent())) {
            log.debug("跳過已發送提醒的會議: reservationId={}", reservation.getId());
            return;
        }
        
        // 2. 查詢會議室資訊
        Room room = roomRepository.findById(reservation.getRoomId())
                .orElse(null);
        if (room == null) {
            log.warn("會議室不存在,跳過提醒: roomId={}", reservation.getRoomId());
            return;
        }
        
        // 3. 查詢預約者資訊
        User user = userRepository.findById(reservation.getUserId())
                .orElse(null);
        if (user == null) {
            log.warn("用戶不存在,跳過提醒: userId={}", reservation.getUserId());
            return;
        }
        
        // 4. 解析語言偏好
        Locale locale = emailService.parseLocale(user.getLanguagePreference());
        
        // 5. 組合位置字串
        String roomLocation = room.getBuilding() + " " + room.getFloor();
        if (room.getLocationDescription() != null && !room.getLocationDescription().isBlank()) {
            roomLocation += " " + room.getLocationDescription();
        }
        
        // 6. 組合設備字串
        String equipmentStr = room.getEquipment().stream()
                .map(eq -> eq.getName() + (eq.getQuantity() != null && eq.getQuantity() > 1 ? " x" + eq.getQuantity() : ""))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        
        // 7. 發送提醒郵件
        emailService.sendMeetingReminder(
                reservation,
                room.getName(),
                roomLocation,
                equipmentStr,
                room.getCapacity(),
                user.getFullName(),
                user.getEmail(),
                locale
        );
        
        // 8. 更新 reminderSent 旗標
        reservation.setReminderSent(true);
        reservationRepository.save(reservation);
        
        log.info("會議提醒已發送: reservationId={}, userId={}, email={}", 
                reservation.getId(), user.getId(), user.getEmail());
    }

    /**
     * 備註: 為何使用 @Scheduled 而非 RabbitMQ DLQ?
     * 
     * 優勢:
     * 1. 簡化架構: 無需維護 RabbitMQ 延遲隊列和消費者
     * 2. 提高可靠性: 排程任務由 Spring 框架保證執行
     * 3. 靈活查詢: 可動態調整提醒時間窗口 (30 分鐘)
     * 4. 防止重複: reminderSent 旗標比消息去重更簡單
     * 5. 取消處理: 自動過濾已取消的預約 (無需消息取消機制)
     * 
     * 劣勢:
     * 1. 精確度較低: 每 5 分鐘掃描一次 (vs RabbitMQ DLQ 精確到秒)
     * 2. 輪詢開銷: 需定期查詢資料庫 (但實務上開銷可接受)
     * 
     * 結論:
     * 對於會議提醒場景,@Scheduled 的簡單可靠優於 DLQ 的精確度
     * 5 分鐘的掃描間隔對使用者體驗影響微小 (30 分鐘提醒的誤差範圍內)
     */
}
