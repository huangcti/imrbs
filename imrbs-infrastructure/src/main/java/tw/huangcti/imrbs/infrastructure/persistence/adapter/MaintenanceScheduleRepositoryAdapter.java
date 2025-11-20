package tw.huangcti.imrbs.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.model.MaintenanceSchedule;
import tw.huangcti.imrbs.domain.repository.MaintenanceScheduleRepository;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.MaintenanceScheduleJpaEntity;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.repository.MaintenanceScheduleJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MaintenanceScheduleRepositoryAdapter - MaintenanceSchedule Repository 的 JPA 實作
 */
@Repository
@RequiredArgsConstructor
public class MaintenanceScheduleRepositoryAdapter implements MaintenanceScheduleRepository {
    
    private final MaintenanceScheduleJpaRepository jpaRepository;
    
    @Override
    public MaintenanceSchedule save(MaintenanceSchedule schedule) {
        MaintenanceScheduleJpaEntity entity = MaintenanceScheduleJpaEntity.fromDomain(schedule);
        MaintenanceScheduleJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
    
    @Override
    public Optional<MaintenanceSchedule> findById(Long id) {
        return jpaRepository.findById(id)
                .map(MaintenanceScheduleJpaEntity::toDomain);
    }
    
    @Override
    public List<MaintenanceSchedule> findByRoomId(Long roomId) {
        return jpaRepository.findByRoomId(roomId).stream()
                .map(MaintenanceScheduleJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MaintenanceSchedule> findByRoomIdAndTimeRange(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.findByRoomIdAndTimeRange(roomId, startTime, endTime).stream()
                .map(MaintenanceScheduleJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MaintenanceSchedule> findUpcomingMaintenances() {
        LocalDateTime now = LocalDateTime.now();
        return jpaRepository.findUpcomingMaintenances(now).stream()
                .map(MaintenanceScheduleJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeScheduleId) {
        return jpaRepository.hasConflict(roomId, startTime, endTime, excludeScheduleId);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
