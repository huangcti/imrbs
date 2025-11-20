# API Endpoints: 會議室預約系統

**Feature**: 001-meeting-room-booking  
**Date**: 2025-11-20  
**Phase**: 1 (API 合約設計)  
**Base URL**: `/api/v1`

## 概述

本文件定義會議室預約系統的 RESTful API 端點,遵循 OpenAPI 3.0 規範。完整的 API 規格請參考 `openapi.yaml`。

---

## 認證與授權

### 認證方式

- **OAuth 2.0 + OIDC**: SSO 單一登入
- **JWT Token**: API 請求攜帶 `Authorization: Bearer {token}`

### 權限角色

| 角色 | 權限 |
|------|------|
| `EMPLOYEE` | 查詢會議室,CRUD 自己的預約 |
| `ROOM_ADMIN` | EMPLOYEE + 管理會議室, 審核訪客預約 |
| `SYSTEM_ADMIN` | ROOM_ADMIN + 使用者管理, 系統配置 |

---

## API 端點清單

### 1. 認證 (Authentication)

#### `POST /auth/login`
- **描述**: OAuth 2.0 登入 (交換 Authorization Code)
- **權限**: Public
- **請求**: `{ "code": "auth_code", "redirect_uri": "..." }`
- **回應**: `{ "access_token": "jwt_token", "refresh_token": "...", "expires_in": 900 }`

#### `POST /auth/refresh`
- **描述**: 刷新 Access Token
- **權限**: Public
- **請求**: `{ "refresh_token": "..." }`
- **回應**: `{ "access_token": "new_jwt", "expires_in": 900 }`

---

### 2. 會議室 (Rooms)

#### `GET /rooms`
- **描述**: 查詢會議室清單 (支援過濾)
- **權限**: EMPLOYEE+
- **查詢參數**: `?capacity_min=10&floor=3F&status=AVAILABLE`
- **回應**: `{ "data": [Room], "total": 50 }`

#### `GET /rooms/{id}`
- **描述**: 查詢單一會議室詳細資訊
- **權限**: EMPLOYEE+
- **回應**: Room 物件

#### `POST /rooms`
- **描述**: 新增會議室
- **權限**: ROOM_ADMIN+
- **請求**: Room 物件 (不含 id)
- **回應**: 201 Created, Room 物件

#### `PUT /rooms/{id}`
- **描述**: 更新會議室資訊
- **權限**: ROOM_ADMIN+
- **請求**: Room 物件
- **回應**: 200 OK, Room 物件

#### `DELETE /rooms/{id}`
- **描述**: 刪除會議室 (軟刪除)
- **權限**: ROOM_ADMIN+
- **回應**: 204 No Content

#### `GET /rooms/{id}/availability`
- **描述**: 查詢會議室可用性 (指定日期)
- **權限**: EMPLOYEE+
- **查詢參數**: `?date=2025-12-01`
- **回應**: `{ "date": "2025-12-01", "available_slots": [TimeSlot] }`

---

### 3. 預約 (Reservations)

#### `GET /reservations`
- **描述**: 查詢預約清單 (當前使用者)
- **權限**: EMPLOYEE+
- **查詢參數**: `?status=CONFIRMED&start_date=2025-12-01&end_date=2025-12-31`
- **回應**: `{ "data": [Reservation], "total": 20 }`

#### `GET /reservations/{id}`
- **描述**: 查詢單一預約詳細資訊
- **權限**: EMPLOYEE+ (僅查詢自己的預約)
- **回應**: Reservation 物件

#### `POST /reservations`
- **描述**: 創建預約
- **權限**: EMPLOYEE+
- **請求**: `{ "room_id": 1, "meeting_title": "...", "start_time": "2025-12-01T14:00:00Z", "end_time": "...", "participants": "..." }`
- **回應**: 201 Created, Reservation 物件
- **錯誤**: 409 Conflict (時間衝突)

