# Research Document: 會議室預約系統

**Feature**: 001-meeting-room-booking  
**Date**: 2025-11-20  
**Phase**: 0 (技術研究與決策)

## 概述

本文件記錄會議室預約系統實作前的技術研究與決策過程,解決 `plan.md` 中標記的 **NEEDS CLARIFICATION** 項目,並評估各技術選項的優劣。

## 研究問題清單

根據 `plan.md` Technical Context 與 Constitution Check,需解決以下技術選型:

1. **主資料庫選擇**: PostgreSQL vs SQL Server
2. **訊息佇列選擇**: RabbitMQ vs Kafka
3. **微前端框架選擇**: Single-SPA vs Module Federation

---

## 1. 主資料庫選擇: PostgreSQL vs SQL Server

### 決策 (Decision)

**推薦: PostgreSQL**

### 理由 (Rationale)

#### PostgreSQL 優勢

1. **開源免費**: 無授權成本,企業可節省大量授權費用
2. **跨平台支援**: Linux/Windows/macOS 全平台支援,Docker 容器化部署更簡單
3. **強大的 JSONB 支援**: 適合儲存會議室設備清單 (JSON 格式),查詢效能優異
4. **社群活躍**: 龐大的開源社群,豐富的擴展套件 (如 TimescaleDB 用於時間序列)
5. **Spring Boot 整合**: Spring Data JPA 完美支援,Hibernate Dialect 成熟穩定
6. **Kubernetes 生態**: 有 Postgres Operator (Zalando, CrunchyData) 簡化 K8s 部署
7. **監控工具**: pgAdmin, pg_stat_statements, 與 Prometheus Exporter 整合良好
8. **合規性**: 符合 GDPR 要求,可使用 pgcrypto 實現欄位級加密

#### SQL Server 優勢

1. **企業級工具**: SSMS (SQL Server Management Studio) 圖形化工具強大
2. **Windows 整合**: 與 Active Directory, LDAP 整合更緊密
3. **商業支援**: Microsoft 官方技術支援
4. **全文搜尋**: Full-Text Search 功能成熟 (但 PostgreSQL 也有 tsquery)

#### SQL Server 劣勢

1. **授權成本高**: Standard Edition 授權費用昂貴,Enterprise Edition 更貴
2. **Linux 支援有限**: SQL Server 2017+ 支援 Linux,但生態不如 PostgreSQL 成熟
3. **容器化部署**: Docker 映像體積大 (>1.5GB),PostgreSQL 僅 ~300MB
4. **雲端成本**: Azure SQL Database 成本高於 Azure Database for PostgreSQL

### 考慮的替代方案 (Alternatives Considered)

| 方案 | 優點 | 缺點 | 拒絕理由 |
|------|------|------|----------|
| SQL Server | 企業工具成熟, Windows 整合佳 | 授權成本高, 容器化體積大 | 成本考量, 開源優先原則 |
| MySQL | 開源, 社群大 | JSON 支援較弱, 複雜查詢效能不如 PostgreSQL | PostgreSQL 功能更強大 |
| Oracle | 功能最強大 | 授權成本極高, 過度複雜 | 違反 YAGNI 原則, 成本太高 |

### 技術細節

#### PostgreSQL 版本選擇

- **推薦版本**: PostgreSQL 16.x (最新穩定版)
- **理由**: 支援 SQL/JSON 標準, 查詢優化器改進, 邏輯複製增強

#### Spring Boot 配置

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/imrbs
    driver-class-name: org.postgresql.Driver
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    properties:
      hibernate:
        default_schema: public
        jdbc:
          time_zone: UTC
