# Implementation Plan: 會議室預約系統

**Branch**: `001-meeting-room-booking` | **Date**: 2025-11-20 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-meeting-room-booking/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

建立會議室預約系統,支援員工快速查詢與預約會議室、修改取消預約、SSO認證、管理員管理會議室與訪客預約審核、通知系統、使用率報告、多語系支援、整合Outlook/Teams。技術實作採用 Vue 3.x + Spring Boot 4.x 架構,前端使用微前端框架(Single-SPA或Module Federation),後端遵循 Clean Architecture,容器化部署至 Kubernetes,整合 Redis 快取與 RabbitMQ/Kafka 訊息佇列,嚴格遵循 TDD 95%+ 測試覆蓋率。

## Technical Context

**Language/Version**: 
- 前端: Vue 3.x (最新穩定版, Composition API), TypeScript 5.x
- 後端: Java 25, Spring Boot 4.x (最新穩定版)

**Primary Dependencies**: 
- 前端: PrimeVue (UI元件庫), Tailwind CSS (樣式框架), Pinia (狀態管理), Vue Router (路由), vue-i18n (多語系), Vite (建置工具)
- 後端: Spring Data JPA (資料存取), Hibernate (ORM), Spring Security (認證授權), SpringDoc OpenAPI (API文檔), Spring Cache (快取抽象)
- 微前端: Single-SPA 或 Module Federation (Webpack 5) - NEEDS CLARIFICATION
- 訊息佇列: RabbitMQ 或 Kafka (spring-boot-starter-amqp 或 spring-kafka) - NEEDS CLARIFICATION

**Storage**: 
- 主資料庫: PostgreSQL 或 SQL Server - **NEEDS CLARIFICATION** (需選定其中之一,兩者皆支援 Spring Data JPA)
- 快取層: Redis (搭配 spring-boot-starter-data-redis, Spring Cache 抽象 @Cacheable/@CacheEvict)
- 訊息佇列: RabbitMQ 或 Kafka (用於非同步通知、日曆同步事件)

**Testing**: 
- 前端: Jest (單元測試) + Vue Test Utils (元件測試) + Cypress (E2E測試)
- 後端: JUnit 5 (單元測試) + Mockito (模擬物件) + Spring Boot Test (整合測試)
- 架構測試: ArchUnit (驗證各層依賴關係)
- API測試: REST Assured (SIT環境整合測試)
- 覆蓋率目標: 95%+ (單元測試), 100% API端點覆蓋(整合測試)
- 效能測試: JMeter 或 Gatling (SIT環境負載測試)

**Target Platform**: 
- 容器化: Docker (主要部署方式)
- 編排: Kubernetes 或 Docker Swarm
- 備選: VM 虛擬機 (傳統部署方式)
- 開發環境: Podman/Docker + Dev Containers (確保環境一致性)
- 監控: Prometheus + Grafana (指標視覺化), ELK Stack (日誌集中管理), Spring Boot Actuator (健康檢查)

**Project Type**: web (前端 Vue 3 SPA + 後端 Spring Boot RESTful API)

**Performance Goals**: 
- API 回應時間 < 2秒 (P95), 關鍵查詢 (會議室可用性) < 500ms (P95)
- 支援至少 100 個並發使用者,系統不降級
- 會議室查詢結果 < 1秒返回 (P95)
- 系統可用性 99.9% (每月停機時間 < 45分鐘)
- 預約操作完成時間 < 30秒 (含查詢、選擇、提交、確認)
- 通知 email 發送延遲 < 5分鐘
- 前端頁面載入時間: LCP < 2.5s, FID < 100ms, CLS < 0.1

**Constraints**: 
- 關鍵操作響應時間 < 200ms p95
- WCAG 2.1 AA 無障礙標準 (支援鍵盤導航、螢幕閱讀器)
- GDPR/個資法合規 (提供資料匯出刪除功能, AES-256 敏感資料加密)
- 強制 SSO 認證 (整合公司 LDAP/Active Directory, 支援 OAuth 2.0/OIDC)
- RBAC 授權控制 (一般員工/會議室管理員/系統管理員三層權限)
- 強制 HTTPS/TLS 1.3 傳輸加密
- 防範 OWASP Top 10 漏洞
- 審計日誌保留 ≥ 1年
- 禁止使用 `any` 型別 (TypeScript) 或原始型別 (Java)
- TDD 測試先行 (Red-Green-Refactor 循環) - **不可協商**

