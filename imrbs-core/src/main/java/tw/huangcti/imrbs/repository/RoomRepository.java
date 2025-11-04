package tw.huangcti.imrbs.repository;

import tw.huangcti.imrbs.domain.Room;

import java.util.List;
import java.util.Optional;

/**
 * 會議室儲存介面
 */
public interface RoomRepository {
    
    /**
     * 儲存會議室
     */
    Room save(Room room);
    
    /**
     * 根據 ID 查詢會議室
     */
    Optional<Room> findById(String id);
    
    /**
     * 根據地點查詢會議室
     */
    List<Room> findByLocation(String location);
    
    /**
     * 查詢所有會議室
     */
    List<Room> findAll();
    
    /**
     * 刪除會議室
     */
    void deleteById(String id);
}