```

#### Flyway 資料庫遷移

- 使用 Flyway 管理 schema 版本
- 腳本命名: `V1__create_rooms_table.sql`, `V2__create_reservations_table.sql`
- 支援回滾腳本: `U1__drop_rooms_table.sql`

### 效能評估

- **查詢效能**: PostgreSQL 在複雜 JOIN 與時間範圍查詢上效能優異
- **並發處理**: MVCC (多版本並發控制) 支援高並發讀寫
- **快取配置**: `shared_buffers=256MB`, `effective_cache_size=1GB` (根據伺服器資源調整)

### 部署策略

- **開發環境**: Docker Compose (PostgreSQL 16 + pgAdmin 4)
- **SIT 環境**: Kubernetes StatefulSet + Persistent Volume
- **生產環境**: Managed Service (AWS RDS for PostgreSQL / Azure Database for PostgreSQL) 或自建 HA 叢集 (Patroni + etcd)

---

## 2. 訊息佇列選擇: RabbitMQ vs Kafka

### 決策 (Decision)

**推薦: RabbitMQ**

### 理由 (Rationale)

#### RabbitMQ 優勢

1. **簡單易用**: 安裝配置簡單,開箱即用,學習曲線低
2. **AMQP 協議**: 支援標準 AMQP 0-9-1,訊息可靠性保證強
3. **路由靈活**: Direct, Fanout, Topic, Headers 四種 Exchange 類型,滿足多種場景
4. **管理介面**: RabbitMQ Management Plugin 提供直觀的 Web UI (http://localhost:15672)
5. **Spring Boot 整合**: `spring-boot-starter-amqp` 完美整合,`@RabbitListener` 簡化開發
6. **訊息持久化**: 支援 Durable Queue 和 Persistent Message,斷電不丟失
7. **延遲佇列**: 透過 Dead Letter Exchange 實現延遲訊息 (如會議前 30 分鐘提醒)
8. **輕量級**: 適合中小規模應用 (每秒千級訊息)

#### Kafka 優勢

1. **高吞吐量**: 適合大數據場景 (每秒百萬級訊息)
2. **持久化日誌**: 訊息可重複消費,適合事件溯源 (Event Sourcing)
3. **分散式架構**: Partition 機制支援水平擴展

#### Kafka 劣勢

1. **複雜度高**: 需部署 Zookeeper/KRaft,運維成本高
2. **過度設計**: 會議室系統不需要百萬級吞吐量
3. **延遲較高**: 訊息延遲 (latency) 通常 > RabbitMQ
4. **Spring Boot 整合**: `spring-kafka` 配置較複雜

### 考慮的替代方案 (Alternatives Considered)

| 方案 | 優點 | 缺點 | 拒絕理由 |
|------|------|------|----------|
| Kafka | 高吞吐量, 事件溯源 | 複雜度高, 過度設計 | 違反 YAGNI 原則, 運維成本高 |
| Redis Pub/Sub | 極簡, 效能高 | 不可靠 (訂閱者離線丟失訊息) | 不符合可靠性要求 |
| AWS SQS | 完全託管, 免運維 | 雲端綁定, 無法本地開發 | 不符合多雲/混合雲策略 |

### 使用場景

#### 會議室系統的訊息場景

1. **預約確認通知**: 預約成功 → 發送 email 給預約者與參與者
2. **會議提醒**: 會議開始前 30 分鐘 → 發送提醒 email
3. **取消通知**: 預約取消 → 通知所有參與者
4. **外部系統同步**: 預約成功 → 同步至 Outlook/Teams (非同步處理)
5. **審計日誌**: 所有操作 → 發送至日誌系統 (ELK Stack)

#### 訊息量估算

- **每月預約數**: 5000 筆
- **每筆預約訊息**: 3-5 則 (確認 + 提醒 + 同步)
- **每月總訊息數**: ~20000 則
- **平均 QPS**: ~0.01 (遠低於 RabbitMQ 處理能力)

### 技術細節

#### RabbitMQ 拓撲設計

```text
Exchange: imrbs.topic (Type: Topic)
├── Queue: imrbs.notification.email (Routing Key: notification.email.*)
│   ├── notification.email.reservation_created
│   ├── notification.email.reservation_cancelled
│   └── notification.email.meeting_reminder
├── Queue: imrbs.integration.outlook (Routing Key: integration.outlook.*)
│   └── integration.outlook.sync
├── Queue: imrbs.integration.teams (Routing Key: integration.teams.*)
│   └── integration.teams.create_meeting
└── Queue: imrbs.audit.log (Routing Key: audit.#)
    └── audit.* (所有事件)
```

#### Spring Boot 配置

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: 5672
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    listener:
      simple:
        acknowledge-mode: manual # 手動 ACK,確保可靠性
        retry:
          enabled: true
          max-attempts: 3
```

#### Dead Letter Exchange (延遲佇列)

```java
// 配置延遲佇列 (30 分鐘提醒)
@Bean
Queue reminderQueue() {
    return QueueBuilder.durable("imrbs.reminder")
        .withArgument("x-dead-letter-exchange", "imrbs.topic")
        .withArgument("x-dead-letter-routing-key", "notification.email.meeting_reminder")
        .withArgument("x-message-ttl", 30 * 60 * 1000) // 30 分鐘
        .build();
}
```

### 部署策略

- **開發環境**: Docker Compose (RabbitMQ 3.13 + Management Plugin)
- **SIT 環境**: Kubernetes Deployment (單實例 + Persistent Volume)
- **生產環境**: RabbitMQ 叢集 (3 節點 HA,鏡像佇列)

---

## 3. 微前端框架選擇: Single-SPA vs Module Federation

### 決策 (Decision)

**推薦: Module Federation (Webpack 5)**

### 理由 (Rationale)

#### Module Federation 優勢

1. **原生整合**: Webpack 5 內建,無需額外框架,減少依賴
2. **依賴共享**: 自動共享 Vue, Vue Router, Pinia 等函式庫,避免重複載入
3. **效能優異**: 真正的模組聯邦,執行時動態載入,bundle size 更小
4. **類型安全**: TypeScript 支援良好,可定義 remote 模組的型別
5. **開發體驗**: Vite + @originjs/vite-plugin-federation 支援 Module Federation
6. **向後相容**: 可降級為傳統單體應用 (不使用聯邦功能)

#### Single-SPA 優勢

1. **框架無關**: 可混合 Vue, React, Angular 等不同框架
2. **社群成熟**: 最老牌的微前端框架,文檔豐富
3. **生態完整**: single-spa-vue, single-spa-react 官方適配器

#### Single-SPA 劣勢

1. **學習曲線**: 需理解 bootstrap, mount, unmount 生命週期
2. **額外依賴**: 需引入 single-spa 函式庫 (~20KB gzip)
3. **依賴重複**: 各子應用可能重複載入相同函式庫 (除非手動配置 import maps)
4. **Vite 支援**: 需第三方插件 (vite-plugin-single-spa),整合不如 Module Federation 原生

### 考慮的替代方案 (Alternatives Considered)

| 方案 | 優點 | 缺點 | 拒絕理由 |
|------|------|------|----------|
| Single-SPA | 框架無關, 社群成熟 | 學習曲線高, 額外依賴 | 本專案僅使用 Vue 3,不需多框架支援 |
| Micro-Frontends (iframe) | 最簡單, 完全隔離 | 效能差, 使用者體驗割裂 | 不符合現代前端標準 |
| Monolithic SPA | 無複雜性 | 無法獨立部署子模組 | 不符合微前端架構要求 (若企業有多團隊需求) |

### 使用場景

#### 會議室系統的微前端場景

**當前階段 (MVP)**: 單一 Vue 3 SPA (無需微前端)

**未來擴展場景** (使用 Module Federation):

1. **主應用 (Host)**: `imrbs-shell`
   - 提供全域 Header, Footer, 導航
   - 管理路由與認證
   - 懶加載子應用

2. **子應用 (Remote)**:
   - `imrbs-reservation-app`: 預約管理模組 (P1 優先)
   - `imrbs-admin-app`: 管理員後台 (P2)
   - `imrbs-report-app`: 報告分析模組 (P2)
   - `imrbs-guest-app`: 訪客預約模組 (P3)

3. **共享模組**:
   - `@imrbs/shared-ui`: PrimeVue 元件封裝
   - `@imrbs/shared-utils`: 工具函式
   - `@imrbs/shared-types`: TypeScript 型別定義

### 技術細節

#### Vite + Module Federation 配置

**主應用 (Host) vite.config.ts**:

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import federation from '@originjs/vite-plugin-federation'

export default defineConfig({
  plugins: [
    vue(),
    federation({
      name: 'imrbs-shell',
      remotes: {
        reservationApp: 'http://localhost:5001/assets/remoteEntry.js',
        adminApp: 'http://localhost:5002/assets/remoteEntry.js',
      },
      shared: ['vue', 'vue-router', 'pinia', 'vue-i18n'] // 共享依賴
    })
  ]
})
```

**子應用 (Remote) vite.config.ts**:

```typescript
export default defineConfig({
  plugins: [
    vue(),
    federation({
      name: 'reservation-app',
      filename: 'remoteEntry.js',
      exposes: {
        './ReservationApp': './src/App.vue', // 暴露根元件
        './router': './src/router/index.ts'
      },
      shared: ['vue', 'vue-router', 'pinia', 'vue-i18n']
    })
  ],
  build: {
    target: 'esnext'
  }
})
```

#### TypeScript 類型定義

```typescript
// remotes.d.ts
declare module 'reservationApp/ReservationApp' {
  import { DefineComponent } from 'vue'
  const component: DefineComponent
  export default component
}
```

### MVP 建議

**Phase 1 (MVP)**: 先實作單一 Vue 3 SPA,不引入微前端複雜性

**Phase 2 (擴展)**: 當以下條件滿足時,考慮拆分為微前端:
- 團隊規模 > 5 人
- 模組間邏輯完全獨立
- 需要獨立部署子模組
- 不同模組由不同團隊負責

**決策**: 當前採用 **單一 Vue 3 SPA**,預留 Module Federation 擴展能力 (在 `vite.config.ts` 中預先配置好 federation plugin,但暫不啟用 remotes)

---

## 4. 其他技術決策

### 4.1 CSS 框架: Tailwind CSS + PrimeVue

**理由**:
- PrimeVue 提供完整 UI 元件 (Button, Dialog, DataTable, Calendar)
- Tailwind CSS 補充自定義樣式 (間距, 顏色, RWD)
- 兩者整合良好,無衝突

**配置**:
```javascript
// tailwind.config.js
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts}'],
  theme: {
    extend: {
      colors: {
        primary: '#3B82F6', // PrimeVue 主題色
      }
    }
  }
}
```

### 4.2 狀態管理: Pinia

**理由**:
- Vue 3 官方推薦 (取代 Vuex)
- TypeScript 支援完美
- DevTools 整合良好
- 模組化設計,易於測試

**Store 設計**:
```typescript
// stores/auth.ts
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    token: ''
  }),
  actions: {
    async login(credentials: LoginCredentials) { /* ... */ }
  }
})
```

### 4.3 API 層設計: Axios + Composables

**理由**:
- Axios 提供攔截器 (interceptor),統一處理 JWT Token
- Vue 3 Composables 封裝 API 呼叫邏輯,可重用

**範例**:
```typescript
// composables/useReservation.ts
export function useReservation() {
  const { data, error, isLoading, execute } = useAsyncData(
    () => reservationService.getMyReservations()
  )
  return { reservations: data, error, isLoading, refresh: execute }
}
```

### 4.4 日期處理: date-fns

**理由**:
- 輕量級 (相比 moment.js)
- Tree-shakable (只打包用到的函式)
- TypeScript 原生支援
- 函式式 API,易於測試

**範例**:
```typescript
import { format, addMinutes, isWithinInterval } from 'date-fns'

