# IMRBS - Integrated Meeting Room Booking System

會議室預約系統 - 採用 Clean Architecture 與 TDD 方法論開發

## 🚀 快速開始

### 前置需求

- **後端**: JDK 25, Maven 3.9+
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

- **前端**: http://localhost:3000
- **後端 API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **RabbitMQ 管理介面**: http://localhost:15672 (imrbs_user/imrbs_pass)

## 📁 專案結構

```
imrbs/
├── imrbs-core/              # 領域層 + 應用層 (框架無關)
│   ├── domain/              # 領域模型、Repository 介面、領域服務
│   └── application/         # Use Cases (業務邏輯編排)
├── imrbs-infrastructure/    # 基礎設施層
│   ├── persistence/         # JPA 實體、Repository 實作
│   ├── messaging/           # RabbitMQ 整合
│   └── integration/         # Email、Outlook、Teams 整合
├── imrbs-web/               # 表現層
│   ├── controller/          # REST Controllers
│   ├── security/            # Spring Security, OAuth 2.0, JWT
│   └── dto/                 # API Request/Response DTOs
├── imrbs-frontend/          # Vue 3 前端
│   ├── components/          # Vue 元件
│   ├── stores/              # Pinia 狀態管理
│   ├── router/              # Vue Router 路由
│   └── services/            # API 服務層
└── docker/                  # Docker Compose 開發環境
```

## 🏗️ 技術棧

### 後端
- **框架**: Spring Boot 3.4.0, Java 25
- **資料庫**: PostgreSQL 16
- **快取**: Redis 7
- **訊息佇列**: RabbitMQ 3.13
- **認證**: Spring Security, OAuth 2.0/OIDC, JWT
- **測試**: JUnit 5, Mockito, Testcontainers
- **文件**: SpringDoc OpenAPI 3.0

### 前端
- **框架**: Vue 3 (Composition API), TypeScript 5
- **UI 元件**: PrimeVue, Tailwind CSS
- **狀態管理**: Pinia
- **路由**: Vue Router
- **多語系**: vue-i18n (zh-TW, en)
- **測試**: Vitest, Cypress

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

## 📝 開發進度

### ✅ Phase 1: Setup (完成 7/7)
- [X] T001: Clean Architecture 多模組結構
- [X] T002: Maven POM 配置
- [X] T003: Vue 3 專案初始化
- [X] T004: Linting 工具配置
- [X] T005: Docker Compose 開發環境
- [X] T006: GitHub Actions CI/CD
- [X] T007: Spring Boot 主應用程式

### 🔄 Phase 2: Foundational (進行中 0/42)
- [ ] T008-T015: 資料庫遷移 (6張表 + 種子資料)
- [ ] T016-T021: 領域模型 (6個實體)
- [ ] T022-T027: Repository 介面
- [ ] T028-T033: JPA 實作
- [ ] T034-T037: 安全認證基礎設施
- [ ] T038-T042: 共用基礎設施 (Redis, RabbitMQ, 異常處理)
- [ ] T043-T049: 前端基礎架構

### ⏳ Phase 3-5: MVP (待開始 61個任務)
- US1: 員工查詢與預約會議室 (26個任務)
- US2: 員工修改與取消預約 (17個任務)
- US3: SSO 單一登入整合 (18個任務)

## 📚 相關文件

- [憲章](doc/需求/憲章.md) - IMRBS 專案的 10 項核心原則
- [功能規格](specs/001-meeting-room-booking/spec.md) - 8個使用者故事、31個功能需求
- [實作計畫](specs/001-meeting-room-booking/plan.md) - 技術棧、架構設計
- [數據模型](specs/001-meeting-room-booking/data-model.md) - 6個核心實體設計
- [API 合約](specs/001-meeting-room-booking/contracts/api-endpoints.md) - RESTful API 規格
- [任務清單](specs/001-meeting-room-booking/tasks.md) - 199個任務的詳細分解

## 🤝 貢獻指南

1. 所有開發必須遵循 **TDD (Red-Green-Refactor)** 流程
2. 測試覆蓋率必須達到 **95%+** (NON-NEGOTIABLE)
3. 程式碼必須通過 **Checkstyle** 和 **ESLint** 檢查
4. 每個 PR 必須經過 **Code Review**
5. Commit 訊息格式: `feat/fix/docs/test: [TaskID] Description`

## 📄 授權

Copyright © 2025 IMRBS Team. All rights reserved.
