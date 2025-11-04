---

description: "Task list for imrbs 會議室預約系統"
---

# Tasks: imrbs 會議室預約系統

**Input**: Design documents from `/specs/dev/`
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: 依憲章與 NFR-002，衝突檢查邏輯必須具單元/整合測試。其餘測試視需求與風險增列。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 初始化 JSON 儲存與設定,確保可在本機立即執行

- [x] T001 建立 JSON 儲存目錄與空白檔案於 imrbs-core/src/main/resources/data/{reservations.json, rooms.json}
- [x] T002 [P] 在 `imrbs-web/src/main/resources/application.yml` 新增 storage 路徑設定與基本屬性（如 server.servlet.context-path: /api）
- [x] T003 [P] 建立全域例外處理骨架於 `imrbs-web/src/main/java/tw/huangcti/imrbs/web/GlobalExceptionHandler.java`
- [x] T004 [P] 建立驗證訊息 resource（若需本地化）於 `imrbs-web/src/main/resources/messages.properties`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 先完成跨故事共用的核心結構。

- [x] T005 [P] 建立領域實體 Room 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/domain/Room.java`
- [x] T006 [P] 建立領域實體 Reservation 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/domain/Reservation.java`
- [x] T007 [P] 建立常數與版本資訊 `SchemaVersion` 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/domain/SchemaVersion.java`
- [x] T008 [P] 建立時間區段驗證工具 `TimeRangeValidator` 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/util/TimeRangeValidator.java`
- [x] T009 定義儲存介面 ReservationRepository 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/repository/ReservationRepository.java`
- [x] T010 [P] JSON 實作 `JsonReservationRepository` 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/repository/json/JsonReservationRepository.java`（讀寫 resources/data/reservations.json）
- [x] T011 [P] 定義儲存介面 RoomRepository 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/repository/RoomRepository.java`
- [x] T012 [P] JSON 實作 `JsonRoomRepository` 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/repository/json/JsonRoomRepository.java`（讀寫 resources/data/rooms.json）
- [x] T013 建立衝突檢查服務 `ConflictChecker` 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/service/ConflictChecker.java`
- [x] T014 [P] 建立 EmailService 介面與 InMemoryEmailSender（測試替身）於 `imrbs-core/src/main/java/tw/huangcti/imrbs/service/email`
- [x] T015 建立應用層服務 `ReservationService` 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/service/ReservationService.java`（依賴 Repository、ConflictChecker、EmailService）
- [x] T016 [P] 建立例外型別（ValidationException, ConflictException, NotFoundException）於 `imrbs-core/src/main/java/tw/huangcti/imrbs/exception`
- [x] T017 [P] 為 ConflictChecker 撰寫單元測試於 `imrbs-core/src/test/java/tw/huangcti/imrbs/service/ConflictCheckerTest.java`
- [x] T018 為 ReservationService 建立整合測試（使用 JSON repository + email mock）於 `imrbs-core/src/test/java/tw/huangcti/imrbs/service/ReservationServiceIT.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - 建立預約（P1） 🎯 MVP

**Goal**: 使用者可建立預約，系統執行衝突檢查、寫入 JSON、寄送確認 email。

**Independent Test**: 呼叫 POST /api/reservations 建立成功；衝突情境被正確拒絕；JSON 有寫入；email mock 收到事件。

### Implementation for User Story 1

- [x] T019 [P] [US1] 建立 DTO CreateReservationRequest/Response 於 `imrbs-web/src/main/java/tw/huangcti/imrbs/web/dto`
- [x] T020 [P] [US1] 建立 DTO Mapper（或在 Service 內處理）於 `imrbs-web/src/main/java/tw/huangcti/imrbs/web/mapper`
- [x] T021 [US1] ReservationService 實作 `createReservation(...)` 完成寫入、衝突檢查、email 通知於 `imrbs-core/src/main/java/tw/huangcti/imrbs/service/ReservationService.java`
- [x] T022 [US1] 建立 Controller `ReservationController` 於 `imrbs-web/src/main/java/tw/huangcti/imrbs/web/ReservationController.java`，新增 `POST /reservations`
- [x] T023 [US1] 加入輸入驗證（Bean Validation）與錯誤回應對應 `GlobalExceptionHandler`
- [x] T024 [US1] 新增操作紀錄與關鍵資訊 logging

**Checkpoint**: US1 功能可獨立驗證，構成 MVP

---

## Phase 4: User Story 2 - 修改/取消預約（P1）

**Goal**: 預約者可修改或取消，時間異動需再次衝突檢查，並寄送通知。

**Independent Test**: PUT /api/reservations/{id} 可修改成功/衝突被拒；DELETE /api/reservations/{id} 取消成功並標記狀態。

### Implementation for User Story 2

- [x] T025 [P] [US2] 建立 DTO UpdateReservationRequest 於 `imrbs-web/src/main/java/tw/huangcti/imrbs/web/dto`
- [x] T026 [US2] ReservationService 實作 `updateReservation(id, ...)` 與 `cancelReservation(id)` 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/service/ReservationService.java`
- [x] T027 [US2] 在 `ReservationController` 新增 `PUT /reservations/{id}` 與 `DELETE /reservations/{id}`
- [x] T028 [US2] 更新 email mock：修改/取消通知事件