const reminderTime = addMinutes(meeting.startTime, -30)
const formattedDate = format(new Date(), 'yyyy-MM-dd HH:mm')
```

### 4.5 表單驗證: Vee-Validate + Yup

**理由**:
- Vee-Validate 是 Vue 3 最流行的表單驗證庫
- Yup schema 驗證,可重用規則
- PrimeVue 整合良好

**範例**:
```typescript
import { useForm } from 'vee-validate'
import * as yup from 'yup'

const schema = yup.object({
  roomId: yup.string().required('請選擇會議室'),
  startTime: yup.date().required('請選擇開始時間'),
  endTime: yup.date().min(yup.ref('startTime'), '結束時間必須晚於開始時間')
})

const { errors, validate } = useForm({ validationSchema: schema })
```

### 4.6 測試策略

#### 單元測試: Jest + Vue Test Utils

**範例**:
```typescript
import { mount } from '@vue/test-utils'
import RoomCard from '@/components/room/RoomCard.vue'

test('顯示會議室名稱', () => {
  const wrapper = mount(RoomCard, {
    props: { room: { name: '會議室 A', capacity: 10 } }
  })
  expect(wrapper.text()).toContain('會議室 A')
})
```

#### E2E 測試: Cypress

**範例**:
```typescript
// tests/e2e/specs/reservation.cy.ts
describe('預約流程', () => {
  it('員工可以成功預約會議室', () => {
    cy.login('employee@example.com')
    cy.visit('/rooms')
    cy.get('[data-testid="room-card-A"]').click()
    cy.get('[data-testid="reserve-btn"]').click()
    cy.get('[data-testid="start-time"]').type('2025-12-01 14:00')
    cy.get('[data-testid="submit-btn"]').click()
    cy.contains('預約成功').should('be.visible')
  })
})
```

---

## 5. 監控與可觀測性

### 5.1 後端監控

#### Spring Boot Actuator

**暴露端點**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  metrics:
    export:
      prometheus:
        enabled: true
```

