# JSON 資料結構與持久化研究

## 1. Schema 設計

### Room Schema (rooms.json)
```json
{
  "schema_version": "1",
  "last_updated": "2025-11-03T10:00:00Z",
  "rooms": [
    {
      "id": "string",
      "name": "string",
      "location": "板橋|民生",
      "floor": "string",
      "capacity": "number",
      "features": ["投影機", "白板"],
      "status": "active|maintenance",
      "created_at": "ISO-8601",
      "updated_at": "ISO-8601"
    }
  ]
}
```

### Reservation Schema (reservations.json)
```json
{
  "schema_version": "1",
  "last_updated": "2025-11-03T10:00:00Z",
  "reservations": [
    {
      "id": "string",
      "room_id": "string",
      "title": "string",
      "organizer_email": "string",
      "participants": ["string"],
      "date": "YYYY-MM-DD",
      "start_time": "HH:mm",
      "end_time": "HH:mm",
      "status": "confirmed|cancelled",
      "created_at": "ISO-8601",
      "updated_at": "ISO-8601",
      "version": "number"
    }
  ]
}
```

## 2. 檔案存取策略

### 讀取機制
- 使用 Java NIO.2 的 `WatchService` 監控檔案變更
- 實作檔案鎖定機制避免併發寫入
- 使用 Jackson ObjectMapper 處理 JSON 序列化

### 寫入策略
```java
public class JsonStorage<T> {
    private final Path filePath;
    private final FileChannel channel;
    private final ObjectMapper mapper;

    public void write(T data) throws IOException {
        try (FileLock lock = channel.lock()) {
            // 先寫到暫存檔
            Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            mapper.writeValue(tempFile.toFile(), data);
            
            // 原子性替換
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, 
                      StandardCopyOption.ATOMIC_MOVE);
        }
    }
}
```

## 3. 備份機制

### 自動備份策略
- 每次寫入前建立暫時備份
- 定期（每日）完整備份
- 保留最近 7 天的備份

### 備份檔案結構
```
data/
├── rooms.json
├── reservations.json
└── backups/
    ├── YYYY-MM-DD/
    │   ├── rooms.json
    │   └── reservations.json
    └── latest/
        ├── rooms.json
        └── reservations.json
```

## 4. 版本控制與遷移

### 版本號管理
- 使用語意化版本 (MAJOR.MINOR.PATCH)
- 每個實體獨立版本號
- Schema 版本與資料版本分開追蹤

### 遷移工具
```java
@Service
public class JsonSchemaMigrator {
    private static final Map<String, Consumer<JsonNode>> MIGRATIONS = Map.of(
        "1.0.0->1.1.0", node -> {
            // 遷移邏輯
        }
    );

    public void migrate(Path jsonFile, String targetVersion) {
        // 1. 讀取當前版本
        // 2. 決定遷移路徑
        // 3. 執行遷移
        // 4. 驗證結果
    }
}
```

## 5. 效能考量

### 快取策略
- 使用 Caffeine 快取會議室資料（不常變動）
- 預約資料依時段做時間窗格快取
- 衝突檢查使用記憶體內時段索引

### 監控指標
- 檔案讀寫時間
- 快取命中率
- 併發寫入等待時間
- 檔案大小變化

## 6. 測試策略

### 單元測試
```java
@Test
void whenWriteAndRead_thenDataIsConsistent() {
    Room room = new Room("R1", "太平洋會議室", "板橋", "6F");
    storage.write(room);
    Room read = storage.read("R1");
    assertEquals(room, read);
}
```

### 整合測試
```java
@Test
void whenConcurrentWrites_thenDataIsConsistent() {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    // 模擬多執行緒寫入
    // 驗證資料一致性
}
```

## 7. 安全性

### 檔案權限
- 使用系統使用者執行
- 限制檔案目錄存取權限
- 備份檔案加密

### 資料驗證
- Schema 驗證
- 資料完整性檢查
- 版本號檢查

## 結論

選用 JSON 檔案儲存具備以下優勢：
1. 符合憲章規範
2. 便於人工讀寫與偵錯
3. 容易備份與版本控制
4. 適合中小規模資料量

建議配套措施：
1. 實作完整的備份機制
2. 加入檔案監控
3. 建立效能指標
4. 規劃遷移工具