package tw.huangcti.imrbs.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.huangcti.imrbs.domain.Reservation;
import tw.huangcti.imrbs.domain.Room;
import tw.huangcti.imrbs.service.RoomService;
import tw.huangcti.imrbs.web.dto.RoomStatusResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 會議室查詢控制器
 */
@RestController
@RequestMapping("/rooms")
public class RoomQueryController {

    private final RoomService roomService;

    public RoomQueryController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * 查詢會議室狀態
     */
    @GetMapping("/status")
    public ResponseEntity<RoomStatusResponse> getRoomStatus(
            @RequestParam(required = false) String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Room> rooms = location != null && !location.isEmpty()
            ? roomService.findByLocation(location)
            : roomService.findAll();

        Map<String, List<Reservation>> reservationsByRoom = 
            roomService.getReservationsByRoomAndDate(location, date);

        List<RoomStatusResponse.RoomStatusItem> roomItems = rooms.stream()
            .map(room -> {
                List<Reservation> reservations = reservationsByRoom.getOrDefault(room.getId(), new ArrayList<>());
                List<RoomStatusResponse.TimeSlot> timeSlots = reservations.stream()
                    .map(r -> RoomStatusResponse.TimeSlot.builder()
                        .startTime(r.getStartTime().toString())
                        .endTime(r.getEndTime().toString())
                        .status(r.getStatus().name())
                        .reservationId(r.getId())
                        .title(r.getTitle())
                        .build())
                    .collect(Collectors.toList());

                return RoomStatusResponse.RoomStatusItem.builder()
                    .roomId(room.getId())
                    .roomName(room.getName())
                    .floor(room.getFloor())
                    .timeSlots(timeSlots)
                    .build();
            })
            .collect(Collectors.toList());

        RoomStatusResponse response = RoomStatusResponse.builder()
            .date(date)
            .location(location)
            .rooms(roomItems)
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 查詢所有會議室
     */
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.findAll());
    }
}