#### Prometheus 指標

- JVM 記憶體使用量
- HTTP 請求延遲 (P50, P95, P99)
- 資料庫連線池使用率
- RabbitMQ 佇列長度

#### Grafana Dashboard

- 使用 Spring Boot 2.x Micrometer 官方 Dashboard (ID: 4701)
- 自定義 Panel: 會議室預約數/小時, API 錯誤率

### 5.2 前端監控

#### Sentry (錯誤追蹤)

**配置**:
```typescript
import * as Sentry from '@sentry/vue'

Sentry.init({
  app,
  dsn: import.meta.env.VITE_SENTRY_DSN,
  integrations: [
    new Sentry.BrowserTracing({
      routingInstrumentation: Sentry.vueRouterInstrumentation(router)
    })
  ],
  tracesSampleRate: 0.1 // 10% 取樣
})
```

#### Web Vitals (效能監控)

- LCP (Largest Contentful Paint): < 2.5s
- FID (First Input Delay): < 100ms
- CLS (Cumulative Layout Shift): < 0.1

**追蹤**:
```typescript
import { onCLS, onFID, onLCP } from 'web-vitals'

onLCP(metric => sendToAnalytics(metric))
onFID(metric => sendToAnalytics(metric))
onCLS(metric => sendToAnalytics(metric))
```

