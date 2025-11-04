package tw.huangcti.imrbs.web;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.huangcti.imrbs.domain.Room;
import tw.huangcti.imrbs.service.RoomService;

/**
 * 會議室管理控制器
 */
@RestController
@RequestMapping("/admin/rooms")
public class RoomAdminController {

    private static final Logger log = LoggerFactory.getLogger(RoomAdminController.class);

    private final RoomService roomService;

    public RoomAdminController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * 新增會議室
     */
    @PostMapping
    public ResponseEntity<Room> createRoom(@Valid @RequestBody Room room) {
        log.info("新增會議室: name={}, location={}", room.getName(), room.getLocation());
        Room created = roomService.save(room);
        log.info("會議室新增成功: id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新會議室
     */
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable String id, @Valid @RequestBody Room room) {
        log.info("更新會議室: id={}", id);
        room.setId(id);
        Room updated = roomService.save(room);
        log.info("會議室更新成功: id={}", updated.getId());
        return ResponseEntity.ok(updated);
    }

    /**
     * 刪除會議室
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String id) {
        log.info("刪除會議室: id={}", id);
        roomService.deleteById(id);
        log.info("會議室刪除成功: id={}", id);
        return ResponseEntity.noContent().build();
    }
}
