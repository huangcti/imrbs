# Quickstart Guide: 會議室預約系統

**Feature**: 001-meeting-room-booking  
**Date**: 2025-01-20  
**Version**: 1.0.0

## 概述

本指南幫助開發者快速設定開發環境並開始參與會議室預約系統的開發。

---

## 前置需求

### 必要工具

| 工具 | 版本 | 說明 |
|------|------|------|
| **Java** | OpenJDK 21+ | 後端開發 |
| **Maven** | 3.9+ | 建置工具 |
| **Node.js** | 20 LTS+ | 前端開發 |
| **npm** | 10+ | 套件管理 |
| **Docker** | 最新穩定版 | 容器化環境 |
| **Docker Compose** | V2+ | 多容器編排 |
| **Git** | 2.40+ | 版本控制 |

### IDE 設定

#### 後端 (IntelliJ IDEA / VS Code)

```
推薦插件:
- Spring Boot Extension Pack
- Lombok
- CheckStyle-IDEA
- SonarLint
```

#### 前端 (VS Code)

```
必要插件:
- Volar (Vue 3 官方)
- TypeScript Vue Plugin
- ESLint
- Prettier
- Tailwind CSS IntelliSense
```

---

## 環境設定

### 1. Clone 專案

```powershell
git clone https://github.com/your-org/imrbs.git
cd imrbs
git checkout 001-meeting-room-booking
```

### 2. 啟動本地服務 (Docker Compose)

```powershell
cd docker
docker compose up -d
```

啟動的服務:

| 服務 | 連接埠 | 認證資訊 |
|------|--------|----------|
| **PostgreSQL** | `localhost:5432` | DB: `imrbs`, User: `admin`, Pass: `secret` |
| **Redis** | `localhost:6379` | 無密碼 (開發環境) |
| **RabbitMQ** | `localhost:5672` | User: `imrbs_user`, Pass: `imrbs_pass` |
| **RabbitMQ UI** | `http://localhost:15672` | 同上 |

### 3. 後端設定

#### 執行資料庫遷移

```powershell
# Flyway 會在應用程式啟動時自動執行遷移
# 或手動執行:
cd imrbs-web
mvn flyway:migrate
```

#### 啟動後端

```powershell
# 從專案根目錄
mvn spring-boot:run -pl imrbs-web

# 或指定 profile
mvn spring-boot:run -pl imrbs-web -Dspring-boot.run.profiles=dev
```

驗證啟動成功:
- API 端點: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Check: `http://localhost:8080/actuator/health`

### 4. 前端設定

```powershell
cd imrbs-frontend
npm install
npm run dev
```

前端可訪問: `http://localhost:5173`

#### 前端環境變數 (.env.local)

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_KEYCLOAK_URL=http://localhost:8180
VITE_KEYCLOAK_REALM=imrbs
VITE_KEYCLOAK_CLIENT_ID=imrbs-frontend
```

---

## 開發流程 (TDD)

### 1. Red (編寫失敗的測試)

```java
@Test
void testCreateReservation_Success() {
    // Given
    CreateReservationCommand command = new CreateReservationCommand(
        1L, "部門月會", 
        LocalDateTime.of(2025, 12, 1, 14, 0),
        LocalDateTime.of(2025, 12, 1, 16, 0), 
        List.of("alice@example.com", "bob@example.com")
    );
    
    // When
    ReservationDto result = createReservationUseCase.execute(command);
    
    // Then
    assertThat(result.getMeetingTitle()).isEqualTo("部門月會");
    assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
}
```

### 2. Green (實作最小程式碼讓測試通過)

```java
@Service
public class CreateReservationUseCaseImpl implements CreateReservationUseCase {
    
    @Override
    @Transactional
    public ReservationDto execute(CreateReservationCommand command) {
        // 衝突檢測
        conflictDetectionService.checkConflicts(command);
        
        // 建立預約
        Reservation reservation = Reservation.create(command);
        reservationRepository.save(reservation);
        
        // 發送通知
        notificationService.sendConfirmation(reservation);
        
        return ReservationMapper.toDto(reservation);
    }
}
```

### 3. Refactor (重構)

- 提取重複邏輯到領域服務
- 優化命名，提高可讀性
- 確保符合 Clean Architecture 原則

---

## 專案結構導覽

```
imrbs/
├── imrbs-core/                    # 核心業務邏輯 (框架無關)
│   └── src/main/java/tw/huangcti/imrbs/
│       ├── domain/
│       │   ├── model/             # 領域實體 (Reservation, Room, User)
│       │   ├── repository/        # Repository 介面
│       │   └── service/           # 領域服務
│       └── application/
│           ├── usecase/           # 用例實作
│           └── dto/               # 應用層 DTO
│
├── imrbs-infrastructure/          # 基礎設施實作
│   └── src/main/java/tw/huangcti/imrbs/infrastructure/
│       ├── persistence/
│       │   ├── jpa/               # JPA 實體與 Repository
│       │   └── redis/             # Redis 快取服務
│       ├── messaging/             # RabbitMQ 整合
│       └── integration/           # 外部服務整合
│
├── imrbs-web/                     # Web API 層
│   └── src/main/java/tw/huangcti/imrbs/web/
│       ├── controller/            # REST Controllers
│       ├── security/              # 安全配置
│       ├── actuator/              # 自訂健康檢查
│       └── dto/                   # API DTOs
│
└── imrbs-frontend/                # Vue 3 前端
    └── src/
        ├── components/            # Vue 元件
        ├── views/                 # 頁面視圖
        ├── stores/                # Pinia 狀態
        ├── router/                # 路由 (Lazy Loading)
        ├── services/              # API 服務
        └── locales/               # i18n (zh-TW, en)
