package tw.huangcti.imrbs.repository.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.Reservation;
import tw.huangcti.imrbs.domain.SchemaVersion;
import tw.huangcti.imrbs.repository.ReservationRepository;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * JSON 檔案實作的預約儲存
 */
@Repository
public class JsonReservationRepository implements ReservationRepository {

    @Value("${app.data.reservations}")
    private String dataFilePath;

    private final ObjectMapper objectMapper;
    private final Map<String, Reservation> cache = new ConcurrentHashMap<>();

    public JsonReservationRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
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
            List<Reservation> reservations = objectMapper.convertValue(
                data.get("reservations"),
                new TypeReference<List<Reservation>>() {}
            );
            cache.clear();
            reservations.forEach(r -> cache.put(r.getId(), r));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load reservations from " + dataFilePath, e);
        }
    }

    private synchronized void saveToFile() {
        File file = new File(dataFilePath);
        file.getParentFile().mkdirs();

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("schema_version", SchemaVersion.CURRENT);
            data.put("reservations", new ArrayList<>(cache.values()));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save reservations to " + dataFilePath, e);
        }
    }

    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null || reservation.getId().isEmpty()) {
            reservation.setId(UUID.randomUUID().toString());
        }
        cache.put(reservation.getId(), reservation);
        saveToFile();
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public List<Reservation> findByRoomIdAndDate(String roomId, LocalDate date) {
        return cache.values().stream()
            .filter(r -> r.getRoomId().equals(roomId) && r.getDate().equals(date))
            .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public void deleteById(String id) {
        cache.remove(id);
        saveToFile();
    }
}
