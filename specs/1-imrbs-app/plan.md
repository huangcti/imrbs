# Implementation Plan: imrbs 會議室預約系統

**Branch**: `1-imrbs-app` | **Date**: 2025-11-03 | **Spec**: `/specs/1-imrbs-app/spec.md`

## Technical Context

**Language/Version**: Java 21 (LTS)  
**Primary Dependencies**:  
- 後端: Spring Boot 3.2.x (最新穩定版)
  - Spring Web
  - Spring Validation
  - Spring Mail (郵件通知)
  - Jackson (JSON 處理)
  - Lombok (簡化開發)
  - JUnit 5 (測試框架)
  - Mockito (測試替身)
- 前端: Vue 3.x (最新穩定版)
  - Vue Router
  - Pinia (狀態管理)
  - Vite (建置工具)
  - Vitest (單元測試)
  - Vue Test Utils
  - Cypress (E2E測試)

**Storage**: JSON files in application home directory
- 會議室資料: `${app.home}/data/rooms.json`
- 預約資料: `${app.home}/data/reservations.json`

**Testing**:  
- 後端: JUnit 5 + Mockito
- 前端: Vitest + Vue Test Utils + Cypress
- 整合測試: JUnit + TestContainers (選用)

**Target Platform**: Web (瀏覽器 + Java 伺服器)  
**Project Type**: Web application (前後端分離)

**Performance Goals**: 
- API 回應時間 < 1s (p95)
- 預約衝突檢查 < 500ms
- UI 互動延遲 < 200ms

**Constraints**:
- 所有儲存操作必須有版本欄位
- JSON 檔案需支援備份/還原
- 遵循憲章簡潔性原則

**Scale/Scope**:
- 會議室數量: < 100
- 每日預約數: < 1000
- 並行使用者: < 100

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

依據憲章必須檢核的項目：

1. 規格語言：
   - [x] 所有 spec 與需求文件採用中文
   - [x] plan.md 內技術文件可保留英文術語但須附中文說明

2. 測試覆蓋：
   - [x] 後端：配置 JUnit + Mockito，覆蓋核心業務邏輯
   - [x] 前端：配置 Vitest + Vue Test Utils，覆蓋元件與狀態
   - [x] E2E：配置 Cypress 確保主要使用流程
   - [x] 已規劃測試目錄結構

3. 儲存方案：
   - [x] 採用 JSON 檔案儲存
   - [x] schema_version 已規劃
   - [x] 備份/還原機制已規劃
   - [x] 檔案路徑結構合理

4. UI 要求：
   - [x] 規劃使用 Vue 3 元件架構
   - [x] 已考慮無障礙需求 (ARIA 標記)
   - [x] 回饋機制明確（錯誤、載入、成功）

5. 簡潔性檢核：
   - [x] 資料夾結構符合職責分離
   - [x] 共用邏輯置於 core 模組
   - [x] 相依性最小化，僅引入必要套件

檢核結果：通過所有門檻。未發現需要額外說明的複雜度增加。

## Project Structure

### Documentation

```text
specs/1-imrbs-app/
├── plan.md              # This file
├── research.md          # Phase 0: 技術選型與研究文件
├── data-model.md        # Phase 1: 資料模型與JSON結構
├── quickstart.md        # Phase 1: 快速啟動指南
└── contracts/           # Phase 1: API契約與介面定義
```

### Source Code 

