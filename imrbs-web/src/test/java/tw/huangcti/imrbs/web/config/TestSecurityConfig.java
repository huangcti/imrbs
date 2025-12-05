package tw.huangcti.imrbs.web.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tw.huangcti.imrbs.application.usecase.CancelReservationUseCase;
import tw.huangcti.imrbs.application.usecase.CreateReservationUseCase;
import tw.huangcti.imrbs.application.usecase.GetReservationUseCase;
import tw.huangcti.imrbs.application.usecase.SyncUserFromSsoUseCase;
import tw.huangcti.imrbs.application.usecase.UpdateReservationUseCase;
import tw.huangcti.imrbs.domain.event.ReservationEventPublisher;
import tw.huangcti.imrbs.domain.repository.MaintenanceScheduleRepository;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.domain.service.CancellationPolicyService;
import tw.huangcti.imrbs.domain.service.ConflictDetectionService;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.repository.*;

import java.util.Optional;

import static org.mockito.Mockito.mock;

/**
 * 測試環境配置
 * 提供測試所需的 Mock Beans
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public RoomRepository roomRepository() {
        return mock(RoomRepository.class);
    }

    @Bean
    @Primary
    public UserRepository userRepository() {
        return mock(UserRepository.class);
    }

    @Bean
    @Primary
    public ReservationRepository reservationRepository() {
        return mock(ReservationRepository.class);
    }

    @Bean
    @Primary
    public MaintenanceScheduleRepository maintenanceScheduleRepository() {
        return mock(MaintenanceScheduleRepository.class);
    }

    @Bean
    @Primary
    public ConflictDetectionService conflictDetectionService() {
        return new ConflictDetectionService(
            reservationRepository(),
            maintenanceScheduleRepository()
        );
    }

    @Bean
    @Primary
    public CancellationPolicyService cancellationPolicyService() {
        return new CancellationPolicyService();
    }

    @Bean
    @Primary
    public Optional<ReservationEventPublisher> optionalEventPublisher() {
        return Optional.empty();
    }

    @Bean
    @Primary
    public CreateReservationUseCase createReservationUseCase() {
        return new CreateReservationUseCase(
            roomRepository(),
            userRepository(),
            reservationRepository(),
            conflictDetectionService()
        );
    }

    @Bean
    @Primary
    public UpdateReservationUseCase updateReservationUseCase() {
        return new UpdateReservationUseCase(
            reservationRepository(),
            conflictDetectionService(),
            optionalEventPublisher()
        );
    }

    @Bean
    @Primary
    public CancelReservationUseCase cancelReservationUseCase() {
        return new CancelReservationUseCase(
            reservationRepository(),
            cancellationPolicyService(),
            optionalEventPublisher()
        );
    }

    @Bean
    @Primary
    public GetReservationUseCase getReservationUseCase() {
        return new GetReservationUseCase(reservationRepository());
    }

    @Bean
    @Primary
    public SyncUserFromSsoUseCase syncUserFromSsoUseCase() {
        return new SyncUserFromSsoUseCase(userRepository());
    }

    // ========== JPA Repositories (Mocked) ==========

    @Bean
    @Primary
    public UserJpaRepository userJpaRepository() {
        return mock(UserJpaRepository.class);
    }

    @Bean
    @Primary
    public RoomJpaRepository roomJpaRepository() {
        return mock(RoomJpaRepository.class);
    }

    @Bean
    @Primary
    public ReservationJpaRepository reservationJpaRepository() {
        return mock(ReservationJpaRepository.class);
    }

    @Bean
    @Primary
    public MaintenanceScheduleJpaRepository maintenanceScheduleJpaRepository() {
        return mock(MaintenanceScheduleJpaRepository.class);
    }

    @Bean
    @Primary
    public NotificationJpaRepository notificationJpaRepository() {
        return mock(NotificationJpaRepository.class);
    }

    @Bean
    @Primary
    public GuestReservationRequestJpaRepository guestReservationRequestJpaRepository() {
        return mock(GuestReservationRequestJpaRepository.class);
    }
}
