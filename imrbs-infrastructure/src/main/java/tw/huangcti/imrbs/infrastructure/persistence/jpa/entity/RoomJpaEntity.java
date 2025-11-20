package tw.huangcti.imrbs.infrastructure.persistence.jpa.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import tw.huangcti.imrbs.domain.model.Room;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomJpaEntity - Room 的 JPA 實體
 * 
 * 描述: 對應資料庫 rooms 表
 * 
 * 設計: Clean Architecture - Infrastructure Layer 實作
 * 職責: 資料庫持久化，處理 JSONB 欄位
 */
@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "idx_room_name", columnList = "name", unique = true),
        @Index(name = "idx_room_capacity", columnList = "capacity"),
        @Index(name = "idx_room_status", columnList = "status"),
        @Index(name = "idx_room_building_floor", columnList = "building, floor")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(name = "floor", nullable = false, length = 10)
    private String floor;
    
    @Column(name = "building", length = 50)
    private String building;
    
    @Column(name = "location_description", length = 255)
    private String locationDescription;
    
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @Type(JsonBinaryType.class)
    @Column(name = "equipment", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<Room.Equipment> equipment = new ArrayList<>();
    
    @Type(JsonBinaryType.class)
    @Column(name = "photos", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> photos = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Room.RoomStatus status;
    
    @Type(JsonBinaryType.class)
    @Column(name = "features", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> features = new ArrayList<>();
    
    @Type(JsonBinaryType.class)
    @Column(name = "booking_rule", columnDefinition = "jsonb")
    private Room.BookingRule bookingRule;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Room.RoomStatus.AVAILABLE;
        }
        if (equipment == null) {
            equipment = new ArrayList<>();
        }
        if (photos == null) {
            photos = new ArrayList<>();
        }
        if (features == null) {
            features = new ArrayList<>();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 轉換為 Domain Model
     * 
     * @return Room domain model
     */
    public Room toDomain() {
        return Room.builder()
                .id(id)
                .name(name)
                .floor(floor)
                .building(building)
                .locationDescription(locationDescription)
                .capacity(capacity)
                .equipment(equipment != null ? new ArrayList<>(equipment) : new ArrayList<>())
                .photos(photos != null ? new ArrayList<>(photos) : new ArrayList<>())
                .status(status)
                .features(features != null ? new ArrayList<>(features) : new ArrayList<>())
                .bookingRule(bookingRule)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
    
    /**
     * 從 Domain Model 轉換
     * 
     * @param room domain model
     * @return RoomJpaEntity
     */
    public static RoomJpaEntity fromDomain(Room room) {
        return RoomJpaEntity.builder()
                .id(room.getId())
                .name(room.getName())
                .floor(room.getFloor())
                .building(room.getBuilding())
                .locationDescription(room.getLocationDescription())
                .capacity(room.getCapacity())
                .equipment(room.getEquipment() != null ? new ArrayList<>(room.getEquipment()) : new ArrayList<>())
                .photos(room.getPhotos() != null ? new ArrayList<>(room.getPhotos()) : new ArrayList<>())
                .status(room.getStatus())
                .features(room.getFeatures() != null ? new ArrayList<>(room.getFeatures()) : new ArrayList<>())
                .bookingRule(room.getBookingRule())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
