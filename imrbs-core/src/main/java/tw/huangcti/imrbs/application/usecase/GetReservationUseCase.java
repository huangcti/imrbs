package tw.huangcti.imrbs.application.usecase;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;

import java.util.Optional;

/**
 * GetReservationUseCase - 查詢預約 Use Case
 * 
 * 負責查詢單一預約或預約清單
 * 
 * Clean Architecture: Application Layer (框架無關)
 */
@RequiredArgsConstructor
public class GetReservationUseCase {
    
    private final ReservationRepository reservationRepository;
    
    /**
     * 根據 ID 查詢預約
     * 
     * @param reservationId 預約 ID
     * @return 預約資訊
     */
    public Optional<Reservation> execute(Long reservationId) {
        return reservationRepository.findById(reservationId);
    }
}