```text
imrbs/                      # 根目錄
├── pom.xml                 # 主要 Maven 配置
├── data/                   # JSON 資料目錄
│   ├── rooms.json         # 會議室資料
│   └── reservations.json  # 預約資料
│
├── imrbs-core/            # 共用核心模組
│   ├── pom.xml           # Core 模組 Maven 配置
│   ├── src/
│   │   └── main/java/
│   │       └── tw/com/iisi/imrbs/
│   │           ├── model/       # 實體類別
│   │           ├── service/     # 共用服務
│   │           └── util/        # 工具類別
│   └── tests/            # Core 測試
│
└── imrbs-web/           # Web 應用模組
    ├── pom.xml          # Web 模組 Maven 配置
    ├── src/
    │   └── main/
    │       ├── java/    # 後端程式
    │       │   └── tw/com/iisi/imrbs/
    │       │       ├── config/    # Spring 配置
    │       │       ├── web/       # Web 控制器
    │       │       └── mail/      # 郵件服務
    │       └── ui/      # 前端程式 (Vue)
    │           ├── src/
    │           │   ├── components/
    │           │   ├── views/
    │           │   └── services/
    │           ├── tests/
    │           │   ├── unit/      # 前端單元測試
    │           │   └── e2e/       # E2E 測試
    │           └── package.json
    └── tests/           # 後端測試
```

**結構說明**：
1. 核心邏輯與實體置於 `imrbs-core`
2. Web 應用與 UI 置於 `imrbs-web`
3. JSON 檔案儲存於主目錄的 `data/`
4. 測試分層：單元、整合、E2E

## Phase 0: Research Tasks

1. JSON 資料結構與版本控制
   - Schema 設計與版本欄位規劃
   - 檔案讀寫與併發處理機制
   - 備份還原策略

2. 前後端架構整合
   - Vue 3 與 Spring Boot 專案配置
   - 開發環境設定 (CORS, proxy)
   - 建置流程規劃

3. 測試策略
   - 單元測試配置與範例
   - 整合測試環境設定
   - E2E 測試流程規劃

## Phase 1: Core Implementation

1. 資料模型與持久層
   - Room, Reservation 實體類別
   - JSON 讀寫服務
   - 版本控制與遷移機制

2. 業務邏輯層
   - 預約服務 (建立/修改/取消)
   - 衝突檢查邏輯
   - 郵件通知服務

3. API層
   - REST endpoints 實作
   - 輸入驗證
   - 錯誤處理

## Phase 2: Web UI Implementation

1. 基礎架構
   - Vue 專案配置
   - 路由設定
   - API 客戶端服務

2. 核心元件
   - 會議室列表/狀態顯示
   - 預約表單
   - 時間選擇器

3. 管理介面
   - 會議室管理
   - 預約管理
   - 系統設定

## Phase 3: Testing & Quality

1. 單元測試
   - 核心服務測試
   - UI 元件測試
   - Mock 設定

2. 整合測試
   - API 端點測試
   - 預約流程測試
   - 郵件發送測試

3. E2E測試
   - 主要使用情境測試
   - 錯誤處理測試

## Development Workflow

1. 本地開發
```bash
# 後端
cd imrbs-web
mvn spring-boot:run

# 前端
cd imrbs-web/src/ui
npm install
npm run dev
```

2. 測試執行
```bash
# 後端測試
mvn test

# 前端測試
cd imrbs-web/src/ui
npm run test:unit
npm run test:e2e
```

## Deployment Notes

1. 配置檔案位置：
   - `application.properties`: 主要設定
   - `application-dev.properties`: 開發環境
   - `application-prod.properties`: 生產環境

2. 環境變數：
   - `IMRBS_DATA_DIR`: JSON 檔案目錄
   - `IMRBS_MAIL_*`: 郵件伺服器設定

3. 部署檢查清單：
   - [ ] 確認 Java 21 環境
   - [ ] 設定資料目錄權限
   - [ ] 配置郵件伺服器
   - [ ] 設定 CORS 允許來源
   - [ ] 確認備份機制

## Migration Guide

JSON Schema 遷移步驟：
1. 備份現有 JSON 檔案
2. 執行遷移腳本
3. 驗證新版本欄位
4. 更新應用程式

## Notes

- 遵循 Java 21 最佳實作
- 確保所有輸入驗證
- 紀錄檔規劃待補充
- 效能監控待規劃
