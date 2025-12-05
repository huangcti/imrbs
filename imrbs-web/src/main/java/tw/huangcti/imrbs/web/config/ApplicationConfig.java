package tw.huangcti.imrbs.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.huangcti.imrbs.application.usecase.*;
import tw.huangcti.imrbs.domain.event.ReservationEventPublisher;
import tw.huangcti.imrbs.domain.repository.GuestReservationRequestRepository;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.domain.service.CancellationPolicyService;
import tw.huangcti.imrbs.domain.service.ConflictDetectionService;

import java.util.Optional;

/**
 * ApplicationConfig - 應用層配置
 * 
 * 配置 Use Cases 和 Domain Services 的 Spring Beans
 * 將 Domain Layer 和 Application Layer 整合到 Spring 容器
 */
@Configuration
public class ApplicationConfig {
    
    /**
     * 取消政策服務
     */
    @Bean
    public CancellationPolicyService cancellationPolicyService() {
        return new CancellationPolicyService();
    }
    
    /**
     * 預約變更事件發布者 (Optional - 僅在有 RabbitMQ 配置時生效)
     */
    @Bean
    public Optional<ReservationEventPublisher> optionalEventPublisher() {
        // 暫時返回空 Optional,因為 RabbitMQ EventPublisher 尚未配置
        return Optional.empty();
    }
    
    // ========== Use Cases ==========
    
    @Bean
    public CreateReservationUseCase createReservationUseCase(
            RoomRepository roomRepository,
            UserRepository userRepository,
            ReservationRepository reservationRepository,
            ConflictDetectionService conflictDetectionService) {
        return new CreateReservationUseCase(roomRepository, userRepository, reservationRepository, conflictDetectionService);
    }
    
    @Bean
    public UpdateReservationUseCase updateReservationUseCase(
            ReservationRepository reservationRepository,
            ConflictDetectionService conflictDetectionService,
            Optional<ReservationEventPublisher> eventPublisher) {
        return new UpdateReservationUseCase(reservationRepository, conflictDetectionService, eventPublisher);
    }
    
    @Bean
    public CancelReservationUseCase cancelReservationUseCase(
            ReservationRepository reservationRepository,
            CancellationPolicyService cancellationPolicyService,
            Optional<ReservationEventPublisher> eventPublisher) {
        return new CancelReservationUseCase(reservationRepository, cancellationPolicyService, eventPublisher);
    }
    
    @Bean
    public GetReservationUseCase getReservationUseCase(ReservationRepository reservationRepository) {
        return new GetReservationUseCase(reservationRepository);
    }
    
    @Bean
    public SyncUserFromSsoUseCase syncUserFromSsoUseCase(UserRepository userRepository) {
        return new SyncUserFromSsoUseCase(userRepository);
    }
    
    // ========== Guest Request Use Cases ==========
    
    @Bean
    public CreateGuestRequestUseCase createGuestRequestUseCase(
            RoomRepository roomRepository,
            GuestReservationRequestRepository guestReservationRequestRepository,
            ConflictDetectionService conflictDetectionService) {
        return new CreateGuestRequestUseCase(roomRepository, guestReservationRequestRepository, conflictDetectionService);
    }
    
    @Bean
    public ApproveGuestRequestUseCase approveGuestRequestUseCase(
            GuestReservationRequestRepository guestReservationRequestRepository,
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            UserRepository userRepository,
            ConflictDetectionService conflictDetectionService) {
        return new ApproveGuestRequestUseCase(
                guestReservationRequestRepository,
                reservationRepository,
                roomRepository,
                userRepository,
                conflictDetectionService);
    }
    
    @Bean
    public RejectGuestRequestUseCase rejectGuestRequestUseCase(
            GuestReservationRequestRepository guestReservationRequestRepository,
            UserRepository userRepository) {
        return new RejectGuestRequestUseCase(guestReservationRequestRepository, userRepository);
    }
    
    // ========== User Profile Use Cases ==========
    
    @Bean
    public UpdateLanguagePreferenceUseCase updateLanguagePreferenceUseCase(
            UserRepository userRepository) {
        return new UpdateLanguagePreferenceUseCase(userRepository);
    }
    
    @Bean
    public GetUserProfileUseCase getUserProfileUseCase(
            UserRepository userRepository) {
        return new GetUserProfileUseCase(userRepository);
    }
}