```

---

## 常用指令

### 後端

```powershell
# 執行單元測試
mvn test

# 執行測試並生成覆蓋率報告
mvn test jacoco:report
# 報告位置: target/site/jacoco/index.html

# 檢查 95% 覆蓋率要求
mvn jacoco:check

# 執行 Checkstyle 檢查
mvn checkstyle:check

# 執行架構測試 (ArchUnit)
mvn test -Dtest="*ArchitectureTest,*LayerTest"

# 建置專案 (跳過測試)
mvn package -DskipTests

# 建置 Docker 映像
docker build -t imrbs-backend:latest .
```

### 前端

```powershell
# 安裝依賴
npm install

# 啟動開發伺服器
npm run dev

# 執行單元測試
npm run test:unit

# 執行 E2E 測試 (需先啟動後端)
npm run test:e2e

# 建置生產版本
npm run build

# Lint 檢查
npm run lint

# 類型檢查
npm run type-check
```

### Docker

```powershell
# 啟動所有服務
docker compose up -d

# 查看服務狀態
docker compose ps

# 查看日誌
docker compose logs -f

# 停止所有服務
docker compose down

# 清除所有資料
docker compose down -v
```

---

## 常見問題 (FAQ)

### Q1: Maven 編譯失敗，找不到類別

**問題**: `Cannot find symbol` 錯誤

**解決方案**:
```powershell
# 清理並重新編譯
mvn clean install -DskipTests
```

### Q2: Docker 容器無法啟動

**問題**: 連接埠已被佔用

**解決方案**:
```powershell
# 檢查連接埠使用情況
netstat -ano | findstr :5432

# 停止佔用程序或修改 docker-compose.yml 中的連接埠映射
```

### Q3: 前端無法連接後端 API

**問題**: CORS 錯誤

**解決方案**:
1. 確認後端已啟動: `http://localhost:8080/actuator/health`
2. 確認 `.env.local` 中的 `VITE_API_BASE_URL` 正確
3. 檢查後端 `SecurityConfig.java` 中的 CORS 配置

### Q4: Flyway 遷移失敗

**問題**: 資料庫 schema 不一致

**解決方案**:
```powershell
# 清理資料庫並重新遷移 (開發環境)
mvn flyway:clean flyway:migrate -pl imrbs-web

# 或刪除 Docker volume 重新建立
docker compose down -v
docker compose up -d
```

### Q5: 測試覆蓋率不足

**問題**: JaCoCo 檢查失敗

**解決方案**:
```powershell
# 生成覆蓋率報告查看未覆蓋的程式碼
mvn test jacoco:report

# 報告位置: target/site/jacoco/index.html
# 補充缺少的測試案例
```

### Q6: Redis 連線失敗

**問題**: `Connection refused`

**解決方案**:
```powershell
# 確認 Redis 容器正在運行
docker compose ps redis

# 測試連線
docker exec -it imrbs-redis redis-cli ping
# 預期回應: PONG
```

### Q7: 如何切換語系?

**問題**: 測試多語系功能

**解決方案**:
```typescript
// 前端程式碼中
import { useI18n } from 'vue-i18n';
const { locale } = useI18n();

// 切換到英文
locale.value = 'en';

// 切換到繁體中文
locale.value = 'zh-TW';
```

或在瀏覽器開發者工具中:
```javascript
localStorage.setItem('locale', 'en');
location.reload();
```

---

## 下一步

1. ✅ 閱讀 `spec.md` 了解功能需求
2. ✅ 閱讀 `data-model.md` 了解資料結構
3. ✅ 閱讀 `contracts/api-endpoints.md` 了解 API 設計
4. ✅ 執行 `tasks.md` 中的任務
5. 🚀 開始 TDD 開發!

---

## 聯繫方式

- **技術問題**: 提交 GitHub Issue
- **Code Review**: 透過 Pull Request

---

**Quickstart 更新日期**: 2025-01-20  
**文件版本**: 1.0.0