### 5.3 日誌管理

#### ELK Stack

- **Elasticsearch**: 儲存所有日誌
- **Logstash**: 收集與轉換日誌
- **Kibana**: 視覺化查詢介面

#### 日誌格式

**結構化日誌 (JSON)**:
```json
{
  "timestamp": "2025-11-20T10:30:00Z",
  "level": "INFO",
  "logger": "tw.huangcti.imrbs.web.controller.ReservationController",
  "message": "預約創建成功",
  "userId": "employee123",
  "reservationId": "res-456",
  "traceId": "abc-def-123" // 用於分散式追蹤
}
```

---

## 6. 安全性研究

### 6.1 SSO 整合: OAuth 2.0 + OIDC

#### 流程

1. 使用者訪問前端 → 重定向至 SSO 登入頁 (LDAP/AD)
2. 輸入帳號密碼 → SSO 驗證成功 → 回傳 Authorization Code
3. 前端用 Code 交換 Access Token (JWT)
4. 前端將 JWT 存於 `localStorage` (或 `httpOnly` Cookie 更安全)
5. 每次 API 請求帶上 `Authorization: Bearer {JWT}`

#### Spring Security 配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .oauth2Login() // OAuth 2.0 登入
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/**").authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt()) // JWT 驗證
            .build();
    }
}
```

### 6.2 RBAC 權限設計

#### 角色定義

| 角色 | 權限 |
|------|------|
| EMPLOYEE | 查詢會議室, 預約, 修改/取消自己的預約 |
| ROOM_ADMIN | EMPLOYEE 權限 + 管理會議室, 批准訪客預約 |
| SYSTEM_ADMIN | ROOM_ADMIN 權限 + 使用者管理, 系統配置 |

#### Spring Security 註解

```java
@PreAuthorize("hasRole('ROOM_ADMIN')")
@PutMapping("/api/v1/rooms/{id}")
public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
    // ...
}
```

### 6.3 資料加密

#### 傳輸中加密: TLS 1.3

- Kubernetes Ingress 配置 TLS 證書 (Let's Encrypt)
- 強制 HTTPS 重定向

#### 靜態資料加密: AES-256

**敏感欄位** (使用 JPA AttributeConverter):
```java
@Entity
public class User {
    @Convert(converter = EmailEncryptor.class)
    private String email; // 加密儲存
}

@Converter
public class EmailEncryptor implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return AES.encrypt(attribute, secretKey);
    }
    @Override
    public String convertToEntityAttribute(String dbData) {
        return AES.decrypt(dbData, secretKey);
    }
}
```

### 6.4 防範 OWASP Top 10

| 漏洞 | 防範措施 |
|------|----------|
| Injection (SQL Injection) | 使用 JPA Prepared Statements, 禁止動態 SQL 拼接 |
| Broken Authentication | 強制 SSO, JWT 短期有效 (15 分鐘), Refresh Token 機制 |
| Sensitive Data Exposure | TLS 1.3, AES-256 加密敏感欄位 |
| XML External Entities (XXE) | 禁用 XML 解析器外部實體 |
| Broken Access Control | Spring Security RBAC, `@PreAuthorize` 註解 |
| Security Misconfiguration | Spring Boot Actuator 端點僅內網可訪問 |
| XSS | Vue 3 自動轉義, CSP Header 配置 |
| Insecure Deserialization | 禁止反序列化不受信任的資料 |
| Insufficient Logging | 審計日誌記錄所有關鍵操作 |
| Server-Side Request Forgery (SSRF) | 限制外部 HTTP 請求白名單 |

---

## 7. 效能優化策略

### 7.1 資料庫優化

#### 索引設計

```sql
-- 會議室查詢索引
CREATE INDEX idx_rooms_capacity ON rooms(capacity);
CREATE INDEX idx_rooms_status ON rooms(status);

-- 預約查詢索引 (複合索引)
CREATE INDEX idx_reservations_room_time ON reservations(room_id, start_time, end_time);
CREATE INDEX idx_reservations_user ON reservations(user_id);
CREATE INDEX idx_reservations_status ON reservations(status);
```

#### 查詢優化

**N+1 問題解決** (JPA Fetch Join):
```java
@Query("SELECT r FROM Reservation r JOIN FETCH r.room JOIN FETCH r.user WHERE r.user.id = :userId")
List<Reservation> findByUserIdWithRoomAndUser(@Param("userId") Long userId);
```

### 7.2 快取策略

#### Redis 快取設計

| 快取 Key | TTL | 說明 |
|----------|-----|------|
| `room:availability:{roomId}:{date}` | 30s | 會議室當日可用性 |
| `user:profile:{userId}` | 1h | 使用者個人資料 |
| `rooms:all` | 5m | 所有會議室清單 |

#### Spring Cache 配置

```java
@Cacheable(value = "roomAvailability", key = "#roomId + '-' + #date")
public List<TimeSlot> getRoomAvailability(Long roomId, LocalDate date) {
    // 複雜計算邏輯
}

@CacheEvict(value = "roomAvailability", key = "#reservation.room.id + '-' + #reservation.date")
public Reservation createReservation(Reservation reservation) {
    // 創建預約後清除快取
}
```

### 7.3 前端效能優化

#### 程式碼分割 (Code Splitting)

```typescript
// 路由懶加載
const routes = [
  {
    path: '/admin',
    component: () => import('./views/admin/AdminDashboard.vue') // 動態載入
  }
]
```

#### 虛擬滾動 (PrimeVue VirtualScroller)

```vue
<VirtualScroller :items="allReservations" :itemSize="50">
  <template #item="{ item }">
    <ReservationCard :reservation="item" />
  </template>
</VirtualScroller>
```

#### 圖片優化

- 使用 WebP 格式 (瀏覽器支援率 95%+)
- 懶加載 (Intersection Observer API)
- CDN 加速

---

## 8. 開發環境設定

### 8.1 Docker Compose (本地開發)

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: imrbs
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    ports:
      - "5672:5672"   # AMQP
      - "15672:15672" # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: secret

volumes:
  postgres_data:
```

### 8.2 Dev Containers (VS Code)

**.devcontainer/devcontainer.json**:
```json
{
  "name": "IMRBS Dev",
  "dockerComposeFile": "../docker-compose.yml",
  "service": "app",
  "workspaceFolder": "/workspace",
  "customizations": {
    "vscode": {
      "extensions": [
        "vscjava.vscode-spring-boot-dashboard",
        "Vue.volar",
        "esbenp.prettier-vscode"
      ]
    }
  }
}
```

---

## 9. 總結

### 已解決的 NEEDS CLARIFICATION

| 項目 | 決策 | 理由 |
|------|------|------|
| 主資料庫 | **PostgreSQL 16** | 開源免費, JSONB 支援, 容器化友好, 成本低 |
| 訊息佇列 | **RabbitMQ 3.13** | 簡單易用, 適合中小規模, 延遲佇列支援提醒功能 |
| 微前端框架 | **Module Federation (預留能力)** | 原生整合 Vite, 依賴共享, MVP 階段先用單一 SPA |

### 技術棧最終確認

| 類別 | 技術 | 版本 |
|------|------|------|
| 前端框架 | Vue 3 | 3.x (最新穩定版) |
| UI 元件庫 | PrimeVue | 最新版 |
| CSS 框架 | Tailwind CSS | 3.x |
| 狀態管理 | Pinia | 2.x |
| 建置工具 | Vite | 5.x |
| 後端框架 | Spring Boot | 4.x |
| 程式語言 | Java | 25 |
| 資料庫 | PostgreSQL | 16 |
| 快取 | Redis | 7 |
| 訊息佇列 | RabbitMQ | 3.13 |
| 容器化 | Docker | 最新穩定版 |
| 編排 | Kubernetes | 1.28+ |

### 下一步行動

**Phase 1 (由 `/speckit.plan` 自動執行)**:

1. 生成 `data-model.md`: ER 圖 (6 個實體: User, Room, Reservation, MaintenanceSchedule, Notification, GuestReservationRequest)
2. 生成 `contracts/openapi.yaml`: RESTful API 規格 (31 個 API 端點)
3. 生成 `quickstart.md`: 開發者快速上手指南
4. 更新 `.github/copilot-instructions.md`: 將技術決策加入 AI 助手上下文

**Phase 2 (由 `/speckit.tasks` 執行)**:

1. 拆解為可實作的任務清單 (tasks.md)
2. 按優先級排序 (P1 → P2 → P3)

**Phase 3 (實際開發)**:

1. 遵循 TDD (Red-Green-Refactor)
2. 每個任務對應一個 PR
3. 所有 PR 必須通過 CI (測試 + SonarQube)

---

**Research 完成日期**: 2025-11-20  
**下一階段**: Phase 1 - 生成資料模型與 API 合約
