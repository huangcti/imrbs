package tw.huangcti.imrbs.infrastructure.persistence.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.domain.model.Room;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * T175 [P] Redis 快取服務 - 會議室快取策略
 * 
 * 快取策略:
 * - 會議室清單: 快取 10 分鐘 (ROOM_LIST_TTL)
 * - 單一會議室: 快取 30 分鐘 (ROOM_DETAIL_TTL)
 * - 會議室可用性: 快取 5 分鐘 (AVAILABILITY_TTL)
 * 
 * 快取失效策略:
 * - 會議室 CRUD 操作時主動失效相關快取
 * - 預約建立/修改/取消時失效可用性快取
 * 
 * Key 命名規則:
 * - room:list - 會議室清單
 * - room:{id} - 單一會議室
 * - room:{id}:availability:{date} - 指定日期可用性
 * 
 * @author IMRBS Team
 * @since 2025-01-24
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(RedisTemplate.class)
public class RoomCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // 快取 Key 前綴
    private static final String ROOM_LIST_KEY = "room:list";
    private static final String ROOM_DETAIL_KEY_PREFIX = "room:";
    private static final String AVAILABILITY_KEY_SUFFIX = ":availability:";

    // 快取 TTL (Time To Live)
    private static final Duration ROOM_LIST_TTL = Duration.ofMinutes(10);
    private static final Duration ROOM_DETAIL_TTL = Duration.ofMinutes(30);
    private static final Duration AVAILABILITY_TTL = Duration.ofMinutes(5);

    // ========== 會議室清單快取 ==========

    /**
     * 快取會議室清單
     * 
     * @param rooms 會議室清單
     */
    public void cacheRoomList(List<Room> rooms) {
        try {
            String json = objectMapper.writeValueAsString(rooms);
            redisTemplate.opsForValue().set(ROOM_LIST_KEY, json, ROOM_LIST_TTL);
            log.debug("會議室清單已快取: count={}", rooms.size());
        } catch (Exception e) {
            log.warn("快取會議室清單失敗: {}", e.getMessage());
        }
    }

    /**
     * 取得快取的會議室清單
     * 
     * @return Optional<List<Room>> 快取的會議室清單
     */
    public Optional<List<Room>> getCachedRoomList() {
        try {
            String json = redisTemplate.opsForValue().get(ROOM_LIST_KEY);
            if (json != null) {
                List<Room> rooms = objectMapper.readValue(json, new TypeReference<List<Room>>() {});
                log.debug("命中會議室清單快取: count={}", rooms.size());
                return Optional.of(rooms);
            }
        } catch (Exception e) {
            log.warn("讀取會議室清單快取失敗: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * 失效會議室清單快取
     */
    public void evictRoomListCache() {
        try {
            redisTemplate.delete(ROOM_LIST_KEY);
            log.debug("會議室清單快取已失效");
        } catch (Exception e) {
            log.warn("失效會議室清單快取失敗: {}", e.getMessage());
        }
    }

    // ========== 單一會議室快取 ==========

    /**
     * 快取單一會議室
     * 
     * @param room 會議室
     */
    public void cacheRoom(Room room) {
        try {
            String key = ROOM_DETAIL_KEY_PREFIX + room.getId();
            String json = objectMapper.writeValueAsString(room);
            redisTemplate.opsForValue().set(key, json, ROOM_DETAIL_TTL);
            log.debug("會議室已快取: id={}", room.getId());
        } catch (Exception e) {
            log.warn("快取會議室失敗: roomId={}, error={}", room.getId(), e.getMessage());
        }
    }

    /**
     * 取得快取的會議室
     * 
     * @param roomId 會議室 ID
     * @return Optional<Room> 快取的會議室
     */
    public Optional<Room> getCachedRoom(Long roomId) {
        try {
            String key = ROOM_DETAIL_KEY_PREFIX + roomId;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                Room room = objectMapper.readValue(json, Room.class);
                log.debug("命中會議室快取: id={}", roomId);
                return Optional.of(room);
            }
        } catch (Exception e) {
            log.warn("讀取會議室快取失敗: roomId={}, error={}", roomId, e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * 失效單一會議室快取
     * 
     * @param roomId 會議室 ID
     */
    public void evictRoomCache(Long roomId) {
        try {
            String key = ROOM_DETAIL_KEY_PREFIX + roomId;
            redisTemplate.delete(key);
            log.debug("會議室快取已失效: id={}", roomId);
        } catch (Exception e) {
            log.warn("失效會議室快取失敗: roomId={}, error={}", roomId, e.getMessage());
        }
    }

    // ========== 可用性快取 ==========

    /**
     * 快取會議室可用性
     * 
     * @param roomId       會議室 ID
     * @param date         日期
     * @param availability 可用性資料 (JSON 字串)
     */
    public void cacheAvailability(Long roomId, LocalDate date, String availability) {
        try {
            String key = ROOM_DETAIL_KEY_PREFIX + roomId + AVAILABILITY_KEY_SUFFIX + date;
            redisTemplate.opsForValue().set(key, availability, AVAILABILITY_TTL);
            log.debug("會議室可用性已快取: roomId={}, date={}", roomId, date);
        } catch (Exception e) {
            log.warn("快取會議室可用性失敗: roomId={}, date={}, error={}", roomId, date, e.getMessage());
        }
    }

    /**
     * 取得快取的可用性
     * 
     * @param roomId 會議室 ID
     * @param date   日期
     * @return Optional<String> 快取的可用性資料
     */
    public Optional<String> getCachedAvailability(Long roomId, LocalDate date) {
        try {
            String key = ROOM_DETAIL_KEY_PREFIX + roomId + AVAILABILITY_KEY_SUFFIX + date;
            String availability = redisTemplate.opsForValue().get(key);
            if (availability != null) {
                log.debug("命中會議室可用性快取: roomId={}, date={}", roomId, date);
                return Optional.of(availability);
            }
        } catch (Exception e) {
            log.warn("讀取會議室可用性快取失敗: roomId={}, date={}, error={}", roomId, date, e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * 失效會議室可用性快取 (指定日期)
     * 
     * @param roomId 會議室 ID
     * @param date   日期
     */
    public void evictAvailabilityCache(Long roomId, LocalDate date) {
        try {
            String key = ROOM_DETAIL_KEY_PREFIX + roomId + AVAILABILITY_KEY_SUFFIX + date;
            redisTemplate.delete(key);
            log.debug("會議室可用性快取已失效: roomId={}, date={}", roomId, date);
        } catch (Exception e) {
            log.warn("失效會議室可用性快取失敗: roomId={}, date={}, error={}", roomId, date, e.getMessage());
        }
    }

    /**
     * 失效會議室所有可用性快取 (使用模式匹配)
     * 
     * @param roomId 會議室 ID
     */
    public void evictAllAvailabilityCache(Long roomId) {
        try {
            String pattern = ROOM_DETAIL_KEY_PREFIX + roomId + AVAILABILITY_KEY_SUFFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("會議室所有可用性快取已失效: roomId={}, count={}", roomId, keys.size());
            }
        } catch (Exception e) {
            log.warn("失效會議室所有可用性快取失敗: roomId={}, error={}", roomId, e.getMessage());
        }
    }

    // ========== 批量操作 ==========

    /**
     * 失效所有會議室相關快取
     * 用於系統重置或大規模更新
     */
    public void evictAllRoomCaches() {
        try {
            // 刪除清單快取
            redisTemplate.delete(ROOM_LIST_KEY);

            // 刪除所有會議室快取
            Set<String> roomKeys = redisTemplate.keys(ROOM_DETAIL_KEY_PREFIX + "*");
            if (roomKeys != null && !roomKeys.isEmpty()) {
                redisTemplate.delete(roomKeys);
                log.info("所有會議室快取已失效: count={}", roomKeys.size());
            }
        } catch (Exception e) {
            log.error("失效所有會議室快取失敗: {}", e.getMessage());
        }
    }

    // ========== 快取統計 ==========

    /**
     * 檢查快取是否存在
     * 
     * @param key 快取 Key
     * @return true 如果存在
     */
    public boolean hasCache(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 取得快取 TTL
     * 
     * @param key 快取 Key
     * @return TTL (秒)，-1 表示永不過期，-2 表示不存在
     */
    public long getCacheTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -2;
        } catch (Exception e) {
            return -2;
        }
    }
}
