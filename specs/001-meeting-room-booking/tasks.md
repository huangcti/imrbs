# Tasks: 會議室預約系統

**Feature Branch**: `001-meeting-room-booking`  
**Input**: Design documents from `/specs/001-meeting-room-booking/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

**注意**: 本任務清單基於 TDD (Test-Driven Development) 方法論，憲章第 II 條要求 95%+ 測試覆蓋率（NON-NEGOTIABLE）

---

## 任務格式: `- [ ] [TaskID] [P?] [Story?] Description with file path`

- **[TaskID]**: 任務序號 (T001, T002...)，按執行順序編號
- **[P]**: 可平行執行標記（不同檔案，無依賴關係）
- **[Story]**: 使用者故事標籤（US1, US2, US3...）
- **說明**: 必須包含明確的檔案路徑

## 路徑約定

根據 plan.md Project Structure，本專案採用多模組架構：

- **後端核心**: `imrbs-core/src/main/java/tw/huangcti/imrbs/`
- **後端基礎設施**: `imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/`
- **後端 Web API**: `imrbs-web/src/main/java/tw/huangcti/imrbs/web/`
- **前端**: `imrbs-frontend/src/`
- **資料庫遷移**: `imrbs-infrastructure/src/main/resources/db/migration/`
- **測試**: 各模組的 `src/test/` 目錄

---

## Phase 1: Setup（專案初始化）

**目的**: 建立專案結構與基礎配置

- [X] T001 根據 plan.md 建立 Clean Architecture 多模組結構（imrbs-core, imrbs-infrastructure, imrbs-web, imrbs-frontend）
- [X] T002 [P] 配置後端 Maven 根 POM 與子模組依賴（pom.xml, imrbs-core/pom.xml, imrbs-infrastructure/pom.xml, imrbs-web/pom.xml）
- [X] T003 [P] 初始化前端 Vue 3 專案（imrbs-frontend/package.json, vite.config.ts, tsconfig.json）
- [X] T004 [P] 配置 Checkstyle、Prettier、ESLint（.checkstyle.xml, .prettierrc, .eslintrc.js）
- [X] T005 [P] 配置 Docker Compose 開發環境（docker/docker-compose.yml: PostgreSQL, Redis, RabbitMQ）
- [X] T006 [P] 配置 GitHub Actions CI/CD 工作流程（.github/workflows/backend-ci.yml, frontend-ci.yml）
- [X] T007 建立 Spring Boot 主應用程式類別（imrbs-web/src/main/java/tw/huangcti/imrbs/ImrbsApplication.java）

---

## Phase 2: Foundational（基礎架構 - 所有 User Story 的前置條件）

**目的**: 實作核心基礎設施，完成前任何 User Story 都無法開始

**⚠️ 關鍵**: 本階段必須完成後才能開始 User Story 實作

### 資料庫基礎設施

- [X] T008 配置 Flyway 資料庫遷移（imrbs-infrastructure/src/main/resources/application-infra.yml）
- [X] T009 [P] 建立 User 資料表遷移腳本（imrbs-infrastructure/src/main/resources/db/migration/V1__create_users_table.sql）
- [X] T010 [P] 建立 Room 資料表遷移腳本（imrbs-infrastructure/src/main/resources/db/migration/V2__create_rooms_table.sql）
- [X] T011 [P] 建立 Reservation 資料表遷移腳本（imrbs-infrastructure/src/main/resources/db/migration/V3__create_reservations_table.sql）
- [X] T012 [P] 建立 MaintenanceSchedule 資料表遷移腳本（imrbs-infrastructure/src/main/resources/db/migration/V4__create_maintenance_schedules_table.sql）
- [X] T013 [P] 建立 Notification 資料表遷移腳本（imrbs-infrastructure/src/main/resources/db/migration/V5__create_notifications_table.sql）
- [X] T014 [P] 建立 GuestReservationRequest 資料表遷移腳本（imrbs-infrastructure/src/main/resources/db/migration/V6__create_guest_reservation_requests_table.sql）
- [X] T015 [P] 建立種子資料遷移腳本（V7: 測試使用者與會議室）（imrbs-infrastructure/src/main/resources/db/migration/V7__seed_data.sql）

### 領域模型層（Domain）

- [ ] T016 [P] 建立 User 領域模型（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/model/User.java）
- [ ] T017 [P] 建立 Room 領域模型（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/model/Room.java）
- [ ] T018 [P] 建立 Reservation 領域模型（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/model/Reservation.java）
- [ ] T019 [P] 建立 MaintenanceSchedule 領域模型（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/model/MaintenanceSchedule.java）
- [ ] T020 [P] 建立 Notification 領域模型（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/model/Notification.java）
- [ ] T021 [P] 建立 GuestReservationRequest 領域模型（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/model/GuestReservationRequest.java）

### Repository 介面（Domain）

- [ ] T022 [P] 定義 UserRepository 介面（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/repository/UserRepository.java）
- [ ] T023 [P] 定義 RoomRepository 介面（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/repository/RoomRepository.java）
- [ ] T024 [P] 定義 ReservationRepository 介面（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/repository/ReservationRepository.java）
- [ ] T025 [P] 定義 MaintenanceScheduleRepository 介面（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/repository/MaintenanceScheduleRepository.java）
- [ ] T026 [P] 定義 NotificationRepository 介面（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/repository/NotificationRepository.java）
- [ ] T027 [P] 定義 GuestReservationRequestRepository 介面（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/repository/GuestReservationRequestRepository.java）

### JPA 實體與 Repository 實作（Infrastructure）

- [ ] T028 [P] 實作 UserJpaEntity 與 UserJpaRepository（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/persistence/jpa/entity/UserJpaEntity.java, repository/UserJpaRepository.java）
- [ ] T029 [P] 實作 RoomJpaEntity 與 RoomJpaRepository（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/persistence/jpa/entity/RoomJpaEntity.java, repository/RoomJpaRepository.java）
- [ ] T030 [P] 實作 ReservationJpaEntity 與 ReservationJpaRepository（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/persistence/jpa/entity/ReservationJpaEntity.java, repository/ReservationJpaRepository.java）
- [ ] T031 [P] 實作 MaintenanceScheduleJpaEntity 與 Repository（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/persistence/jpa/entity/MaintenanceScheduleJpaEntity.java, repository/MaintenanceScheduleJpaRepository.java）
- [ ] T032 [P] 實作 NotificationJpaEntity 與 NotificationJpaRepository（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/persistence/jpa/entity/NotificationJpaEntity.java, repository/NotificationJpaRepository.java）
- [ ] T033 [P] 實作 GuestReservationRequestJpaEntity 與 Repository（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/persistence/jpa/entity/GuestReservationRequestJpaEntity.java, repository/GuestReservationRequestJpaRepository.java）

### 安全認證基礎設施

- [ ] T034 配置 Spring Security OAuth 2.0/OIDC（imrbs-web/src/main/java/tw/huangcti/imrbs/web/security/SecurityConfig.java）
- [ ] T035 實作 JWT 認證過濾器（imrbs-web/src/main/java/tw/huangcti/imrbs/web/security/JwtAuthenticationFilter.java）
- [ ] T036 實作 SSO 整合服務（imrbs-web/src/main/java/tw/huangcti/imrbs/web/security/SsoIntegration.java）
- [ ] T037 配置 RBAC 權限註解支援（imrbs-web/src/main/java/tw/huangcti/imrbs/web/security/RoleBasedAccessControl.java）

### 共用基礎設施

- [ ] T038 [P] 配置 Redis 快取（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/config/RedisConfig.java）
- [ ] T039 [P] 配置 RabbitMQ 訊息佇列（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/config/RabbitMQConfig.java）
- [ ] T040 [P] 實作全域異常處理器（imrbs-web/src/main/java/tw/huangcti/imrbs/web/exception/GlobalExceptionHandler.java）
- [ ] T041 [P] 配置 SpringDoc OpenAPI（imrbs-web/src/main/resources/application.yml）
- [ ] T042 [P] 實作審計日誌 AOP（imrbs-web/src/main/java/tw/huangcti/imrbs/web/aspect/AuditLogAspect.java）

### 前端基礎架構

- [ ] T043 [P] 配置 Vue Router 路由（imrbs-frontend/src/router/index.ts）
- [ ] T044 [P] 配置 Pinia 狀態管理（imrbs-frontend/src/stores/index.ts）
- [ ] T045 [P] 配置 Axios API 客戶端（imrbs-frontend/src/services/api.ts）
- [ ] T046 [P] 配置 vue-i18n 多語系（imrbs-frontend/src/i18n.ts, public/locales/zh-TW.json, en.json）
- [ ] T047 [P] 配置 Tailwind CSS 與 PrimeVue（imrbs-frontend/tailwind.config.js, src/main.ts）
- [ ] T048 [P] 建立通用佈局元件（imrbs-frontend/src/components/layout/Header.vue, Footer.vue, Sidebar.vue）
- [ ] T049 [P] 建立 Loading、Modal、Toast 通用元件（imrbs-frontend/src/components/common/Loading.vue, Modal.vue, Toast.vue）

**Checkpoint ✅**: 基礎架構完成，User Story 實作可以平行開始

---

## Phase 3: User Story 1 - 員工查詢與預約會議室 (Priority: P1) 🎯 MVP

**目標**: 員工可以查詢可用會議室、查看詳情、提交預約並收到確認

**獨立測試**: 員工登入→查詢會議室（日期/時間/容量）→查看詳情→提交預約→收到確認 email

**測試策略**: TDD Red-Green-Refactor 循環，測試先行

### 測試任務（US1）- 先寫測試，確保 RED 狀態

- [ ] T050 [P] [US1] 撰寫會議室查詢 API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/RoomControllerTest.java: testGetAvailableRooms）
- [ ] T051 [P] [US1] 撰寫預約創建 API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/ReservationControllerTest.java: testCreateReservation）
- [ ] T052 [P] [US1] 撰寫衝突檢測單元測試（imrbs-core/src/test/java/tw/huangcti/imrbs/domain/service/ConflictDetectionServiceTest.java）
- [ ] T053 [P] [US1] 撰寫通知發送整合測試（imrbs-infrastructure/src/test/java/tw/huangcti/imrbs/infrastructure/messaging/NotificationServiceTest.java）
- [ ] T054 [P] [US1] 撰寫前端預約表單 E2E 測試（imrbs-frontend/tests/e2e/specs/reservation.cy.ts）

### 後端實作（US1）

- [ ] T055 [P] [US1] 實作會議室可用性查詢服務（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/service/RoomAvailabilityService.java）
- [ ] T056 [P] [US1] 實作預約衝突檢測服務（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/service/ConflictDetectionService.java）
- [ ] T057 [US1] 實作創建預約 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/CreateReservationUseCase.java，依賴 T055, T056）
- [ ] T058 [P] [US1] 實作 RoomController GET /rooms 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/RoomController.java）
- [ ] T059 [P] [US1] 實作 RoomController GET /rooms/{id} 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/RoomController.java）
- [ ] T060 [P] [US1] 實作 RoomController GET /rooms/{id}/availability 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/RoomController.java）
- [ ] T061 [US1] 實作 ReservationController POST /reservations 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/ReservationController.java，依賴 T057）
- [ ] T062 [P] [US1] 實作 DTO 映射器（RoomMapper, ReservationMapper）（imrbs-web/src/main/java/tw/huangcti/imrbs/web/mapper/RoomMapper.java, ReservationMapper.java）
- [ ] T063 [P] [US1] 實作 Email 通知服務（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/integration/email/EmailService.java）
- [ ] T064 [US1] 實作 RabbitMQ 預約確認事件監聽器（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/messaging/listener/ReservationConfirmedListener.java，依賴 T063）

### 前端實作（US1）

- [ ] T065 [P] [US1] 實作會議室查詢 Pinia Store（imrbs-frontend/src/stores/room.ts）
- [ ] T066 [P] [US1] 實作預約 Pinia Store（imrbs-frontend/src/stores/reservation.ts）
- [ ] T067 [P] [US1] 實作會議室 API 服務（imrbs-frontend/src/services/room.service.ts）
- [ ] T068 [P] [US1] 實作預約 API 服務（imrbs-frontend/src/services/reservation.service.ts）
- [ ] T069 [P] [US1] 建立會議室篩選元件（imrbs-frontend/src/components/room/RoomFilter.vue）
- [ ] T070 [P] [US1] 建立會議室卡片元件（imrbs-frontend/src/components/room/RoomCard.vue）
- [ ] T071 [P] [US1] 建立會議室詳情元件（imrbs-frontend/src/components/room/RoomDetail.vue）
- [ ] T072 [P] [US1] 建立預約表單元件（imrbs-frontend/src/components/reservation/ReservationForm.vue）
- [ ] T073 [US1] 建立會議室搜尋頁面（imrbs-frontend/src/views/RoomSearch.vue，整合 T069-T072）
- [ ] T074 [P] [US1] 實作日期時間工具函式（imrbs-frontend/src/utils/date.ts）
- [ ] T075 [P] [US1] 實作表單驗證工具函式（imrbs-frontend/src/utils/validation.ts）

**Checkpoint ✅**: US1 完整實作完成，可以獨立測試與交付

---

## Phase 4: User Story 2 - 員工修改與取消預約 (Priority: P1) 🎯 MVP

**目標**: 員工可以查看個人預約、修改預約時間/參與者、取消預約（需驗證 24 小時規則）

**獨立測試**: 員工登入→查看我的預約→選擇預約→修改或取消→系統驗證規則→發送通知

### 測試任務（US2）

- [ ] T076 [P] [US2] 撰寫修改預約 API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/ReservationControllerTest.java: testUpdateReservation）
- [ ] T077 [P] [US2] 撰寫取消預約 API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/ReservationControllerTest.java: testCancelReservation）
- [ ] T078 [P] [US2] 撰寫 24 小時取消規則單元測試（imrbs-core/src/test/java/tw/huangcti/imrbs/domain/service/CancellationPolicyServiceTest.java）
- [ ] T079 [P] [US2] 撰寫前端我的預約頁面 E2E 測試（imrbs-frontend/tests/e2e/specs/my-reservations.cy.ts）

### 後端實作（US2）

- [ ] T080 [P] [US2] 實作取消政策檢查服務（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/service/CancellationPolicyService.java）
- [ ] T081 [US2] 實作修改預約 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/UpdateReservationUseCase.java，依賴 T056）
- [ ] T082 [US2] 實作取消預約 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/CancelReservationUseCase.java，依賴 T080）
- [ ] T083 [P] [US2] 實作 ReservationController GET /reservations 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/ReservationController.java）
- [ ] T084 [P] [US2] 實作 ReservationController GET /reservations/{id} 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/ReservationController.java）
- [ ] T085 [US2] 實作 ReservationController PUT /reservations/{id} 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/ReservationController.java，依賴 T081）
- [ ] T086 [US2] 實作 ReservationController DELETE /reservations/{id} 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/ReservationController.java，依賴 T082）
- [ ] T087 [P] [US2] 實作預約變更通知服務（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/integration/email/ReservationChangeNotificationService.java）
- [ ] T088 [US2] 實作 RabbitMQ 預約變更事件監聽器（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/messaging/listener/ReservationChangedListener.java，依賴 T087）

### 前端實作（US2）

- [ ] T089 [P] [US2] 建立預約清單元件（imrbs-frontend/src/components/reservation/ReservationList.vue）
- [ ] T090 [P] [US2] 建立預約編輯表單元件（imrbs-frontend/src/components/reservation/ReservationEditForm.vue）
- [ ] T091 [P] [US2] 建立取消預約確認 Modal 元件（imrbs-frontend/src/components/reservation/CancelReservationModal.vue）
- [ ] T092 [US2] 建立我的預約頁面（imrbs-frontend/src/views/MyReservations.vue，整合 T089-T091）

**Checkpoint ✅**: US2 完整實作完成，與 US1 組成完整預約管理 MVP

---

## Phase 5: User Story 3 - SSO 單一登入整合 (Priority: P1) 🎯 MVP

**目標**: 員工使用公司帳號（LDAP/Active Directory）登入，系統自動獲取員工資訊與權限

**獨立測試**: 員工訪問系統→點擊 SSO 登入→跳轉驗證→返回系統→顯示員工姓名與權限

### 測試任務（US3）

- [ ] T093 [P] [US3] 撰寫 SSO 登入流程整合測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/security/SsoIntegrationTest.java）
- [ ] T094 [P] [US3] 撰寫 JWT Token 生成與驗證單元測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/security/JwtServiceTest.java）
- [ ] T095 [P] [US3] 撰寫 RBAC 權限檢查單元測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/security/RoleBasedAccessControlTest.java）
- [ ] T096 [P] [US3] 撰寫前端登入頁面 E2E 測試（imrbs-frontend/tests/e2e/specs/login.cy.ts）

### 後端實作（US3）

- [ ] T097 [P] [US3] 實作 OAuth 2.0 Authorization Code 交換服務（imrbs-web/src/main/java/tw/huangcti/imrbs/web/security/OAuth2Service.java）
- [ ] T098 [P] [US3] 實作 JWT Token 生成與驗證服務（imrbs-web/src/main/java/tw/huangcti/imrbs/web/security/JwtService.java）
- [ ] T099 [US3] 實作 SSO 使用者資訊同步服務（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/SyncUserFromSsoUseCase.java，依賴 T097）
- [ ] T100 [P] [US3] 實作 AuthController POST /auth/login 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/AuthController.java）
- [ ] T101 [P] [US3] 實作 AuthController POST /auth/refresh 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/AuthController.java）
- [ ] T102 [P] [US3] 實作 AuthController GET /auth/me 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/AuthController.java）
- [ ] T103 [P] [US3] 配置 Session 管理與逾時策略（imrbs-web/src/main/resources/application.yml: session.timeout=8h）

### 前端實作（US3）

- [ ] T104 [P] [US3] 實作認證 Pinia Store（imrbs-frontend/src/stores/auth.ts）
- [ ] T105 [P] [US3] 實作認證 API 服務（imrbs-frontend/src/services/auth.service.ts）
- [ ] T106 [P] [US3] 實作認證 Composable（imrbs-frontend/src/composables/useAuth.ts）
- [ ] T107 [P] [US3] 實作路由守衛（登入檢查）（imrbs-frontend/src/router/guards.ts）
- [ ] T108 [P] [US3] 建立登入頁面（imrbs-frontend/src/views/Login.vue）
- [ ] T109 [P] [US3] 建立首頁（imrbs-frontend/src/views/Home.vue）
- [ ] T110 [P] [US3] 實作 Axios 攔截器（自動附加 JWT Token）（imrbs-frontend/src/services/api.ts）

**Checkpoint ✅**: US3 完整實作完成，US1+US2+US3 構成可交付的 MVP

---

## Phase 6: User Story 4 - 會議室管理功能 (Priority: P2)

**目標**: 管理員可以新增/編輯/刪除會議室、上傳照片、設定維護時段

**獨立測試**: 管理員登入→會議室管理→新增會議室→上傳照片→設定維護→員工查詢可見

### 測試任務（US4）

- [ ] T111 [P] [US4] 撰寫會議室 CRUD API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/RoomControllerTest.java: testCreateRoom, testUpdateRoom, testDeleteRoom）
- [ ] T112 [P] [US4] 撰寫維護時段 API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/MaintenanceControllerTest.java）
- [ ] T113 [P] [US4] 撰寫前端會議室管理頁面 E2E 測試（imrbs-frontend/tests/e2e/specs/room-management.cy.ts）

### 後端實作（US4）

- [ ] T114 [P] [US4] 實作創建會議室 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/CreateRoomUseCase.java）
- [ ] T115 [P] [US4] 實作更新會議室 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/UpdateRoomUseCase.java）
- [ ] T116 [P] [US4] 實作刪除會議室 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/DeleteRoomUseCase.java）
- [ ] T117 [P] [US4] 實作創建維護時段 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/CreateMaintenanceScheduleUseCase.java）
- [ ] T118 [P] [US4] 實作 RoomController POST /rooms 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/RoomController.java，需 ROOM_ADMIN 權限）
- [ ] T119 [P] [US4] 實作 RoomController PUT /rooms/{id} 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/RoomController.java，需 ROOM_ADMIN 權限）
- [ ] T120 [P] [US4] 實作 RoomController DELETE /rooms/{id} 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/RoomController.java，需 ROOM_ADMIN 權限）
- [ ] T121 [P] [US4] 實作 MaintenanceController POST /admin/rooms/{id}/maintenance 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/MaintenanceController.java）
- [ ] T122 [P] [US4] 實作檔案上傳服務（會議室照片）（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/integration/FileUploadService.java）

### 前端實作（US4）

- [ ] T123 [P] [US4] 建立會議室表單元件（imrbs-frontend/src/components/admin/RoomForm.vue）
- [ ] T124 [P] [US4] 建立照片上傳元件（imrbs-frontend/src/components/admin/PhotoUpload.vue）
- [ ] T125 [P] [US4] 建立維護時段設定元件（imrbs-frontend/src/components/admin/MaintenanceScheduleForm.vue）
- [ ] T126 [US4] 建立會議室管理頁面（imrbs-frontend/src/views/admin/RoomManagement.vue，整合 T123-T125）

**Checkpoint ✅**: US4 完整實作完成，管理員可自主管理會議室

---

## Phase 7: User Story 5 - 通知與提醒系統 (Priority: P2)

**目標**: 員工/管理員收到預約相關通知（成功/修改/取消），會議前 30 分鐘提醒

**獨立測試**: 完成預約→檢查 email→修改預約→檢查通知→等待會議前 30 分鐘→檢查提醒

### 測試任務（US5）

- [ ] T127 [P] [US5] 撰寫 Email 通知發送單元測試（imrbs-infrastructure/src/test/java/tw/huangcti/imrbs/infrastructure/integration/email/EmailServiceTest.java）
- [ ] T128 [P] [US5] 撰寫會議提醒排程任務單元測試（imrbs-infrastructure/src/test/java/tw/huangcti/imrbs/infrastructure/messaging/scheduler/MeetingReminderSchedulerTest.java）
- [ ] T129 [P] [US5] 撰寫通知重試機制單元測試（imrbs-infrastructure/src/test/java/tw/huangcti/imrbs/infrastructure/messaging/NotificationRetryServiceTest.java）

### 後端實作（US5）

- [ ] T130 [P] [US5] 實作 Email 模板引擎（Thymeleaf）（imrbs-infrastructure/src/main/resources/templates/email/reservation-confirmed.html, reservation-cancelled.html, meeting-reminder.html）
- [ ] T131 [P] [US5] 實作多語系 Email 通知服務（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/integration/email/I18nEmailService.java）
- [ ] T132 [P] [US5] 實作 RabbitMQ Dead Letter Queue 配置（30 分鐘延遲提醒）（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/config/RabbitMQConfig.java）
- [ ] T133 [P] [US5] 實作會議提醒排程任務（Spring @Scheduled）（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/messaging/scheduler/MeetingReminderScheduler.java）
- [ ] T134 [P] [US5] 實作通知失敗重試機制（最多 3 次）（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/messaging/NotificationRetryService.java）
- [ ] T135 [P] [US5] 實作訪客審核通知服務（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/integration/email/GuestRequestNotificationService.java）

**Checkpoint ✅**: US5 完整實作完成，通知系統自動化運作

---

## Phase 8: User Story 6 - 使用率統計報告 (Priority: P2)

**目標**: 管理員可以生成會議室使用率報告（圖表、表格）並匯出 Excel/CSV

**獨立測試**: 管理員登入→使用率報告→選擇時間範圍→查看圖表→匯出 Excel

### 測試任務（US6）

- [ ] T136 [P] [US6] 撰寫使用率計算服務單元測試（imrbs-core/src/test/java/tw/huangcti/imrbs/domain/service/UsageStatisticsServiceTest.java）
- [ ] T137 [P] [US6] 撰寫報告 API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/ReportControllerTest.java）
- [ ] T138 [P] [US6] 撰寫前端報告頁面 E2E 測試（imrbs-frontend/tests/e2e/specs/usage-report.cy.ts）

### 後端實作（US6）

- [ ] T139 [P] [US6] 實作使用率統計服務（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/service/UsageStatisticsService.java）
- [ ] T140 [P] [US6] 實作熱門時段分析服務（imrbs-core/src/main/java/tw/huangcti/imrbs/domain/service/PopularTimeSlotsService.java）
- [ ] T141 [US6] 實作生成報告 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/GenerateUsageReportUseCase.java，依賴 T139, T140）
- [ ] T142 [P] [US6] 實作 ReportController GET /admin/reports/usage 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/ReportController.java）
- [ ] T143 [P] [US6] 實作 Excel 匯出服務（Apache POI）（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/integration/ExcelExportService.java）
- [ ] T144 [P] [US6] 實作 ReportController GET /admin/reports/export 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/ReportController.java）

### 前端實作（US6）

- [ ] T145 [P] [US6] 建立報告篩選元件（imrbs-frontend/src/components/admin/ReportFilter.vue）
- [ ] T146 [P] [US6] 建立圖表元件（Chart.js/ECharts 整合）（imrbs-frontend/src/components/admin/UsageChart.vue）
- [ ] T147 [P] [US6] 建立報告表格元件（imrbs-frontend/src/components/admin/UsageTable.vue）
- [ ] T148 [US6] 建立報告儀表板頁面（imrbs-frontend/src/views/admin/ReportDashboard.vue，整合 T145-T147）

**Checkpoint ✅**: US6 完整實作完成，管理員可基於數據優化資源配置

---

## Phase 9: User Story 7 - 外部訪客預約審核 (Priority: P3)

**目標**: 訪客可提交預約申請，管理員審核批准或拒絕

**獨立測試**: 訪客提交申請→管理員收到通知→審核批准/拒絕→訪客收到結果通知

### 測試任務（US7）

- [ ] T149 [P] [US7] 撰寫訪客預約提交 API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/GuestControllerTest.java）
- [ ] T150 [P] [US7] 撰寫訪客預約審核 API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/GuestControllerTest.java）
- [ ] T151 [P] [US7] 撰寫前端訪客預約頁面 E2E 測試（imrbs-frontend/tests/e2e/specs/guest-request.cy.ts）

### 後端實作（US7）

- [ ] T152 [P] [US7] 實作創建訪客預約申請 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/CreateGuestRequestUseCase.java）
- [ ] T153 [P] [US7] 實作批准訪客預約 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/ApproveGuestRequestUseCase.java）
- [ ] T154 [P] [US7] 實作拒絕訪客預約 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/RejectGuestRequestUseCase.java）
- [ ] T155 [P] [US7] 實作 GuestController POST /guest/requests 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/GuestController.java，Public 權限）
- [ ] T156 [P] [US7] 實作 GuestController GET /admin/guest-requests 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/GuestController.java，ROOM_ADMIN 權限）
- [ ] T157 [P] [US7] 實作 GuestController POST /admin/guest-requests/{id}/approve 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/GuestController.java）
- [ ] T158 [P] [US7] 實作 GuestController POST /admin/guest-requests/{id}/reject 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/GuestController.java）

### 前端實作（US7）

- [ ] T159 [P] [US7] 建立訪客預約表單元件（imrbs-frontend/src/components/guest/GuestRequestForm.vue）
- [ ] T160 [P] [US7] 建立訪客預約頁面（公開頁面）（imrbs-frontend/src/views/GuestRequest.vue）
- [ ] T161 [P] [US7] 建立訪客預約審核清單元件（imrbs-frontend/src/components/admin/GuestRequestList.vue）
- [ ] T162 [P] [US7] 建立訪客預約審核頁面（imrbs-frontend/src/views/admin/GuestApproval.vue）

**Checkpoint ✅**: US7 完整實作完成，支援訪客預約流程

---

## Phase 10: User Story 8 - 多語系支援（中英文）(Priority: P3)

**目標**: 系統支援繁體中文與英文介面，員工可切換語言

**獨立測試**: 登入→設定→切換語言為 English→確認介面、email 通知皆為英文

### 測試任務（US8）

- [ ] T163 [P] [US8] 撰寫語言切換 API 合約測試（imrbs-web/src/test/java/tw/huangcti/imrbs/web/controller/UserControllerTest.java: testUpdateLanguagePreference）
- [ ] T164 [P] [US8] 撰寫多語系 Email 通知單元測試（imrbs-infrastructure/src/test/java/tw/huangcti/imrbs/infrastructure/integration/email/I18nEmailServiceTest.java）
- [ ] T165 [P] [US8] 撰寫前端語言切換 E2E 測試（imrbs-frontend/tests/e2e/specs/i18n.cy.ts）

### 後端實作（US8）

- [ ] T166 [P] [US8] 配置 Spring i18n MessageSource（imrbs-web/src/main/resources/messages_zh_TW.properties, messages_en.properties）
- [ ] T167 [P] [US8] 實作更新語言偏好 Use Case（imrbs-core/src/main/java/tw/huangcti/imrbs/application/usecase/UpdateLanguagePreferenceUseCase.java）
- [ ] T168 [P] [US8] 實作 UserController PUT /users/me/language 端點（imrbs-web/src/main/java/tw/huangcti/imrbs/web/controller/UserController.java）
- [ ] T169 [P] [US8] 實作多語系 Email 模板（繁中/英文版本）（imrbs-infrastructure/src/main/resources/templates/email/reservation-confirmed_zh_TW.html, reservation-confirmed_en.html）

### 前端實作（US8）

- [ ] T170 [P] [US8] 完善繁體中文翻譯檔案（imrbs-frontend/public/locales/zh-TW.json，涵蓋所有介面文字）
- [ ] T171 [P] [US8] 完善英文翻譯檔案（imrbs-frontend/public/locales/en.json，涵蓋所有介面文字）
- [ ] T172 [P] [US8] 建立語言切換元件（imrbs-frontend/src/components/common/LanguageSwitcher.vue）
- [ ] T173 [P] [US8] 整合語言切換至 Header 元件（imrbs-frontend/src/components/layout/Header.vue）
- [ ] T174 [P] [US8] 實作瀏覽器語言自動偵測（imrbs-frontend/src/i18n.ts）

**Checkpoint ✅**: US8 完整實作完成，系統支援雙語介面

---

## Final Phase: Polish & Cross-Cutting Concerns

**目的**: 優化系統品質、效能、監控與文檔

### 效能優化

- [ ] T175 [P] 實作 Redis 快取策略（會議室清單、可用性查詢）（imrbs-infrastructure/src/main/java/tw/huangcti/imrbs/infrastructure/persistence/redis/RoomCacheService.java）
- [ ] T176 [P] 配置資料庫索引優化（根據查詢計畫分析）（imrbs-infrastructure/src/main/resources/db/migration/V8__add_performance_indexes.sql）
- [ ] T177 [P] 實作前端程式碼分割（Lazy Loading 路由）（imrbs-frontend/src/router/index.ts）
- [ ] T178 [P] 實作前端虛擬滾動（大型會議室清單）（imrbs-frontend/src/components/room/VirtualRoomList.vue）
- [ ] T179 [P] 配置 CDN 靜態資源快取（imrbs-frontend/vite.config.ts）

### 監控與日誌

- [ ] T180 [P] 配置 Spring Boot Actuator 端點（imrbs-web/src/main/resources/application.yml: management.endpoints）
- [ ] T181 [P] 配置 Prometheus Metrics（imrbs-web/pom.xml: micrometer-registry-prometheus）
- [ ] T182 [P] 實作自訂業務指標（預約成功率、衝突檢測次數）（imrbs-web/src/main/java/tw/huangcti/imrbs/web/metrics/BusinessMetrics.java）
- [ ] T183 [P] 配置 Logback 結構化日誌（JSON 格式）（imrbs-web/src/main/resources/logback-spring.xml）
- [ ] T184 [P] 實作前端錯誤追蹤（Sentry 整合）（imrbs-frontend/src/main.ts）

### 安全強化

- [ ] T185 [P] 實作 CSRF Token 驗證（imrbs-web/src/main/java/tw/huangcti/imrbs/web/security/SecurityConfig.java）
- [ ] T186 [P] 配置 CORS 白名單（imrbs-web/src/main/resources/application.yml）
- [ ] T187 [P] 實作 Rate Limiting（每分鐘 60 次請求）（imrbs-web/src/main/java/tw/huangcti/imrbs/web/filter/RateLimitFilter.java）
- [ ] T188 [P] 實作敏感資料遮罩（日誌中的 email、電話）（imrbs-web/src/main/java/tw/huangcti/imrbs/web/logging/SensitiveDataMasker.java）
- [ ] T189 [P] 配置 TLS 1.3 強制加密（kubernetes/ingress.yaml）

### 架構測試

- [ ] T190 [P] 實作 ArchUnit 依賴規則測試（imrbs-core/src/test/java/tw/huangcti/imrbs/ArchitectureTest.java: testDependencyRules）
- [ ] T191 [P] 實作 ArchUnit 命名規範測試（imrbs-core/src/test/java/tw/huangcti/imrbs/ArchitectureTest.java: testNamingConventions）
- [ ] T192 [P] 實作 ArchUnit 層級隔離測試（imrbs-core/src/test/java/tw/huangcti/imrbs/ArchitectureTest.java: testLayerIsolation）

### 部署與文檔

- [ ] T193 [P] 建立 Kubernetes 部署檔案（kubernetes/backend-deployment.yaml, frontend-deployment.yaml, redis-deployment.yaml, rabbitmq-deployment.yaml）
- [ ] T194 [P] 建立 Kubernetes ConfigMap 與 Secret（kubernetes/configmap.yaml, secret.yaml）
- [ ] T195 [P] 建立 Ingress 配置（kubernetes/ingress.yaml）
- [ ] T196 [P] 撰寫 API 文檔（基於 SpringDoc OpenAPI）（補充註釋至所有 Controller）
- [ ] T197 [P] 撰寫部署文檔（scripts/deploy.md: 部署步驟、環境變數、故障排除）
- [ ] T198 [P] 撰寫開發者文檔（README.md: 專案架構、開發流程、貢獻指南）
- [ ] T199 [P] 更新 quickstart.md（補充完整開發環境設定與常見問題）

**Checkpoint ✅**: 系統已完成所有功能、優化與文檔，準備進入 SIT 測試與生產部署

---

## Dependencies & Parallel Execution

### User Story Completion Order（推薦順序）

1. **Phase 1 → Phase 2** (必須按順序): Setup → Foundational（基礎架構必須先完成）
2. **Phase 3 → Phase 4 → Phase 5** (按順序建議): US1 → US2 → US3（這 3 個 MVP 故事建議按順序實作，確保核心流程穩固）
3. **Phase 6 → Phase 10** (可平行): US4, US5, US6, US7, US8（P2 和 P3 故事彼此獨立，可平行開發）
4. **Final Phase** (最後): Polish & Cross-Cutting Concerns（所有功能完成後優化）

### Parallel Execution Examples（平行執行範例）

#### 範例 1: US1（員工查詢與預約）內部平行

可同時執行：
- T050-T054（測試任務，5 個測試案例獨立撰寫）
- T055-T056（後端服務，不同檔案）
- T065-T068（前端 Store 與 Service，不同檔案）
- T069-T072（前端元件，不同檔案）

不可平行（有依賴）：
- T057 依賴 T055 和 T056（Use Case 需要 Service）
- T061 依賴 T057（Controller 需要 Use Case）
- T064 依賴 T063（事件監聽器需要通知服務）
- T073 依賴 T069-T072（頁面整合元件）

#### 範例 2: P2 故事平行開發

當 US1+US2+US3（MVP）完成後，可同時開發：
- **團隊 A**: Phase 6（US4 會議室管理）
- **團隊 B**: Phase 7（US5 通知系統）
- **團隊 C**: Phase 8（US6 使用率報告）

這些故事彼此獨立，不會互相阻塞。

#### 範例 3: Final Phase 平行優化

可同時執行（不同關注點）：
- T175-T179（效能優化）
- T180-T184（監控與日誌）
- T185-T189（安全強化）
- T190-T192（架構測試）
- T193-T199（部署與文檔）

---

## Implementation Strategy（實作策略）

### MVP First（MVP 優先）

**MVP 定義**: US1 + US2 + US3 = 員工可以登入、查詢會議室、預約、修改、取消

**建議實作順序**:
1. Phase 1 + Phase 2（Setup + Foundational）
2. Phase 3（US1: 查詢與預約）
3. Phase 4（US2: 修改與取消）
4. Phase 5（US3: SSO 登入）
5. **MVP 完成，可交付測試/Demo**

### Incremental Delivery（增量交付）

MVP 完成後，每個 User Story 獨立交付：
- Phase 6（US4）→ **Increment 1**: 管理員可管理會議室
- Phase 7（US5）→ **Increment 2**: 通知系統上線
- Phase 8（US6）→ **Increment 3**: 使用率報告可用
- Phase 9（US7）→ **Increment 4**: 訪客預約功能
- Phase 10（US8）→ **Increment 5**: 多語系支援

### TDD Workflow（TDD 工作流程）

每個 User Story 嚴格遵循 Red-Green-Refactor：

1. **Red**: 撰寫測試（T050-T054 for US1），確保測試失敗（功能尚未實作）
2. **Green**: 實作最小可用程式碼（T055-T064 for US1），通過測試
3. **Refactor**: 重構優化（保持測試通過）

**測試覆蓋率目標**: 95%+（憲章第 II 條 NON-NEGOTIABLE 要求）

### Code Review Checkpoints（程式碼審查檢查點）

每個 User Story 完成後進行 PR Review：
- ✅ 所有測試通過（單元測試、整合測試、E2E 測試）
- ✅ 測試覆蓋率 ≥ 95%
- ✅ SonarQube 品質閘門通過（無 Critical/Major Issues）
- ✅ ArchUnit 架構測試通過（依賴規則符合 Clean Architecture）
- ✅ 功能符合 spec.md 的驗收場景（Given-When-Then）

---

## Summary

**總任務數**: 199 個任務  
**平行機會**: 約 120 個任務標記 [P]（可平行執行）  
**獨立測試標準**: 每個 User Story 都有明確的測試方法  
**MVP 範圍**: Phase 1 + Phase 2 + Phase 3 + Phase 4 + Phase 5（US1+US2+US3）  
**格式驗證**: ✅ 所有任務遵循 `- [ ] [TaskID] [P?] [Story?] Description with file path` 格式

**下一步**: 開始執行 Phase 1（Setup），建立專案結構，然後進入 Phase 2（Foundational）實作基礎架構。