---

## Phase 5: User Story 3 - 檢視會議室清單與狀態（P1）

**Goal**: 顯示指定日期/地點的會議室占用狀態。

**Independent Test**: GET /api/rooms/status?date=YYYY-MM-DD&location=板橋 可回傳每間會議室的空閒/已預約狀態。

### Implementation for User Story 3

- [x] T029 [P] [US3] 建立查詢 DTO（RoomStatus, RoomStatusItem）於 `imrbs-web/src/main/java/tw/huangcti/imrbs/web/dto`
- [x] T030 [US3] ReservationService 新增查詢方法 `getRoomStatuses(date, location)` 於 `imrbs-core/src/main/java/tw/huangcti/imrbs/service/ReservationService.java`
- [x] T031 [US3] 建立 Controller `RoomQueryController` 於 `imrbs-web/src/main/java/tw/huangcti/imrbs/web/RoomQueryController.java`，新增 `GET /rooms/status`

---

## Phase 6: User Story 4 - 管理會議室（P2）

**Goal**: 管理員可新增/編輯/刪除會議室，立即持久化。

**Independent Test**: POST/PUT/DELETE /api/admin/rooms 對 rooms.json 生效，查詢可見更新。

### Implementation for User Story 4

- [x] T032 [P] [US4] RoomRepository 擴充：新增/更新/刪除方法於 `imrbs-core/src/main/java/tw/huangcti/imrbs/repository/RoomRepository.java`
- [x] T033 [US4] 建立 Admin 控制器 `RoomAdminController` 於 `imrbs-web/src/main/java/tw/huangcti/imrbs/web/RoomAdminController.java`，新增 `POST/PUT/DELETE /admin/rooms`
- [x] T034 [US4] 權限控制占位（未來可接入安全框架）於 `imrbs-web/src/main/java/tw/huangcti/imrbs/web/security`

---

## Phase N: Polish & Cross-Cutting Concerns

- [x] T035 [P] 建立種子資料（rooms 初始清單）於 `imrbs-core/src/main/resources/data/rooms.json`
- [x] T036 [P] 建立備份/還原腳本（PowerShell）於 `scripts/backup-restore.ps1`
- [x] T037 更新 README 與 quickstart（端點與測試步驟）於 `README.md` 與 `specs/dev/quickstart.md`
- [x] T038 效能與日誌優化（必要時）於 `imrbs-*/src/**`
- [x] T039 安全性與輸入健全化回顧（email 格式、時間邊界、跨日處理政策）

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup (Phase 1): 無依賴 - 可立即開始
- Foundational (Phase 2): 依賴 Setup 完成 - BLOCKS 所有 user stories
- User Stories (Phase 3+): 依賴 Foundational 完成
- Polish (Final Phase): 依賴欲交付之 user stories 完成

### User Story Dependencies

- US1 (P1): Foundational 後可開始；無需其他故事
- US2 (P1): Foundational 後可開始；可獨立於 US1 實作，但通常依賴已建立的 Reservation 資料
- US3 (P1): Foundational 後可開始；可獨立於 US1/US2 實作
- US4 (P2): Foundational 後可開始；與 US3 部分共用 RoomRepository

### Within Each User Story

- 模型 → 服務 → 控制器
- 必要測試（特別是衝突檢查）先行並確保先失敗後通過
- 保持每個故事可獨立驗證

### Parallel Opportunities

- Phase 1/2 中標記 [P] 的任務皆可平行
- US1/US2/US3 可不同人員平行進行（避免同檔案衝突）
- DTO/Controller 可與部分服務細節平行

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. 完成 Phase 1: Setup
2. 完成 Phase 2: Foundational（CRITICAL）
3. 完成 Phase 3: US1（建立預約）
4. STOP & VALIDATE：獨立測試 US1
5. 視情況部署/示範

### Incremental Delivery

- 依優先順序逐一增加 US2 → US3 → US4，每次完成後可測試與示範

