<!--
============================================================================
Sync Impact Report - Constitution Update
============================================================================
Version Change: Template → 1.0.0 (MINOR: Initial constitution establishment)

Modified Principles:
- Added 10 core principles for IMRBS (Meeting Room Booking System)

Added Sections:
- I. 程式碼品質 (Code Quality)
- II. 測試標準 (Testing Standards) - NON-NEGOTIABLE
- III. 使用者體驗一致性 (UX Consistency)
- IV. 效能需求 (Performance Requirements)
- V. 安全性和合規性 (Security & Compliance)
- VI. 整合性和可擴展性 (Integration & Scalability)
- VII. 架構設計 (Architecture Design)
- VIII. 技術棧 (Technology Stack)
- IX. 平台整合 (Platform Integration)
- X. 文檔語言 (Documentation Language)
- Technical Stack Requirements (Section 2)
- Governance Rules (Section 3)

Template Updates:
✅ spec-template.md - Verified compatibility with prioritized user stories
✅ plan-template.md - Verified constitution check section alignment
✅ tasks-template.md - Verified task categorization supports all principles
⚠ Commands in .specify/templates/commands/*.md - Review pending
⚠ Runtime guidance docs - Update pending if exists

Follow-up TODOs:
- None - All placeholders filled

Rationale:
This is the initial establishment of the IMRBS project constitution, defining
10 non-negotiable principles for meeting room booking system development using
Spec-Driven Development methodology. Version set to 1.0.0 as this is the first
complete, ratified version of the constitution.
============================================================================
-->

# IMRBS 會議室預約系統憲章 (IMRBS Constitution)

## 核心原則 (Core Principles)

### I. 程式碼品質 (Code Quality)

**所有程式碼必須遵循以下不可變規則：**

- **模組化與整潔程式碼**：嚴格遵循單一職責原則（Single Responsibility Principle）、有意義的命名規範、適當的抽象層次
- **型別安全**：前端使用 TypeScript，後端使用 Java，強制型別檢查，禁止使用 `any` 型別或原始型別
- **最小化依賴**：盡量減少外部函式庫依賴，優先使用標準庫，每個新依賴必須經過審核批准
- **程式碼審查**：所有程式碼必須經過至少一名資深開發者審查才能合併
- **可讀性優先**：程式碼必須自我說明，複雜邏輯必須附帶清晰的註釋（繁體中文或英文）

**理由**：高品質的程式碼基礎是專案長期可維護性的保證，型別安全可以在編譯時期捕捉大量錯誤，減少執行時期問題。

---

### II. 測試標準 (Testing Standards) - **不可協商 (NON-NEGOTIABLE)**

**嚴格的測試驅動開發（TDD）流程：**

1. **測試先行**：實現任何功能前必須先編寫測試案例
2. **用戶驗收**：測試案例必須經過產品負責人或用戶代表驗收
3. **紅綠重構**：遵循 Red-Green-Refactor 循環（測試失敗 → 實現 → 測試通過 → 重構）
4. **覆蓋率要求**：單元測試覆蓋率必須達到 95% 以上，整合測試覆蓋所有 API 端點
5. **E2E 測試**：關鍵流程（預訂衝突檢測、通知系統、權限控制）必須有端到端測試
6. **測試工具**：
   - 前端：Jest + Vue Test Utils + Cypress（E2E）
   - 後端：JUnit 5 + Mockito + Spring Boot Test
7. **CI/CD 整合**：所有測試必須在 CI/CD 管線中自動執行，失敗則阻止合併

**理由**：TDD 確保每個功能都經過驗證，減少回歸錯誤，提高程式碼信心。這是專案品質的基石，絕不妥協。

---

### III. 使用者體驗一致性 (UX Consistency)

**使用者介面設計必須遵循：**

- **行動優先（Mobile-First）**：所有介面必須先設計行動版本，再擴展到桌面版
- **響應式設計**：支援各種螢幕尺寸（手機、平板、桌面）
- **直覺互動**：支援拖放式預約重新安排、日曆視圖快速操作
- **無障礙性（Accessibility）**：符合 WCAG 2.1 AA 標準，支援鍵盤導航、螢幕閱讀器
- **多語言支援**：介面支援繁體中文、英文，可擴展至其他語言
- **回饋機制**：提供預訂後評分、使用體驗回饋功能
- **一致性**：使用統一的設計系統（PrimeVue + Tailwind CSS）

**理由**：一致且友善的使用者體驗可以提高系統採用率和使用者滿意度，減少培訓成本。

---

### IV. 效能需求 (Performance Requirements)

**系統效能必須滿足以下可測試標準：**

- **回應時間**：API 回應時間 < 2 秒（P95），關鍵操作（查詢會議室可用性）< 500ms
- **並發支援**：支援至少 100 個並發使用者，系統不降級
- **資料庫最佳化**：使用索引式查詢（SQL Server 或 PostgreSQL），複雜查詢必須有執行計畫分析
- **快取策略**：
  - 會議室可用性檢查使用 Redis 快取（TTL 30 秒）
  - 靜態資源使用 CDN 快取
- **自動擴展**：在高峰時段（如上午 9-10 點）自動水平擴展（Kubernetes HPA）
- **效能監控**：使用 APM 工具（如 New Relic、Datadog）持續監控，設定告警閾值

**理由**：效能直接影響使用者體驗，必須設定可測量的標準並持續監控。

---

### V. 安全性和合規性 (Security & Compliance)

**安全性是不可妥協的基本要求：**

- **身份驗證**：整合單一登入（SSO）系統（公司 LDAP/Active Directory），支援 OAuth 2.0/OIDC
- **授權控制**：實施基於角色的存取控制（RBAC）：
  - 一般員工：預訂自己的會議室、查看可用性
  - 會議室管理員：管理會議室資訊、批准外部訪客預訂
  - 系統管理員：完整系統控制、使用者管理
- **資料加密**：
  - 傳輸中加密：強制 HTTPS/TLS 1.3
  - 靜態資料加密：敏感資料（個人資訊）使用 AES-256 加密
- **審計日誌**：記錄所有關鍵操作（預訂、取消、權限變更），保留至少 1 年
- **合規性**：符合 GDPR、個資法要求，提供資料匯出和刪除功能
- **安全掃描**：
  - 防範 OWASP Top 10 漏洞
  - 定期進行滲透測試和程式碼安全掃描（如 SonarQube、Snyk）

**理由**：會議室系統涉及員工個人資訊和公司機密會議，安全性和合規性是法律和信任的基礎。

---

### VI. 整合性和可擴展性 (Integration & Scalability)

**系統必須具備良好的整合能力和可擴展性：**

- **外部系統整合**：
  - Microsoft Outlook：預訂同步到 Outlook 日曆
  - Microsoft Teams：支援 Teams 會議室預訂、視訊會議連結自動生成
  - 企業通訊系統：通知整合（Email、即時訊息）
- **API 設計**：RESTful API 遵循 OpenAPI 3.0 規範，提供完整的 API 文檔（Swagger UI）
- **事件驅動架構**：使用訊息佇列（RabbitMQ/Kafka）處理非同步任務（通知、日曆同步）
- **多站點支援**：架構支援多個辦公室/分公司，資料隔離清晰
- **可擴展性**：
  - 水平擴展：無狀態服務設計，支援多實例部署
  - 垂直擴展：關鍵服務支援增加資源
- **避免複雜性**：避免過度設計和嵌套邏輯，保持架構簡潔

**理由**：良好的整合能力提升系統價值，可擴展性確保系統能隨業務成長。

---

### VII. 架構設計 (Architecture Design)

**架構必須遵循以下核心原則：**

- **依賴反轉原則（Dependency Inversion Principle）**：
  - 核心業務邏輯和實體不依賴任何外部框架
  - 使用介面和抽象定義契約，實現可替換性
- **關注點分離（Separation of Concerns）**：
  - 清晰的分層架構：Presentation → Application → Domain → Infrastructure
  - 每層職責明確，不跨層調用
- **領域驅動設計（DDD）**：
  - 核心領域模型：Reservation（預訂）、Room（會議室）、User（使用者）
  - 領域服務封裝業務邏輯（如衝突檢測、通知）
- **框架獨立性**：
  - 平台底層可動態更換相似框架（如 Spring Boot → Quarkus）
  - 核心業務邏輯不受框架版本升級影響
- **模組化設計**：
  - 不同客戶（銀行）可有不同介面設計和實作
  - 使用策略模式或外掛機制支援客製化

**理由**：良好的架構設計確保系統長期可維護、可測試、可擴展，降低技術債務。

---

### VIII. 技術棧 (Technology Stack)

**專案必須使用以下經過驗證的技術棧：**

**前端：**

- Vue 3.x（最新穩定版，使用 Composition API）
- TypeScript 5.x
- PrimeVue（UI 元件庫）
- Tailwind CSS（樣式框架）
- Vite（建置工具）
- Pinia（狀態管理）

**後端：**

- Spring Boot 4.x
- Java 25
- Spring Security（安全框架）
- Spring Data JPA（資料存取）
- Hibernate（ORM）

**資料庫：**

- 主資料庫：SQL Server 或 PostgreSQL（擇一）
- 快取：Redis
- 訊息佇列：RabbitMQ 或 Kafka（擇一）

**基礎設施：**

- 容器化：Docker
- 編排：Kubernetes
- CI/CD：GitHub Actions 或 GitLab CI
- 監控：Prometheus + Grafana
- 日誌：ELK Stack（Elasticsearch + Logstash + Kibana）

**理由**：使用成熟穩定的技術棧降低風險，確保社群支援和長期維護性。

---

### IX. 平台整合 (Platform Integration)

**前後端整合必須遵循：**

- **單一後端專案**：所有前端應用（Web、行動）共用同一個後端專案
- **RESTful API 介接**：前端透過 RESTful API 與後端通訊，遵循 REST 原則
- **API 版本控制**：使用 URL 版本控制（如 `/api/v1/`），確保向後相容
- **CORS 設定**：正確設定跨域資源共用，僅允許授權的前端網域
- **API 認證**：使用 JWT（JSON Web Token）進行 API 認證，Token 包含使用者角色資訊
- **統一錯誤處理**：API 回傳統一的錯誤格式（HTTP 狀態碼 + 錯誤訊息 JSON）

**理由**：統一的平台架構簡化維護，減少重複開發，確保一致的業務邏輯。

---

### X. 文檔語言 (Documentation Language)

**文檔必須遵循以下語言規範：**

- **規範文件（Spec）**：繁體中文（zh-TW）
- **計畫文件（Plan）**：繁體中文（zh-TW）
- **任務文件（Tasks）**：繁體中文（zh-TW）
- **使用者文檔**：繁體中文（zh-TW）
- **程式碼註釋**：繁體中文（zh-TW）或英文（技術術語優先英文）
- **提交訊息（Commit Messages）**：繁體中文（zh-TW）
- **API 文檔**：繁體中文（zh-TW）+ 英文（雙語）
- **技術文檔**：繁體中文（zh-TW）為主，技術術語可保留英文
- **變數/函式命名**：英文（遵循各語言慣例）

**理由**：統一的文檔語言確保團隊溝通順暢，減少誤解，提高協作效率。

---

## 技術棧要求 (Technology Stack Requirements)

**必須使用的核心技術：**

1. **前端框架**：Vue 3.x（最新穩定版），使用 Composition API 和 TypeScript
2. **UI 框架**：PrimeVue + Tailwind CSS
3. **後端框架**：Spring Boot 4.x + Java 25
4. **資料庫**：SQL Server 或 PostgreSQL（需在規劃階段確定）
5. **快取層**：Redis（用於會議室可用性快取）
6. **訊息佇列**：RabbitMQ 或 Kafka（用於非同步通知）
7. **容器化**：Docker + Kubernetes
8. **測試工具**：
   - 前端：Jest + Vue Test Utils + Cypress
   - 後端：JUnit 5 + Mockito + Spring Boot Test

**技術決策原則：**

- 優先選擇成熟穩定的技術
- 避免使用實驗性或不穩定的版本
- 新技術引入必須經過技術委員會批准
- 每個技術選擇必須有明確的理由和替代方案分析

---

## 治理規則 (Governance)

**憲章的權威性和執行：**

1. **最高優先級**：本憲章優先於所有其他開發實踐和規範
2. **強制遵循**：所有規範（Spec）、計畫（Plan）、任務（Tasks）必須明確符合憲章原則
3. **審查機制**：
   - 所有 Pull Request 必須驗證是否符合憲章
   - Code Review 必須檢查是否違反憲章原則
   - 定期進行憲章合規性審計（每季度一次）
4. **衝突處理**：
   - 如決策與憲章衝突，必須以書面說明理由
   - 提交給技術委員會審批
   - 獲得批准後才能執行
5. **修訂流程**：
   - 憲章修訂必須經過技術委員會投票（2/3 多數通過）
   - 修訂必須包含版本號更新、理由說明、影響分析
   - 重大修訂（MAJOR 版本）需要遷移計畫
6. **複雜性控制**：
   - 所有複雜決策必須有明確的理由和替代方案分析
   - 優先選擇簡單方案（YAGNI 原則：You Aren't Gonna Need It）
   - 避免過度設計和提前最佳化
7. **迭代改進**：
   - 優先迭代開發而非一次性完整實現
   - 每個迭代都必須交付可用的增量功能
   - 根據回饋持續改進

**版本控制：**

- 遵循語義化版本控制（Semantic Versioning）：MAJOR.MINOR.PATCH
  - **MAJOR**：不向後相容的治理規則變更或原則移除/重新定義
  - **MINOR**：新增原則/章節或重大擴展
  - **PATCH**：澄清、措辭修正、非語義化的精煉

**執行指引：**

- 開發過程中的具體指引參考 `.github/prompts/`、`.github/chatmodes/`、`.github/instructions/` 目錄
- 本憲章定義「做什麼」（WHAT），指引文件定義「怎麼做」（HOW）

---

**版本**: 1.0.0 | **批准日期**: 2025-11-20 | **最後修訂**: 2025-11-20
