# IISIGROUP Meeting Room Booking System (imrbs)

本專案為 **IISIGROUP 會議室預約系統**，提供會議室查詢、預約登錄/修改、衝突檢查、預約通知（email）以及會議室管理功能。

## 🏗️ 系統架構
- **後端 (Backend)**：Spring Boot 3.5.0 + Java 21
- **資料儲存**：JSON 檔案（持久化於 `~/.imrbs/data/`）
- **測試框架**：JUnit 5 + Mockito

## 📦 專案結構
```
imrbs/
├── imrbs-core/          # 核心領域邏輯與服務
│   ├── domain/          # 實體模型 (Room, Reservation)
│   ├── repository/      # 儲存介面與 JSON 實作
│   ├── service/         # 業務邏輯服務
│   └── exception/       # 自訂例外
├── imrbs-web/           # Web API 層
│   ├── web/             # REST Controllers
│   └── dto/             # 資料傳輸物件
├── specs/               # 功能規格文件
└── scripts/             # 備份/還原腳本
```

## 🚀 快速開始

### 環境需求
- JDK 21+
- Maven 3.8+

### 建置與執行
```bash
# 建置專案
mvn clean package

# 執行應用程式
mvn spring-boot:run -pl imrbs-web

# 執行測試
mvn test
```

應用程式將在 `http://localhost:8080/api` 啟動。

## 📋 API 端點

### 預約管理
- `POST /api/reservations` - 建立新預約
- `GET /api/reservations` - 查詢所有預約
- `GET /api/reservations/{id}` - 查詢單一預約
- `PUT /api/reservations/{id}` - 更新預約
- `DELETE /api/reservations/{id}` - 取消預約

### 會議室查詢
- `GET /api/rooms` - 查詢所有會議室
- `GET /api/rooms/status?location={location}&date={date}` - 查詢會議室狀態

### 會議室管理 (Admin)
- `POST /api/admin/rooms` - 新增會議室
- `PUT /api/admin/rooms/{id}` - 更新會議室
- `DELETE /api/admin/rooms/{id}` - 刪除會議室

## 🎯 功能特色
- ✅ 會議室預約建立與衝突檢查
- ✅ 預約修改與取消（含通知）
- ✅ 會議室狀態查詢（依地點與日期）
- ✅ 會議室管理（新增/編輯/刪除）
- ✅ JSON 檔案持久化儲存
- ✅ Email 通知（測試替身支援）

## 🗄️ 資料備份與還原

使用 PowerShell 腳本進行備份/還原：

```powershell
# 備份資料
.\scripts\backup-restore.ps1 -Action backup

# 還原資料
.\scripts\backup-restore.ps1 -Action restore
```

## � 初始會議室清單
- **板橋**
  - 6樓 太平洋會議室
  - 7樓 711會議室
  - 7樓 701會議室
- **民生**
  - 301會議室
  - 310會議室

## 🧪 測試範例

### 建立預約
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "room-001",
    "date": "2025-11-05",
    "startTime": "09:00",
    "endTime": "10:00",
    "title": "團隊會議",
    "organizerEmail": "user@example.com",
    "participants": ["member1@example.com", "member2@example.com"]
  }'
```

### 查詢會議室狀態
```bash
curl "http://localhost:8080/api/rooms/status?location=板橋&date=2025-11-05"
```

## 👥 貢獻與維護
本專案由 **cti&eric** 維護與開發。  
如需協作，請遵循 Git Flow 分支管理策略進行開發。

## 📝 技術規格
- Spring Boot 版本：3.5.0
- Java 版本：21
- JSON Schema 版本：1
- 預設資料路徑：`${user.home}/.imrbs/data/`