**Scale/Scope**: 
- 企業級規模: 支援多辦公室/分公司,多站點部署
- 預期使用者數: 500-1000+ 員工
- 會議室數量: 50-100+ 間
- 預約記錄: 每月 5000+ 筆,需保留 ≥ 2年歷史記錄
- 多語系: 繁體中文 (zh-TW 主要) + 英文 (en)
- 整合範圍: Outlook 行事曆同步, Microsoft Teams 視訊會議連結, 企業 email 通知系統
- 水平擴展: 無狀態服務設計,支援 Kubernetes HPA 自動擴展

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

基於 IMRBS 憲章 v1.0.0 的 10 項核心原則檢查:

### I. 程式碼品質 (Code Quality) - ✅ 符合
- ✅ 模組化與整潔程式碼: 採用 Clean Architecture 分層 (Presentation → Application → Domain → Infrastructure)
- ✅ 型別安全: 前端強制 TypeScript, 禁止 `any`, 後端 Java 25 強型別
- ✅ 最小化依賴: 使用 Spring Boot/Vue 生態系標準函式庫, 新依賴需審核
- ✅ 程式碼審查: 所有 PR 必須經資深開發者審查
- ✅ 可讀性優先: 註釋使用繁體中文, 程式碼自我說明

### II. 測試標準 (Testing Standards) - ✅ 符合 (NON-NEGOTIABLE)
- ✅ 測試先行: 採用 TDD Red-Green-Refactor 循環
- ✅ 用戶驗收: 測試案例經產品負責人驗收 (基於 spec.md 的 32 個 Given-When-Then 場景)
- ✅ 覆蓋率要求: 單元測試 95%+, 整合測試覆蓋所有 API 端點
- ✅ E2E 測試: Cypress 覆蓋關鍵流程 (預訂衝突檢測、通知系統、權限控制)
- ✅ 測試工具: Jest/Vue Test Utils/Cypress (前端), JUnit 5/Mockito/Spring Boot Test (後端)
- ✅ CI/CD 整合: GitHub Actions/GitLab CI 自動執行, 失敗阻止合併

### III. 使用者體驗一致性 (UX Consistency) - ✅ 符合
- ✅ 行動優先 (Mobile-First): UI 先設計行動版本
- ✅ 響應式設計: Tailwind CSS breakpoints (sm/md/lg/xl)
- ✅ 直覺互動: 拖放式預約重新安排 (Phase 2), 日曆視圖快速操作
- ✅ 無障礙性: WCAG 2.1 AA 標準, 鍵盤導航, 螢幕閱讀器
- ✅ 多語言支援: vue-i18n + Spring i18n (zh-TW, en)
- ✅ 回饋機制: 預訂後評分 (Phase 3)
- ✅ 一致性: PrimeVue + Tailwind CSS 統一設計系統

### IV. 效能需求 (Performance Requirements) - ✅ 符合
- ✅ 回應時間: API < 2s (P95), 查詢可用性 < 500ms
- ✅ 並發支援: 100+ 並發使用者, Kubernetes HPA 自動擴展
- ✅ 資料庫最佳化: 索引式查詢, 執行計畫分析
- ✅ 快取策略: Redis 快取會議室可用性 (TTL 30s), CDN 靜態資源
- ✅ 效能監控: Spring Boot Actuator + Prometheus + Grafana, 告警閾值設定

### V. 安全性和合規性 (Security & Compliance) - ✅ 符合
- ✅ 身份驗證: SSO (LDAP/Active Directory), OAuth 2.0/OIDC
- ✅ 授權控制: RBAC (一般員工/會議室管理員/系統管理員)
- ✅ 資料加密: TLS 1.3 (傳輸), AES-256 (敏感資料)
- ✅ 審計日誌: 所有關鍵操作記錄, 保留 ≥ 1年
- ✅ 合規性: GDPR/個資法, 資料匯出刪除功能
- ✅ 安全掃描: SonarQube, Snyk, OWASP Top 10 防範

### VI. 整合性和可擴展性 (Integration & Scalability) - ✅ 符合
- ✅ 外部系統整合: Outlook (日曆同步), Teams (視訊會議連結), Email 通知
- ✅ API 設計: RESTful + OpenAPI 3.0, SpringDoc Swagger UI
- ✅ 事件驅動架構: RabbitMQ/Kafka 處理非同步任務 (通知、同步)
- ✅ 多站點支援: 資料隔離清晰
- ✅ 可擴展性: 無狀態服務設計, Kubernetes 水平擴展
- ✅ 避免複雜性: YAGNI 原則, 保持架構簡潔

