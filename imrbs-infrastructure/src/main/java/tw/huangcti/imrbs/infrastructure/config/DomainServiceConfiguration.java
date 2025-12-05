package tw.huangcti.imrbs.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.huangcti.imrbs.application.usecase.CreateMaintenanceScheduleUseCase;
import tw.huangcti.imrbs.application.usecase.CreateReservationUseCase;
import tw.huangcti.imrbs.application.usecase.CreateRoomUseCase;
import tw.huangcti.imrbs.application.usecase.DeleteRoomUseCase;
import tw.huangcti.imrbs.application.usecase.UpdateRoomUseCase;
import tw.huangcti.imrbs.domain.repository.MaintenanceScheduleRepository;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.domain.service.ConflictDetectionService;
import tw.huangcti.imrbs.domain.service.RoomAvailabilityService;

/**
 * Domain Service Configuration
 * 
 * 將 Domain 層的 POJO 服務註冊為 Spring Bean
 * 這些服務不包含框架依賴,需要手動註冊
 */
@Configuration
public class DomainServiceConfiguration {
    
    /**
     * 會議室可用性服務
     */
    @Bean
    public RoomAvailabilityService roomAvailabilityService(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            MaintenanceScheduleRepository maintenanceScheduleRepository
    ) {
        return new RoomAvailabilityService(
                roomRepository,
                reservationRepository,
                maintenanceScheduleRepository
        );
    }
    
    /**
     * 衝突檢測服務
     */
    @Bean
    public ConflictDetectionService conflictDetectionService(
            ReservationRepository reservationRepository,
            MaintenanceScheduleRepository maintenanceScheduleRepository
    ) {
        return new ConflictDetectionService(
                reservationRepository,
                maintenanceScheduleRepository
        );
    }
    
    /**
     * 建立預約用例
     */
    @Bean
    public CreateReservationUseCase createReservationUseCase(
            RoomRepository roomRepository,
            UserRepository userRepository,
            ReservationRepository reservationRepository,
            ConflictDetectionService conflictDetectionService
    ) {
        return new CreateReservationUseCase(
                roomRepository,
                userRepository,
                reservationRepository,
                conflictDetectionService
        );
    }
    
    /**
     * T114 [P] [US4] 創建會議室用例
     */
    @Bean
    public CreateRoomUseCase createRoomUseCase(
            RoomRepository roomRepository
    ) {
        return new CreateRoomUseCase(roomRepository);
    }
    
    /**
     * T115 [P] [US4] 更新會議室用例
     */
    @Bean
    public UpdateRoomUseCase updateRoomUseCase(
            RoomRepository roomRepository
    ) {
        return new UpdateRoomUseCase(roomRepository);
    }
    
    /**
     * T116 [P] [US4] 刪除會議室用例
     */
    @Bean
    public DeleteRoomUseCase deleteRoomUseCase(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository
    ) {
        return new DeleteRoomUseCase(roomRepository, reservationRepository);
    }
    
    /**
     * T117 [P] [US4] 創建維護時段用例
     */
    @Bean
    public CreateMaintenanceScheduleUseCase createMaintenanceScheduleUseCase(
            MaintenanceScheduleRepository maintenanceScheduleRepository,
            RoomRepository roomRepository,
            ReservationRepository reservationRepository
    ) {
        return new CreateMaintenanceScheduleUseCase(
                maintenanceScheduleRepository,
                roomRepository,
                reservationRepository
        );
    }
}
