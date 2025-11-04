# speckit 各階段傳入參數

## speckit.constitution

### sample 1
- /speckit.constitution Create principles focused on code quality, testing standards, user experience consistency, and performance requirements for a meeting room booking system. Include governance for how these principles should guide technical decisions, such as prioritizing security in user authentication and integration with company calendars. Ensure all implementations follow TDD and maintain scalability for 100+ concurrent users.

- /speckit.comstitution 為會議室預訂系統制定以程式碼品質、測試標準、使用者體驗一致性和效能要求為核心的原則。明確這些原則如何指導技術決策，例如優先考慮使用者身份驗證的安全性以及與公司日曆的整合。確保所有實作​​都遵循測試驅動開發 (TDD) 原則，並能滿足 100 個以上並髮用戶的可擴充性。

### sample 2
- /speckit.constitution Create a comprehensive project constitution for developing a meeting room booking system using Spec-Driven Development. Focus on the following immutable principles:

1. **Code Quality**: All code must be modular, readable, and follow Clean Code practices (e.g., single responsibility principle, meaningful naming). Use TypeScript for frontend and Java for backend to ensure type safety. Limit dependencies to minimal libraries (e.g., no heavy frameworks unless justified).

2. **Testing Standards**: Adopt strict TDD – write unit/integration tests before implementation, aiming for >95% coverage. Include end-to-end tests for key flows like booking conflicts and notifications. Use tools like Jest/Pytest, and mandate automated CI/CD checks.

3. **User Experience Consistency**: Design intuitive, responsive UI (mobile-first) with drag-and-drop for rescheduling. Ensure accessibility (WCAG 2.1 AA compliance) and multilingual support (English/Chinese). Prioritize feedback loops, like post-booking ratings.

4. **Performance Requirements**: Optimize for <2s response times, support 100+ users via efficient queries (e.g., indexed SQLite database). Implement caching for availability checks and auto-scaling for peak hours.

5. **Security and Compliance**: Enforce role-based access (e.g., admins approve external bookings), data encryption (GDPR-compliant), and audit logs for all actions. Integrate with SSO (e.g., company LDAP) and prevent common vulnerabilities (OWASP Top 10).

6. **Integration and Scalability**: Mandate seamless hooks to external systems (e.g., Google Calendar, door access IoT). Design for future expansion, like multi-site support, without nested complexity.

7. **Documentation Language**:All specifications, plans, and user-facing documentation MUST be written in Traditional Chinese (zh-TW),Code comments and technical documentation MAY use English or Traditional Chinese for technical clarity,Commit messages and internal development notes MAY use Traditional Chinese (zh-TW).

Include governance rules: These principles are non-negotiable and must be referenced in every spec, plan, and task. If a decision conflicts, justify in writing and seek approval. Guide AI decisions toward simplicity (e.g., no over-engineering) and iterative refinement over one-shot generation.

- /speckit.constitution 使用規範驅動開發（Spec-Driven Development）方法，為開發會議室預訂系統制定全面的專案章程。重點在於以下不可變原則：

1. **程式碼品質**：所有程式碼必須模組化、易讀，並遵循整潔程式碼實踐（例如，單一職責原則、有意義的命名）。前端使用 TypeScript，後端使用 Java，以確保型別安全。盡量減少依賴函式庫。

2. **測試標準**：採用嚴格的測試驅動開發（TDD）－在實現之前編寫單元測試/整合測試，目標是達到 95% 以上的覆蓋率。包含關鍵流程（例如預訂衝突和通知）的端到端測試。使用 Jest/Pytest 等工具，並強制執行自動化的持續整合/持續交付（CI/CD）檢查。

3. **使用者體驗一致性**：設計直覺、響應式的使用者介面（行動優先），並支援拖放式重新安排功能。確保可訪問性（符合 WCAG 2.1 AA 標準）和多語言支援（英語/中文）。優先考慮回饋循環，例如預訂後的評分。

4. **效能需求**：最佳化回應時間，使其小於 2 秒；透過高效查詢（例如，使用索引式 sql server or posgresql  資料庫）支援 100 多個使用者。實施可用性檢查的快取機制，並在高峰時段自動擴展。

5. **安全性和合規性**：強制執行基於角色的存取控制（例如，管理員批准外部預訂）、資料加密（符合 GDPR 標準）以及所有操作的審計日誌。整合單一登入 (SSO) 系統（例如，公司 LDAP），並防範常見漏洞（OWASP Top 10）。

6. **整合性和可擴展性**：確保與外部系統（例如，Google 日曆、門禁物聯網）無縫整合。設計時應考慮未來的擴展，例如支援多站點，避免嵌套複雜性。

7. **文檔語言**：所有規範、計劃和麵向用戶的文檔必須用繁體中文（zh-TW）編寫，代碼註釋和技術文檔為了技術清晰性可以使用英文或繁體中文，提交信息和內部開發說明可以使用繁體中文（zh-TW）。

包含治理規則：這些原則不容協商，必須在每份規範、計畫和任務中明確。如果決策有衝突，請以書面說明理由並尋求批准。引導人工智慧決策朝著簡潔性方向發展（例如，避免過度設計），並透過迭代改進而非一次性生成。


## speckit.specify 

### sample1

- /speckit.specify Build a web-based Meeting Room Booking System for a mid-sized company with 200 employees and 15 meeting rooms across 3 floors.

#### Core User Stories:

1. **As an employee**, I can view all meeting rooms with real-time availability (calendar grid view) so I can quickly find a free room.
2. **As an employee**, I can book a room by selecting date, time range, room, and purpose, with auto-conflict detection.
3. **As an employee**, I can invite other employees (by email or name) and they receive email + in-app notifications.
4. **As an employee**, I can modify or cancel my bookings (with restrictions: cannot cancel <2 hours before).
5. **As a department admin**, I can pre-approve external visitor bookings or large meetings (>10 people).
6. **As any user**, I receive reminders 30 mins before the meeting via email and push notification.
7. **As an admin**, I can generate usage reports (daily/weekly occupancy, peak hours, underutilized rooms).
8. **As the system**, it auto-cancels "no-show" bookings if no one checks in within 10 minutes (via QR code or NFC tap).

#### Key Constraints:
- No user login required for demo; use predefined user list (5 sample users: 1 PM, 4 engineers).
- Data stored locally in SQLite (no cloud, no auth).
- Responsive web app: works on desktop and mobile.
- Support recurring bookings (weekly team meeting).
- Drag-and-drop to reschedule on calendar view.
- Multilingual: English and Traditional Chinese.
- Accessibility: WCAG 2.1 AA compliant.

Focus on **what the system does** and **why**, not tech stack. Generate clear, testable user stories and acceptance criteria.

- /speckit.specify 為一家擁有 200 名員工、分佈在三層樓的 15 間會議室的中型公司建立一個基於 Web 的會議室預訂系統。

#### 核心使用者故事：

1. **身為員工**，我可以查看所有會議室的即時可用情況（日曆網格視圖），以便快速找到空閒的會議室。

2. **身為員工**，我可以透過選擇日期、時間範圍、會議室和用途來預訂會議室，並自動偵測衝突。

3. **作為員工**，我可以邀請其他員工（透過電子郵件或姓名），他們會收到電子郵件和應用程式內通知。

4. **身為員工**，我可以修改或取消我的預訂（有限制：不能在預訂開始前 2 小時內取消）。

5. **作為部門管理員**，我可以預先批准外部訪客的預訂或大型會議（10 人以上）。

6. **身為一般使用者**，我會在會議開始前 30 分鐘收到電子郵件和推播通知提醒。

7. **作為管理員**，我可以產生使用情況報告（每日/每週入住率、高峰時段、未充分利用的房間）。

8. **作為系統**，如果 10 分鐘內無人簽到（透過二維碼或 NFC 近場通訊），系統會自動取消「未到場」的預訂。

#### 主要限制：

- 示範無需使用者登入；使用預先定義的使用者清單（5 個範例使用者：1 位專案經理，4 位工程師）。

- 資料儲存在本機 SQLite 資料庫中（無雲端，無需身份驗證）。

- 響應式 Web 應用：可在桌面和行動裝置上運作。

- 支援重複預訂（例如每週團隊會議）。

- 可在行事曆檢視中拖曳重新安排會議。

- 支援多語言：英文和繁體中文。

- 無障礙：符合 WCAG 2.1 AA 標準。

關注系統**做什麼**和**為什麼這樣做**，而不是技術棧。編寫清晰、可測試的使用者故事和驗收標準。

## speckit.plan

### sample1

- /speckit.plan Use Vite + React (TypeScript) for frontend, Node.js + Express for backend, SQLite with Prisma ORM. 
- Frontend: Tailwind CSS, React Calendar, drag-and-drop with @dnd-kit, TanStack Query for caching.
- Backend: REST API with rate limiting, WebSocket for real-time updates.
- Local dev: `npm run dev` starts both.
- No auth (use mock user context), but structure for future SSO.
- Deployable as static site + serverless functions.
- Include unit tests (Vitest), e2e tests (Playwright), and CI workflow.

- /speckit.plan 前端使用 Vite + React（TypeScript），後端使用 Node.js + Express，資料庫使用 SQLite 和 Prisma ORM。

- 前端：Tailwind CSS、React Calendar、使用 @dnd-kit 實作拖放功能、使用 TanStack Query 進行快取。

- 後端：REST API 帶有速率限制，WebSocket 用於即時更新。

- 本機開發：`npm run dev` 即可啟動前端和後端。

- 無需身份驗證（使用模擬使用者上下文），但預留了未來單一登入 (SSO) 的架構。

- 可部署為靜態網站和無伺服器函數。

- 包含單元測試（Vitest）、端對端測試（Playwright）和持續整合 (CI) 工作流程。

### sample2





千泰使用傳入參數

/speckit.constitution

會議室預約系統製定開發原則，重點關注：簡潔的程式碼、全面的測試、美觀的富用戶介面、持久化的 JSON 儲存。本專案所有規格都需要採用中文。
 
/speckit.specify 建立一個名為「imrbs」的會議室預約系統應用程式。功能包括：

1. 會議室預約登錄及修改，會議室地點包含板橋與民生；板橋有3間會議室：6樓太平洋會議室、7樓711會議室、7樓701會議室。民生有2間會議室：301會議室、310會議室。

2. 會議室清單與預約狀態顯示。

3. 會議室預約衝突檢查。

4. 預約成功，系統發送email至預約者的信箱。

5. 會議室管理功能。
 
/speckit.plan 使用Java21、套件管理用Maven，前端使用Vue3，後端使用Springboot，使用版本為最新的穩定版本。程序專案位於 imrbs/pom.xml。網站程式置於imrbs/imrbs-web/，共用實體及服務置/imrbs/imrbs-core/。測試位於各自的tests/ 目錄。JSON資料儲存在應用程式主目錄中。
 
/speckit.tasks
 
/speckit.implement

 
不過中間我有下一些動作沒記錄到  下次我用簡單的技術線html+node.js試試，應該會更快   這次中間有很多ai產生的指令出錯，但他自己矯正重試到正確為止，我只要一直同意給他權限執行就好。
 
specs:
 1-imrbs-app 

