package tw.huangcti.imrbs.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.huangcti.imrbs.application.usecase.CreateReservationUseCase;
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
}
