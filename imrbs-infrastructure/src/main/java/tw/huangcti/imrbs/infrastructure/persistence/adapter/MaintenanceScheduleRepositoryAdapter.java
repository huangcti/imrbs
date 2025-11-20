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
    public List<MaintenanceSchedule> findByCreatedBy(Long createdBy) {
        return jpaRepository.findAll().stream()
                .map(MaintenanceScheduleJpaEntity::toDomain)
                .filter(m -> createdBy.equals(m.getCreatedBy()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MaintenanceSchedule> findByRoomIdAndTimeRange(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.findByRoomIdAndTimeRange(roomId, startTime, endTime).stream()
                .map(MaintenanceScheduleJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasMaintenanceInTimeRange(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<MaintenanceSchedule> maintenances = findByRoomIdAndTimeRange(roomId, startTime, endTime);
        return !maintenances.isEmpty();
    }
    
    public List<MaintenanceSchedule> findUpcomingMaintenances() {
        LocalDateTime now = LocalDateTime.now();
        return jpaRepository.findUpcomingMaintenances(now).stream()
                .map(MaintenanceScheduleJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    public boolean hasConflict(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeScheduleId) {
        return jpaRepository.hasConflict(roomId, startTime, endTime, excludeScheduleId);
    }    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<MaintenanceSchedule> findAll() {
        return jpaRepository.findAll().stream()
                .map(MaintenanceScheduleJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceSchedule> findOngoingMaintenance() {
        LocalDateTime now = LocalDateTime.now();
        return jpaRepository.findAll().stream()
                .map(MaintenanceScheduleJpaEntity::toDomain)
                .filter(m -> m.getStartTime().isBefore(now) && m.getEndTime().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceSchedule> findUpcomingMaintenance(int hoursFromNow) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(hoursFromNow);
        return jpaRepository.findAll().stream()
                .map(MaintenanceScheduleJpaEntity::toDomain)
                .filter(m -> m.getStartTime().isAfter(now) && m.getStartTime().isBefore(threshold))
                .collect(Collectors.toList());
    }
}