### VII. 架構設計 (Architecture Design) - ✅ 符合
- ✅ 依賴反轉原則: 核心業務邏輯不依賴框架, 使用介面抽象
- ✅ 關注點分離: Presentation → Application → Domain → Infrastructure 清晰分層
- ✅ 領域驅動設計: 核心領域模型 Reservation, Room, User
- ✅ 框架獨立性: 可替換 Spring Boot → Quarkus, 核心邏輯不受影響
- ✅ 模組化設計: 策略模式支援客製化 (Phase 3)

### VIII. 技術棧 (Technology Stack) - ✅ 符合
- ✅ 前端: Vue 3.x Composition API, TypeScript 5.x, PrimeVue, Tailwind CSS, Vite, Pinia
- ✅ 後端: Spring Boot 4.x, Java 25, Spring Security, Spring Data JPA, Hibernate
- ✅ 資料庫: SQL Server 或 PostgreSQL (NEEDS CLARIFICATION - 需選定)
- ✅ 快取: Redis
- ✅ 訊息佇列: RabbitMQ 或 Kafka (NEEDS CLARIFICATION - 需選定)
- ✅ 基礎設施: Docker, Kubernetes, GitHub Actions/GitLab CI, Prometheus + Grafana, ELK Stack

### IX. 平台整合 (Platform Integration) - ✅ 符合
- ✅ 單一後端專案: 所有前端 (Web/行動) 共用同一後端
- ✅ RESTful API 介接: 前端透過 REST API 通訊
- ✅ API 版本控制: URL 版本控制 (`/api/v1/`)
- ✅ CORS 設定: 僅允許授權前端網域
- ✅ API 認證: JWT 包含使用者角色資訊
- ✅ 統一錯誤處理: HTTP 狀態碼 + JSON 錯誤格式

### X. 文檔語言 (Documentation Language) - ✅ 符合
- ✅ 規範文件 (spec.md): 繁體中文
- ✅ 計畫文件 (plan.md): 繁體中文
- ✅ 任務文件 (tasks.md): 繁體中文 (待生成)
- ✅ 程式碼註釋: 繁體中文或英文 (技術術語優先英文)
- ✅ API 文檔: 繁體中文 + 英文 (雙語)
- ✅ 變數/函式命名: 英文 (遵循 camelCase/PascalCase 慣例)

### 憲章檢查結果: ✅ 通過 (10/10 原則符合)

**需要釐清 (NEEDS CLARIFICATION) 的技術選型:**
1. 主資料庫: PostgreSQL 或 SQL Server (兩者皆支援, 需根據企業現有基礎設施決定)
2. 訊息佇列: RabbitMQ 或 Kafka (RabbitMQ 簡單易用, Kafka 高吞吐量)
3. 微前端框架: Single-SPA 或 Module Federation (Single-SPA 成熟穩定, Module Federation 原生整合)

**Phase 0 research.md 需解決的問題:**
- 評估企業現有資料庫授權與維運能力 (PostgreSQL vs SQL Server)
- 評估訊息量與延遲要求 (RabbitMQ vs Kafka)
- 評估前端團隊技術棧與未來擴展計畫 (Single-SPA vs Module Federation)

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

採用 **Option 2: Web application** (前後端分離架構)

