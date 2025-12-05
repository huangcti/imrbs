package tw.huangcti.imrbs.web.mapper;

import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.MaintenanceSchedule;
import tw.huangcti.imrbs.web.dto.MaintenanceScheduleDTO;

/**
 * MaintenanceSchedule Mapper
 * 職責: MaintenanceSchedule 領域模型與 DTO 之間的轉換
 */
@Component
public class MaintenanceScheduleMapper {

    /**
     * 將領域模型轉換為 DTO
     */
    public MaintenanceScheduleDTO toDTO(MaintenanceSchedule maintenanceSchedule) {
        if (maintenanceSchedule == null) {
            return null;
        }

        return MaintenanceScheduleDTO.builder()
                .id(maintenanceSchedule.getId())
                .roomId(maintenanceSchedule.getRoomId())
                .startTime(maintenanceSchedule.getStartTime())
                .endTime(maintenanceSchedule.getEndTime())
                .reason(maintenanceSchedule.getReason())
                .notes(maintenanceSchedule.getNotes())
                .createdBy(maintenanceSchedule.getCreatedBy())
                .createdAt(maintenanceSchedule.getCreatedAt())
                .build();
    }

    /**
     * 將 DTO 轉換為領域模型
     */
    public MaintenanceSchedule toEntity(MaintenanceScheduleDTO dto) {
        if (dto == null) {
            return null;
        }

        return MaintenanceSchedule.builder()
                .id(dto.id())
                .roomId(dto.roomId())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .reason(dto.reason())
                .notes(dto.notes())
                .createdBy(dto.createdBy())
                .createdAt(dto.createdAt())
                .build();
    }
}
