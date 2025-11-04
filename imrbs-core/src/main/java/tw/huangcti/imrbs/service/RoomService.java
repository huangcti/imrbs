package tw.huangcti.imrbs.service;

import org.springframework.stereotype.Service;
import tw.huangcti.imrbs.domain.Reservation;
import tw.huangcti.imrbs.domain.Room;
import tw.huangcti.imrbs.repository.ReservationRepository;
import tw.huangcti.imrbs.repository.RoomRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 會議室查詢服務
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * 根據地點查詢會議室
     */
    public List<Room> findByLocation(String location) {
        return roomRepository.findByLocation(location);
    }

    /**
     * 查詢所有會議室
     */
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    /**
     * 查詢指定日期與地點的所有預約（按會議室分組）
     */
    public Map<String, List<Reservation>> getReservationsByRoomAndDate(String location, LocalDate date) {
        List<Room> rooms = location != null && !location.isEmpty()
            ? roomRepository.findByLocation(location)
            : roomRepository.findAll();

        return rooms.stream()
            .collect(Collectors.toMap(
                Room::getId,
                room -> reservationRepository.findByRoomIdAndDate(room.getId(), date)
            ));
    }

    /**
     * 儲存會議室
     */
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    /**
     * 刪除會議室
     */
    public void deleteById(String id) {
        roomRepository.deleteById(id);
    }
}
