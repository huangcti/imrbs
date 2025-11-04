# 會議室預約系統 - 前端

Vue 3 + Vite 前端應用程式

## 安裝

```powershell
cd imrbs-web\frontend
npm install
```

## 開發模式

```powershell
npm run dev
```

前端會啟動在 http://localhost:3000，並自動代理 API 請求到後端 (http://localhost:8080)

**注意**: 需要先啟動後端 Spring Boot 應用程式！

## 生產建置

```powershell
npm run build
```

建置檔案會輸出到 `imrbs-web/src/main/resources/static/`，可直接由 Spring Boot 提供服務。

## 功能

- **會議室查詢**: 依地點、日期查詢會議室狀態
- **建立預約**: 選擇會議室、時間，建立新預約
- **我的預約**: 查詢、修改、取消預約
- **管理會議室**: 新增、編輯、刪除會議室 (管理員)

## 技術棧

- Vue 3 (Composition API)
- Vite 5
- Axios (HTTP client)
- 純 CSS (無額外 UI 框架)
