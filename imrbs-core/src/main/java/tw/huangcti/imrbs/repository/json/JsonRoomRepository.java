package tw.huangcti.imrbs.repository.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.Room;
import tw.huangcti.imrbs.domain.SchemaVersion;
import tw.huangcti.imrbs.repository.RoomRepository;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * JSON 檔案實作的會議室儲存
 */
@Repository
public class JsonRoomRepository implements RoomRepository {

    @Value("${app.data.rooms}")
    private String dataFilePath;

    private final ObjectMapper objectMapper;
    private final Map<String, Room> cache = new ConcurrentHashMap<>();

    public JsonRoomRepository() {
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        loadFromFile();
    }

    private synchronized void loadFromFile() {
        File file = new File(dataFilePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            saveToFile();
            return;
        }

        try {
            Map<String, Object> data = objectMapper.readValue(file, new TypeReference<>() {});
            List<Room> rooms = objectMapper.convertValue(
                data.get("rooms"),
                new TypeReference<List<Room>>() {}
            );
            cache.clear();
            rooms.forEach(r -> cache.put(r.getId(), r));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rooms from " + dataFilePath, e);
        }
    }

    private synchronized void saveToFile() {
        File file = new File(dataFilePath);
        file.getParentFile().mkdirs();

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("schema_version", SchemaVersion.CURRENT);
            data.put("rooms", new ArrayList<>(cache.values()));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save rooms to " + dataFilePath, e);
        }
    }

    @Override
    public Room save(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            room.setId(UUID.randomUUID().toString());
        }
        cache.put(room.getId(), room);
        saveToFile();
        return room;
    }

    @Override
    public Optional<Room> findById(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public List<Room> findByLocation(String location) {
        return cache.values().stream()
            .filter(r -> r.getLocation().equals(location))
            .collect(Collectors.toList());
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public void deleteById(String id) {
        cache.remove(id);
        saveToFile();
    }
}