```text
imrbs/                                    # 專案根目錄
├── .specify/                             # Speckit 框架目錄
│   ├── memory/
│   │   └── constitution.md               # IMRBS 憲章 v1.0.0
│   ├── scripts/                          # 自動化腳本
│   └── templates/                        # 規範模板
├── specs/                                # 功能規範目錄
│   └── 001-meeting-room-booking/         # 本功能規範
│       ├── spec.md                       # 功能規格 (已完成)
│       ├── plan.md                       # 實作計畫 (本文件)
│       ├── research.md                   # Phase 0 輸出 (待生成)
│       ├── data-model.md                 # Phase 1 輸出 (待生成)
│       ├── quickstart.md                 # Phase 1 輸出 (待生成)
│       ├── contracts/                    # Phase 1 輸出 (待生成)
│       │   ├── openapi.yaml              # OpenAPI 3.0 規格
│       │   └── api-endpoints.md          # API 端點說明
│       ├── checklists/
│       │   └── requirements.md           # 需求檢查清單
│       └── tasks.md                      # Phase 2 輸出 (由 /speckit.tasks 生成)
├── doc/                                  # 文檔目錄
│   └── 需求/                             # 需求文件
│       ├── 憲章.md
│       ├── 會議室預約系統需求.md
│       └── 技術棧與方法論.md
├── imrbs-core/                           # 後端核心模組 (Domain + Application 層)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/tw/huangcti/imrbs/
│       │   │   ├── domain/               # 領域層 (核心業務邏輯, 框架無關)
│       │   │   │   ├── model/            # 領域模型 (Reservation, Room, User)
│       │   │   │   ├── repository/       # Repository 介面 (抽象)
│       │   │   │   ├── service/          # 領域服務 (衝突檢測, 可用性計算)
│       │   │   │   └── event/            # 領域事件 (ReservationCreated, etc.)
│       │   │   └── application/          # 應用層 (Use Cases)
│       │   │       ├── usecase/          # 用例實作 (CreateReservation, etc.)
│       │   │       ├── dto/              # 資料傳輸物件
│       │   │       └── port/             # 介面/埠定義
│       │   └── resources/
│       │       └── data/                 # 初始資料 (開發用)
│       └── test/                         # 單元測試 (JUnit 5 + Mockito)
│           └── java/tw/huangcti/imrbs/
│               ├── domain/               # 領域層測試 (95%+ 覆蓋率)
│               └── application/          # 應用層測試
├── imrbs-infrastructure/                 # 後端基礎設施模組 (新增, Infrastructure 層)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/tw/huangcti/imrbs/infrastructure/
│       │   │   ├── persistence/          # 資料持久化實作
│       │   │   │   ├── jpa/              # JPA/Hibernate 實作
│       │   │   │   │   ├── entity/       # JPA Entities (映射 DB tables)
│       │   │   │   │   └── repository/   # Spring Data JPA Repository 實作
│       │   │   │   └── redis/            # Redis 快取實作
│       │   │   ├── messaging/            # 訊息佇列實作
│       │   │   │   ├── rabbitmq/         # RabbitMQ 整合 (或 kafka/)
│       │   │   │   └── listener/         # 事件監聽器
│       │   │   ├── integration/          # 外部系統整合
│       │   │   │   ├── outlook/          # Outlook API 整合
│       │   │   │   ├── teams/            # Teams API 整合
│       │   │   │   └── email/            # Email 服務整合
│       │   │   └── config/               # 基礎設施配置
│       │   └── resources/
│       │       ├── db/migration/         # Flyway/Liquibase 資料庫遷移腳本
│       │       └── application-infra.yml
│       └── test/                         # 整合測試 (Spring Boot Test)
│           └── java/tw/huangcti/imrbs/infrastructure/
├── imrbs-web/                            # 後端 Web API 模組 (Presentation 層)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/tw/huangcti/imrbs/
│       │   │   ├── ImrbsApplication.java # Spring Boot 主程式
│       │   │   └── web/
│       │   │       ├── controller/       # REST Controllers
│       │   │       │   ├── ReservationController.java
│       │   │       │   ├── RoomController.java
│       │   │       │   └── AdminController.java
│       │   │       ├── dto/              # API Request/Response DTOs
│       │   │       ├── mapper/           # DTO <-> Domain Model 映射器
│       │   │       ├── security/         # Spring Security 配置
│       │   │       │   ├── SecurityConfig.java
│       │   │       │   ├── JwtAuthenticationFilter.java
│       │   │       │   └── SsoIntegration.java
│       │   │       └── exception/        # 全域異常處理
│       │   │           └── GlobalExceptionHandler.java
│       │   └── resources/
│       │       ├── application.yml       # Spring Boot 主配置
│       │       ├── application-dev.yml   # 開發環境配置
│       │       ├── application-sit.yml   # SIT 環境配置
│       │       ├── application-prod.yml  # 生產環境配置
│       │       └── messages.properties   # i18n 訊息 (zh-TW)
│       └── test/                         # API 整合測試
│           └── java/tw/huangcti/imrbs/web/
│               └── controller/           # Controller 測試 (MockMvc)
├── imrbs-frontend/                       # 前端 Vue 3 專案 (新增)
│   ├── package.json
│   ├── vite.config.ts                    # Vite 配置
│   ├── tsconfig.json                     # TypeScript 配置
│   ├── tailwind.config.js                # Tailwind CSS 配置
│   ├── index.html
│   ├── public/                           # 靜態資源
│   │   ├── favicon.ico
│   │   └── locales/                      # i18n JSON 檔案
│   │       ├── zh-TW.json
│   │       └── en.json
│   ├── src/
│   │   ├── main.ts                       # 應用程式進入點
│   │   ├── App.vue                       # 根元件
│   │   ├── router/                       # Vue Router 配置
│   │   │   └── index.ts
│   │   ├── stores/                       # Pinia 狀態管理
│   │   │   ├── auth.ts                   # 認證狀態
│   │   │   ├── reservation.ts            # 預約狀態
│   │   │   └── room.ts                   # 會議室狀態
│   │   ├── components/                   # Vue 元件
│   │   │   ├── common/                   # 通用元件 (Button, Modal, etc.)
│   │   │   ├── layout/                   # 佈局元件 (Header, Footer, Sidebar)
│   │   │   ├── reservation/              # 預約相關元件
│   │   │   │   ├── ReservationForm.vue
│   │   │   │   ├── ReservationList.vue
│   │   │   │   └── CalendarView.vue
│   │   │   ├── room/                     # 會議室相關元件
│   │   │   │   ├── RoomCard.vue
│   │   │   │   ├── RoomDetail.vue
│   │   │   │   └── RoomFilter.vue
│   │   │   └── admin/                    # 管理介面元件
│   │   │       ├── RoomManagement.vue
│   │   │       ├── UserManagement.vue
│   │   │       └── ReportDashboard.vue
│   │   ├── views/                        # 頁面視圖
│   │   │   ├── Home.vue
│   │   │   ├── Login.vue
│   │   │   ├── MyReservations.vue
│   │   │   ├── RoomSearch.vue
│   │   │   └── admin/
│   │   │       ├── AdminDashboard.vue
│   │   │       └── GuestApproval.vue
│   │   ├── services/                     # API 服務層
│   │   │   ├── api.ts                    # Axios 配置
│   │   │   ├── reservation.service.ts
│   │   │   ├── room.service.ts
│   │   │   └── auth.service.ts
│   │   ├── composables/                  # Vue 3 Composables
│   │   │   ├── useAuth.ts
│   │   │   ├── useReservation.ts
│   │   │   └── useI18n.ts
│   │   ├── types/                        # TypeScript 型別定義
│   │   │   ├── reservation.ts
│   │   │   ├── room.ts
│   │   │   └── user.ts
│   │   ├── utils/                        # 工具函式
│   │   │   ├── date.ts
│   │   │   ├── validation.ts
│   │   │   └── formatter.ts
│   │   └── assets/                       # 樣式與圖片
│   │       ├── styles/
│   │       │   └── main.css              # Tailwind 入口
│   │       └── images/
│   └── tests/                            # 前端測試
│       ├── unit/                         # Jest 單元測試
│       │   ├── components/
│       │   └── services/
│       └── e2e/                          # Cypress E2E 測試
│           ├── specs/
│           │   ├── reservation.cy.ts
│           │   ├── room-search.cy.ts
│           │   └── admin.cy.ts
│           └── support/
├── docker/                               # Docker 配置
│   ├── backend.Dockerfile
│   ├── frontend.Dockerfile
│   └── docker-compose.yml                # 本地開發環境
├── kubernetes/                           # Kubernetes 部署檔案
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── backend-deployment.yaml
│   ├── backend-service.yaml
│   ├── frontend-deployment.yaml
│   ├── frontend-service.yaml
│   ├── redis-deployment.yaml
│   ├── rabbitmq-deployment.yaml
│   └── ingress.yaml
├── .github/                              # GitHub Actions CI/CD
│   └── workflows/
│       ├── backend-ci.yml
│       ├── frontend-ci.yml
│       └── deploy-sit.yml
├── scripts/                              # 自動化腳本
│   ├── db-migration.sh
│   ├── deploy.sh
│   └── backup-restore.ps1
├── pom.xml                               # Maven 根 POM (多模組專案)
├── README.md                             # 專案說明
└── .gitignore
```

**Structure Decision**:

採用 **前後端分離 + Clean Architecture 多模組設計**:

1. **後端分層 (Clean Architecture)**:
   - `imrbs-core`: 核心業務邏輯 (Domain + Application 層), 框架無關, 可獨立測試
   - `imrbs-infrastructure`: 基礎設施實作 (Database, Redis, MQ, 外部 API), 依賴 core
   - `imrbs-web`: Web API 層 (Controllers, Security, DTOs), 依賴 core + infrastructure

2. **前端單一專案**:
   - `imrbs-frontend`: Vue 3 SPA, 透過 `/api/v1/*` 介接後端

3. **依賴方向**: `web → infrastructure → core` (嚴格向內依賴, 符合憲章 VII)

4. **已存在的目錄** (`imrbs-core`, `imrbs-web`) 將重構以符合 Clean Architecture

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**本專案無憲章違規, 無需填寫此表。**

所有架構決策皆符合 IMRBS 憲章 v1.0.0 的 10 項核心原則。
