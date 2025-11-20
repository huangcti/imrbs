package tw.huangcti.imrbs.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.Room;
import tw.huangcti.imrbs.domain.repository.RoomRepository;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.RoomJpaEntity;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.repository.RoomJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RoomRepositoryAdapter - RoomRepository 的實作
 * 
 * 描述: 連接 Domain Layer 與 Infrastructure Layer 的適配器
 * 
 * 設計: Clean Architecture - Adapter Pattern
 */
@Component
@RequiredArgsConstructor
public class RoomRepositoryAdapter implements RoomRepository {
    
    private final RoomJpaRepository jpaRepository;
    
    @Override
    public Optional<Room> findById(Long id) {
        return jpaRepository.findById(id)
                .map(RoomJpaEntity::toDomain);
    }
    
    @Override
    public Optional<Room> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(RoomJpaEntity::toDomain);
    }
    
    @Override
    public List<Room> findAllAvailable() {
        return jpaRepository.findByStatus(Room.RoomStatus.AVAILABLE).stream()
                .map(RoomJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Room> findByCapacityGreaterThanEqual(Integer minCapacity) {
        return jpaRepository.findByCapacityGreaterThanEqual(minCapacity).stream()
                .map(RoomJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Room> findByBuildingAndFloor(String building, String floor) {
        return jpaRepository.findByBuildingAndFloor(building, floor).stream()
                .map(RoomJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Room> findAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.findAvailableRooms(startTime, endTime).stream()
                .map(RoomJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Room> findAvailableRoomsByFilters(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer minCapacity,
            List<String> requiredEquipment
    ) {
        // 先查詢可用會議室
        List<Room> availableRooms = findAvailableRooms(startTime, endTime);
        
        // 應用容量過濾
        if (minCapacity != null) {
            availableRooms = availableRooms.stream()
                    .filter(room -> room.hasCapacityFor(minCapacity))
                    .collect(Collectors.toList());
        }
        
        // 應用設備過濾
        if (requiredEquipment != null && !requiredEquipment.isEmpty()) {
            availableRooms = availableRooms.stream()
                    .filter(room -> requiredEquipment.stream()
                            .allMatch(room::hasEquipment))
                    .collect(Collectors.toList());
        }
        
        return availableRooms;
    }
    
    @Override
    public List<Room> findByFeature(String feature) {
        return jpaRepository.findAll().stream()
                .filter(entity -> entity.getFeatures() != null && entity.getFeatures().contains(feature))
                .map(RoomJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Room save(Room room) {
        RoomJpaEntity entity = RoomJpaEntity.fromDomain(room);
        RoomJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
    
    @Override
    public void deleteById(Long id) {
        // 軟刪除: 設置 status = DISABLED
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(Room.RoomStatus.DISABLED);
            jpaRepository.save(entity);
        });
    }
    
    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
    
    @Override
    public List<Room> findAll() {
        return jpaRepository.findAll().stream()
                .map(RoomJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
}
