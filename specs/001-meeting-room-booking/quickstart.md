# Quickstart Guide: 會議室預約系統

**Feature**: 001-meeting-room-booking  
**Date**: 2025-11-20  
**Phase**: 1 (開發者快速上手指南)

## 概述

本指南幫助開發者快速設定開發環境並開始實作會議室預約系統。

---

## 前置需求

### 必要工具

- **Java**: OpenJDK 25+
- **Maven**: 3.9+
- **Node.js**: 20 LTS+
- **Docker**: 最新穩定版
- **Git**: 2.40+

### IDE 選擇

- **後端**: IntelliJ IDEA Community 或 VS Code (安裝 Java 擴展包)
- **前端**: VS Code (安裝 Volar, ESLint, Prettier)

---

## 環境設定

### 1. Clone 專案

```powershell
git clone https://github.com/your-org/imrbs.git
cd imrbs
git checkout 001-meeting-room-booking
```

### 2. 啟動本地服務 (Docker Compose)

```powershell
docker compose up -d
```

啟動的服務:
- **PostgreSQL**: `localhost:5432` (DB: `imrbs`, User: `admin`, Password: `secret`)
- **Redis**: `localhost:6379`
- **RabbitMQ**: `localhost:5672` (Management UI: `http://localhost:15672`)

### 3. 後端設定

#### 執行資料庫遷移

```powershell
cd imrbs-web
mvn flyway:migrate
```

#### 啟動後端

```powershell
mvn spring-boot:run
```

API 可訪問: `http://localhost:8080/api/v1`

Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. 前端設定

```powershell
cd imrbs-frontend
npm install
npm run dev
```

前端可訪問: `http://localhost:5173`

---

## 開發流程 (TDD)

### 1. Red (編寫失敗的測試)

```java
@Test
void testCreateReservation_Success() {
    // Given
    CreateReservationRequest request = new CreateReservationRequest(
        1L, "部門月會", LocalDateTime.of(2025, 12, 1, 14, 0),
        LocalDateTime.of(2025, 12, 1, 16, 0), "alice@example.com,bob@example.com"
    );
    
    // When & Then
    mockMvc.perform(post("/api/v1/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.meeting_title").value("部門月會"));
}
```

### 2. Green (實作最小程式碼讓測試通過)

```java
@PostMapping("/api/v1/reservations")
public ResponseEntity<Reservation> createReservation(@RequestBody CreateReservationRequest request) {
    Reservation reservation = reservationService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
}
```

### 3. Refactor (重構)

- 提取重複邏輯
- 優化命名
- 增加註釋

---

## 專案結構導覽

```text
imrbs/
├── imrbs-core/           # 核心業務邏輯 (Domain + Application 層)
│   └── src/main/java/tw/huangcti/imrbs/
│       ├── domain/       # 領域模型 (Reservation, Room, User)
│       └── application/  # 用例實作 (CreateReservation, etc.)
├── imrbs-infrastructure/ # 基礎設施實作 (JPA, Redis, RabbitMQ)
├── imrbs-web/            # Web API 層 (Controllers, Security)
└── imrbs-frontend/       # Vue 3 前端
```

---

## 常用指令

### 後端

```powershell
# 執行單元測試
mvn test

# 執行整合測試
mvn verify -P integration-test

# 檢查程式碼品質
mvn sonar:sonar

# 建置 Docker 映像
mvn spring-boot:build-image
```

### 前端

```powershell
# 執行單元測試
npm run test:unit

# 執行 E2E 測試
npm run test:e2e

# 建置生產版本
npm run build

# Lint 檢查
npm run lint
```

---

## 下一步

1. 閱讀 `spec.md` 了解功能需求
2. 閱讀 `data-model.md` 了解資料結構
3. 閱讀 `contracts/api-endpoints.md` 了解 API 設計
4. 執行 `/speckit.tasks` 生成實作任務清單 (tasks.md)
5. 開始 TDD 開發!

---

**Quickstart 完成日期**: 2025-11-20  
**下一階段**: Phase 2 - 生成任務清單 (tasks.md)