#### `PUT /reservations/{id}`
- **描述**: 修改預約
- **權限**: EMPLOYEE+ (僅修改自己的預約)
- **請求**: Reservation 物件
- **回應**: 200 OK, Reservation 物件

#### `DELETE /reservations/{id}`
- **描述**: 取消預約
- **權限**: EMPLOYEE+ (僅取消自己的預約)
- **請求**: `{ "cancellation_reason": "會議延期" }`
- **回應**: 204 No Content

---

### 4. 管理員功能 (Admin)

#### `GET /admin/reservations`
- **描述**: 查詢所有預約 (管理員視圖)
- **權限**: ROOM_ADMIN+
- **查詢參數**: `?room_id=1&user_id=2&status=CONFIRMED`
- **回應**: `{ "data": [Reservation], "total": 100 }`

#### `POST /admin/rooms/{id}/maintenance`
- **描述**: 新增維護時程
- **權限**: ROOM_ADMIN+
- **請求**: `{ "start_time": "...", "end_time": "...", "reason": "設備檢修" }`
- **回應**: 201 Created, MaintenanceSchedule 物件

#### `GET /admin/reports/usage`
- **描述**: 會議室使用率報告
- **權限**: ROOM_ADMIN+
- **查詢參數**: `?start_date=2025-11-01&end_date=2025-11-30`
- **回應**: `{ "rooms": [{ "room_id": 1, "usage_hours": 120, "usage_rate": 0.75 }] }`

---

### 5. 訪客預約 (Guest Requests)

#### `POST /guest/requests`
- **描述**: 訪客提交預約申請
- **權限**: Public
- **請求**: `{ "guest_name": "...", "guest_company": "...", "guest_email": "...", "room_id": 1, ... }`
- **回應**: 201 Created, GuestReservationRequest 物件

#### `GET /admin/guest-requests`
- **描述**: 查詢待審核的訪客申請
- **權限**: ROOM_ADMIN+
- **查詢參數**: `?status=PENDING`
- **回應**: `{ "data": [GuestReservationRequest], "total": 5 }`

#### `POST /admin/guest-requests/{id}/approve`
- **描述**: 批准訪客申請
- **權限**: ROOM_ADMIN+
- **回應**: 200 OK, `{ "reservation_id": 123 }`

#### `POST /admin/guest-requests/{id}/reject`
- **描述**: 拒絕訪客申請
- **權限**: ROOM_ADMIN+
- **請求**: `{ "rejection_reason": "會議室已預訂" }`
- **回應**: 200 OK

---

## 錯誤處理

### 統一錯誤格式

```json
{
  "error": {
    "code": "RESERVATION_CONFLICT",
    "message": "該時段會議室已被預訂",
    "details": {
      "room_id": 1,
      "conflicting_reservation_id": 456
    },
    "timestamp": "2025-11-20T10:30:00Z"
  }
}
```

### HTTP 狀態碼

| 狀態碼 | 說明 |
|--------|------|
| 200 OK | 請求成功 |
| 201 Created | 資源創建成功 |
| 204 No Content | 刪除成功 (無回應內容) |
| 400 Bad Request | 請求參數錯誤 |
| 401 Unauthorized | 未認證 (JWT 無效或過期) |
| 403 Forbidden | 無權限存取 |
| 404 Not Found | 資源不存在 |
| 409 Conflict | 資源衝突 (如預約時間重疊) |
| 500 Internal Server Error | 伺服器錯誤 |

---

## 分頁與排序

### 查詢參數

- `page`: 頁碼 (從 1 開始)
- `size`: 每頁筆數 (預設 20, 最大 100)
- `sort`: 排序欄位 (如 `start_time,desc`)

### 回應格式

```json
{
  "data": [...],
  "pagination": {
    "current_page": 1,
    "total_pages": 5,
    "total_items": 100,
    "page_size": 20
  }
}
```

---

**API 合約完成日期**: 2025-11-20  
**完整 OpenAPI 規格**: 參考 `openapi.yaml`
