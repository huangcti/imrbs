# IMRBS - Integrated Meeting Room Booking System

會議室預約系統 - 採用 Clean Architecture 與 TDD 方法論開發

[![Build Status](https://github.com/your-org/imrbs/workflows/CI/badge.svg)](https://github.com/your-org/imrbs/actions)
[![Coverage](https://img.shields.io/badge/coverage-95%25-brightgreen)](https://github.com/your-org/imrbs)
[![License](https://img.shields.io/badge/license-proprietary-blue)](LICENSE)

## 🚀 快速開始

### 前置需求

- **後端**: JDK 21+, Maven 3.9+
- **前端**: Node.js 20+, npm 10+
- **開發環境**: Docker & Docker Compose

### 啟動開發環境

```bash
# 1. 啟動基礎設施服務 (PostgreSQL, Redis, RabbitMQ)
cd docker
docker-compose up -d

# 2. 啟動後端 (Spring Boot)
mvn spring-boot:run -pl imrbs-web

# 3. 啟動前端 (Vue 3)
cd imrbs-frontend
npm install
npm run dev
```

### 訪問應用程式

| 服務 | URL | 說明 |
|------|-----|------|
| **前端** | http://localhost:5173 | Vue 3 開發伺服器 |
| **後端 API** | http://localhost:8080 | Spring Boot REST API |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API 文件 |
| **Actuator** | http://localhost:8080/actuator | 健康檢查與監控 |
| **RabbitMQ** | http://localhost:15672 | imrbs_user/imrbs_pass |

## 📁 專案結構 (Clean Architecture)

```
imrbs/
├── imrbs-core/              # 領域層 + 應用層 (框架無關)
│   ├── domain/              # 領域模型、Repository 介面、領域服務
│   │   ├── model/           # Reservation, Room, User 等實體
│   │   ├── repository/      # Repository 介面定義
│   │   └── service/         # 領域服務 (ConflictDetectionService)
│   └── application/         # Use Cases (業務邏輯編排)
│       ├── usecase/         # CreateReservationUseCase, CancelReservationUseCase
│       └── dto/             # Application DTOs
├── imrbs-infrastructure/    # 基礎設施層
│   ├── persistence/         
│   │   ├── jpa/             # JPA 實體與 Repository 實作
│   │   └── redis/           # Redis 快取服務
│   ├── messaging/           # RabbitMQ 整合
│   └── integration/         # Email、Outlook、Teams 整合
├── imrbs-web/               # 表現層
│   ├── controller/          # REST Controllers
│   ├── security/            # Spring Security, OAuth 2.0, JWT
│   │   ├── RateLimitingFilter
│   │   └── SecurityHeadersFilter
│   ├── actuator/            # 自訂健康檢查與指標
│   └── dto/                 # API Request/Response DTOs
├── imrbs-frontend/          # Vue 3 前端
│   ├── components/          # Vue 元件 (common/, layout/, room/, reservation/)
│   ├── stores/              # Pinia 狀態管理
│   ├── router/              # Vue Router (Lazy Loading)
│   ├── services/            # API 服務層
│   └── locales/             # i18n 語系檔 (zh-TW, en)
├── k8s/                     # Kubernetes 部署配置
│   ├── namespace.yaml       # 命名空間與資源配額
│   ├── configmap.yaml       # 應用程式配置
│   ├── deployment.yaml      # 後端部署
│   ├── service.yaml         # 服務暴露
│   └── autoscaling.yaml     # HPA 自動擴展
└── docker/                  # Docker 配置
    ├── docker-compose.yml   # 開發環境
    ├── nginx.conf           # Nginx 主配置
    └── default.conf         # Nginx 站點配置
```

## 🏗️ 技術棧

### 後端
| 類別 | 技術 | 版本 |
|------|------|------|
| **框架** | Spring Boot | 3.4.0 |
| **語言** | Java | 21 |
| **資料庫** | PostgreSQL | 16 |
| **快取** | Redis | 7 |
| **訊息佇列** | RabbitMQ | 3.13 |
| **認證** | Spring Security + OAuth 2.0/OIDC + JWT | - |
| **監控** | Spring Boot Actuator + Micrometer + Prometheus | - |
| **測試** | JUnit 5 + Mockito + Testcontainers + ArchUnit | - |
| **文件** | SpringDoc OpenAPI | 3.0 |

### 前端
| 類別 | 技術 | 版本 |
|------|------|------|
| **框架** | Vue 3 (Composition API) | 3.5+ |
| **語言** | TypeScript | 5.x |
| **UI 元件** | PrimeVue + Tailwind CSS | - |
| **狀態管理** | Pinia | - |
| **路由** | Vue Router | - |
| **多語系** | vue-i18n | zh-TW, en |
| **測試** | Vitest + Cypress | - |
| **建置** | Vite | - |

## 🧪 測試

### 後端測試
```bash
# 執行所有測試
mvn test

# 執行測試並生成覆蓋率報告
mvn test jacoco:report

# 檢查 95% 覆蓋率要求
mvn jacoco:check

# 執行 Checkstyle 檢查
mvn checkstyle:check

# 執行 ArchUnit 架構測試
mvn test -Dtest="*ArchitectureTest,*LayerTest,*ConventionsTest"
```

### 前端測試
```bash
cd imrbs-frontend

# 單元測試
npm run test:unit

# E2E 測試
npm run test:e2e

# Lint 檢查
npm run lint
```

## 🚀 部署指南

### Docker 部署

#### 建置映像

```bash
# 建置後端映像
docker build -t imrbs-backend:latest .

# 建置前端映像
docker build -f Dockerfile.frontend -t imrbs-frontend:latest .
```

#### 執行容器

```bash
# 執行後端
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://db:5432/imrbs \
  -e DATABASE_USERNAME=imrbs \
  -e DATABASE_PASSWORD=secret \
  -e REDIS_HOST=redis \
  -e RABBITMQ_HOST=rabbitmq \
  --name imrbs-backend \
  imrbs-backend:latest

# 執行前端
docker run -d \
  -p 80:80 \
  --name imrbs-frontend \
  imrbs-frontend:latest
```

### Kubernetes 部署

#### 前置需求

- Kubernetes 叢集 (1.25+)
- kubectl 已配置
- 已建立必要的 Secrets

#### 部署步驟

```bash
# 1. 建立命名空間
kubectl apply -f k8s/namespace.yaml

# 2. 建立 ConfigMap
kubectl apply -f k8s/configmap.yaml

# 3. 建立 Secrets (需先手動建立)
kubectl create secret generic imrbs-secrets \
  --from-literal=database-password=<password> \
  --from-literal=redis-password=<password> \
  --from-literal=rabbitmq-password=<password> \
  --from-literal=jwt-secret=<secret> \
  -n imrbs

# 4. 部署應用程式
kubectl apply -f k8s/deployment.yaml

# 5. 建立服務
kubectl apply -f k8s/service.yaml

# 6. 設定自動擴展
kubectl apply -f k8s/autoscaling.yaml

# 7. 驗證部署
kubectl get pods -n imrbs
kubectl get svc -n imrbs
```

#### 環境變數

| 變數名稱 | 說明 | 預設值 |
|---------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 啟用的 Spring Profile | `prod` |
| `DATABASE_URL` | PostgreSQL 連線 URL | - |
| `DATABASE_USERNAME` | 資料庫使用者 | - |
| `DATABASE_PASSWORD` | 資料庫密碼 | - |
| `REDIS_HOST` | Redis 主機位址 | `localhost` |
| `REDIS_PORT` | Redis 連接埠 | `6379` |
| `RABBITMQ_HOST` | RabbitMQ 主機位址 | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ 連接埠 | `5672` |
| `JWT_SECRET` | JWT 簽名密鑰 | - |
| `KEYCLOAK_URL` | Keycloak 伺服器 URL | - |

### 監控與維運

#### 健康檢查端點

| 端點 | 說明 |
|------|------|
| `/actuator/health` | 整體健康狀態 |
| `/actuator/health/liveness` | 存活探測 |
| `/actuator/health/readiness` | 就緒探測 |
| `/actuator/info` | 應用程式資訊 |
| `/actuator/metrics` | 指標數據 |
| `/actuator/prometheus` | Prometheus 格式指標 |

#### 自訂業務指標

- `imrbs.reservations.created.total` - 預約建立總數
- `imrbs.reservations.cancelled.total` - 預約取消總數
- `imrbs.conflicts.detected.total` - 衝突檢測次數
- `imrbs.room.search.duration` - 會議室搜尋耗時

#### 日誌查看

```bash
# Kubernetes 環境
kubectl logs -f deployment/imrbs-backend -n imrbs

# Docker 環境
docker logs -f imrbs-backend
```

### 故障排除

#### 常見問題

1. **應用程式無法啟動**
   - 檢查資料庫連線: `kubectl exec -it <pod> -n imrbs -- curl localhost:8080/actuator/health/db`
   - 檢查環境變數是否正確設定
   - 查看啟動日誌: `kubectl logs <pod> -n imrbs`

2. **Redis 連線失敗**
   - 確認 Redis 服務正在運行
   - 檢查網路策略是否允許連線
   - 驗證密碼配置

3. **RabbitMQ 訊息堆積**
   - 檢查消費者是否正常運作
   - 監控佇列深度: RabbitMQ Management UI
   - 考慮增加消費者數量

4. **Rate Limiting 觸發**
   - 預設限制: 60 requests/minute/IP
   - 檢查是否有異常流量
   - 調整 `RateLimitingFilter` 配置

## 📝 開發進度

### ✅ 已完成功能

- **US1**: 員工查詢與預約會議室
- **US2**: 員工修改與取消預約
- **US3**: SSO 單一登入整合
- **US4**: 預約衝突檢測與通知
- **US5**: 會議室設備與容量篩選
- **US6**: 預約通知系統
- **US7**: 管理員會議室管理
- **US8**: 多語系支援 (zh-TW, en)

### ✅ 技術實作

- Clean Architecture 多模組結構
- Redis 快取策略
- Rate Limiting & Security Headers
- Prometheus Metrics 監控
- 結構化 JSON 日誌
- ArchUnit 架構測試
- Kubernetes 部署配置

## 📚 相關文件

- [憲章](doc/需求/憲章.md) - IMRBS 專案的 10 項核心原則
- [功能規格](specs/001-meeting-room-booking/spec.md) - 8個使用者故事、31個功能需求
- [實作計畫](specs/001-meeting-room-booking/plan.md) - 技術棧、架構設計
- [數據模型](specs/001-meeting-room-booking/data-model.md) - 6個核心實體設計
- [API 合約](specs/001-meeting-room-booking/contracts/api-endpoints.md) - RESTful API 規格
- [任務清單](specs/001-meeting-room-booking/tasks.md) - 199個任務的詳細分解
- [快速入門](specs/001-meeting-room-booking/quickstart.md) - 開發環境設定指南

## 🤝 貢獻指南

### 開發流程

1. 從 `main` 分支建立功能分支: `git checkout -b feature/xxx`
2. 遵循 **TDD (Red-Green-Refactor)** 開發流程
3. 確保測試覆蓋率達到 **95%+**
4. 通過 **Checkstyle** 和 **ESLint** 檢查
5. 提交 PR 並通過 Code Review

### Commit 訊息格式

```
<type>(<scope>): [TaskID] <description>

Types: feat, fix, docs, style, refactor, test, chore
Examples:
  feat(reservation): [T050] add conflict detection
  fix(auth): [T125] fix token refresh issue
  docs(readme): update deployment guide
```

### 程式碼審查清單

- [ ] 遵循 Clean Architecture 層級規範
- [ ] 單元測試覆蓋新功能
- [ ] 無 Checkstyle/ESLint 錯誤
- [ ] API 變更更新 Swagger 文件
- [ ] 敏感資訊無硬編碼

## 📄 授權

Copyright © 2025 IMRBS Team. All rights reserved.
