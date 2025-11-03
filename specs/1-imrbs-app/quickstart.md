# IMRBS 快速啟動指南

本文件說明如何快速設置並啟動會議室預約系統（IMRBS）的開發環境。

## 系統需求

### 必要環境
- Java 21 (LTS)
- Maven 3.9+
- Node.js 20+
- Git

### 建議工具
- IntelliJ IDEA 或 Eclipse（Java IDE）
- VS Code（前端開發）
- Postman（API 測試）

## 專案結構

```
imrbs/               # 根目錄
├── pom.xml          # 主要 Maven 配置
├── imrbs-core/      # 核心模組
└── imrbs-web/       # Web 應用模組
```

## 快速啟動步驟

### 1. 取得程式碼

```bash
git clone https://github.com/huangcti/imrbs.git
cd imrbs
git checkout 1-imrbs-app
```

### 2. 建置後端

```bash
# 安裝所有 Maven 依賴
mvn clean install

# 啟動應用程式（開發模式）
cd imrbs-web
mvn spring-boot:run
```

後端將在 http://localhost:8080 啟動

### 3. 啟動前端開發伺服器

```bash
# 安裝前端依賴
cd imrbs-web/src/ui
npm install

# 啟動開發伺服器
npm run dev
```

前端將在 http://localhost:5173 啟動

## 設定開發環境

### 1. 應用程式配置

建立 `application-dev.properties`：

```properties
# 資料目錄
app.data.dir=./data

# 郵件設定（開發用）
spring.mail.host=localhost
spring.mail.port=25
spring.mail.username=test
spring.mail.password=test

# CORS（開發用）
app.cors.allowed-origins=http://localhost:5173
```

### 2. 初始測試資料

在 `data/` 目錄建立初始 JSON 檔案：

```bash
mkdir -p data
cp src/test/resources/initial/*.json data/
```

### 3. 設定 IDE

#### IntelliJ IDEA
1. 匯入 Maven 專案
2. 安裝 Lombok 外掛
3. 啟用 annotation processing

#### VS Code
1. 安裝 Volar 外掛（Vue 3）
2. 安裝 ESLint 與 Prettier

## 測試環境

### 執行測試

```bash
# 後端測試
mvn test

# 前端單元測試
cd imrbs-web/src/ui
npm run test:unit

# 前端 E2E 測試
npm run test:e2e
```

### 測試郵件伺服器（選用）

```bash
# 使用 Mailhog（Docker）
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# 更新 application-dev.properties
spring.mail.host=localhost
spring.mail.port=1025
```

郵件 UI 介面：http://localhost:8025

## 開發工作流程

### 1. 建立功能分支

```bash
git checkout -b feature/your-feature
```

### 2. 本地開發循環

1. 撰寫測試
2. 實作功能
3. 執行測試
4. 提交變更

### 3. 提交規範

```
<type>(<scope>): <subject>

<body>
```

範例：
```
feat(reservation): 新增預約衝突檢查

- 實作時段重疊檢查邏輯
- 加入單元測試
- 更新 API 文件
```

Type 分類：
- feat: 新功能
- fix: 錯誤修正
- docs: 文件更新
- style: 程式碼格式
- refactor: 重構
- test: 測試相關
- chore: 建置/工具相關

## 常見問題

### Q: 如何重置測試資料？
A: 刪除 `data/` 目錄內容並重新複製初始資料：
```bash
rm -rf data/*
cp src/test/resources/initial/*.json data/
```

### Q: 前端 API 請求失敗？
A: 檢查：
1. 後端是否執行中
2. CORS 設定是否正確
3. API 路徑是否正確

### Q: 如何模擬郵件發送？
A: 使用測試設定檔：
```bash
mvn spring-boot:run -Dspring.profiles.active=test
```

## 部署準備

### 1. 建置生產版本

```bash
# 後端
mvn clean package -Pprod

# 前端
cd imrbs-web/src/ui
npm run build
```

### 2. 設定檢查清單

- [ ] 確認 Java 環境
- [ ] 設定資料目錄權限
- [ ] 配置真實郵件伺服器
- [ ] 更新 CORS 設定
- [ ] 設定備份機制

## 聯絡與支援

- 技術文件：`/docs`
- 問題回報：GitHub Issues
- 開發團隊：cti&eric