package tw.huangcti.imrbs.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.huangcti.imrbs.domain.model.MaintenanceSchedule;

import java.time.LocalDateTime;

/**
 * MaintenanceScheduleJpaEntity - MaintenanceSchedule 的 JPA 實體
 */
@Entity
@Table(name = "maintenance_schedules", indexes = {
        @Index(name = "idx_maintenance_room_time", columnList = "room_id, start_time, end_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceScheduleJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", nullable = false)
    private Long roomId;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "reason", nullable = false, length = 255)
    private String reason;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public MaintenanceSchedule toDomain() {
        return MaintenanceSchedule.builder()
                .id(id)
                .roomId(roomId)
                .startTime(startTime)
                .endTime(endTime)
                .reason(reason)
                .createdBy(createdBy)
                .notes(notes)
                .createdAt(createdAt)
                .build();
    }
    
    public static MaintenanceScheduleJpaEntity fromDomain(MaintenanceSchedule schedule) {
        return MaintenanceScheduleJpaEntity.builder()
                .id(schedule.getId())
                .roomId(schedule.getRoomId())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .reason(schedule.getReason())
                .createdBy(schedule.getCreatedBy())
                .notes(schedule.getNotes())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}
