# IMRBS API 契約

## REST API Endpoints

### 會議室管理

#### GET /api/rooms
列出所有會議室

```json
{
  "rooms": [
    {
      "id": "string",
      "name": "string",
      "location": "板橋|民生",
      "floor": "string",
      "capacity": "number",
      "features": ["string"],
      "status": "active|maintenance"
    }
  ]
}
```

#### GET /api/rooms/{id}
取得特定會議室

```json
{
  "id": "string",
  "name": "string",
  "location": "板橋|民生",
  "floor": "string",
  "capacity": "number",
  "features": ["string"],
  "status": "active|maintenance"
}
```

#### POST /api/rooms
新增會議室

Request:
```json
{
  "name": "string",
  "location": "板橋|民生",
  "floor": "string",
  "capacity": "number",
  "features": ["string"]
}
```

Response: 201 Created
```json
{
  "id": "string",
  "name": "string",
  "location": "板橋|民生",
  "floor": "string",
  "capacity": "number",
  "features": ["string"],
  "status": "active"
}
```

#### PUT /api/rooms/{id}
更新會議室

Request:
```json
{
  "name": "string",
  "location": "板橋|民生",
  "floor": "string",
  "capacity": "number",
  "features": ["string"],
  "status": "active|maintenance"
}
```

Response: 200 OK
```json
{
  "id": "string",
  "name": "string",
  "location": "板橋|民生",
  "floor": "string",
  "capacity": "number",
  "features": ["string"],
  "status": "active|maintenance"
}
```

### 預約管理

#### GET /api/reservations
查詢預約列表

Parameters:
- date: YYYY-MM-DD
- room_id: string (optional)
- status: confirmed|cancelled (optional)

```json
{
  "reservations": [
    {
      "id": "string",
      "room_id": "string",
      "room_name": "string",
      "title": "string",
      "organizer_email": "string",
      "participants": ["string"],
      "date": "YYYY-MM-DD",
      "start_time": "HH:mm",
      "end_time": "HH:mm",
      "status": "confirmed|cancelled"
    }
  ]
}
```

#### POST /api/reservations
建立預約

Request:
```json
{
  "room_id": "string",
  "title": "string",
  "organizer_email": "string",
  "participants": ["string"],
  "date": "YYYY-MM-DD",
  "start_time": "HH:mm",
  "end_time": "HH:mm"
}
```

Response: 201 Created
```json
{
  "id": "string",
  "room_id": "string",
  "room_name": "string",
  "title": "string",
  "organizer_email": "string",
  "participants": ["string"],
  "date": "YYYY-MM-DD",
  "start_time": "HH:mm",
  "end_time": "HH:mm",
  "status": "confirmed"
}
```

#### PUT /api/reservations/{id}
更新預約

Request:
```json
{
  "title": "string",
  "participants": ["string"],
  "date": "YYYY-MM-DD",
  "start_time": "HH:mm",
  "end_time": "HH:mm"
}
```

Response: 200 OK
```json
{
  "id": "string",
  "room_id": "string",
  "room_name": "string",
  "title": "string",
  "organizer_email": "string",
  "participants": ["string"],
  "date": "YYYY-MM-DD",
  "start_time": "HH:mm",
  "end_time": "HH:mm",
  "status": "confirmed"
}
```

#### DELETE /api/reservations/{id}
取消預約

Response: 200 OK
```json
{
  "id": "string",
  "status": "cancelled"
}
```

### 查詢與狀態

#### GET /api/rooms/availability
查詢會議室可用性

Parameters:
- date: YYYY-MM-DD
- location: 板橋|民生 (optional)

```json
{
  "date": "YYYY-MM-DD",
  "rooms": [
    {
      "id": "string",
      "name": "string",
      "location": "板橋|民生",
      "floor": "string",
      "time_slots": [
        {
          "start_time": "HH:mm",
          "end_time": "HH:mm",
          "status": "available|reserved|maintenance"
        }
      ]
    }
  ]
}
```

## 錯誤回應格式

### 400 Bad Request
```json
{
  "error": "validation_error",
  "message": "輸入資料驗證失敗",
  "details": {
    "field": "錯誤訊息"
  }
}
```

### 409 Conflict
```json
{
  "error": "reservation_conflict",
  "message": "預約時段衝突",
  "details": {
    "conflicting_reservation": {
      "id": "string",
      "title": "string",
      "date": "YYYY-MM-DD",
      "start_time": "HH:mm",
      "end_time": "HH:mm"
    }
  }
}
```

### 422 Unprocessable Entity
```json
{
  "error": "business_rule_violation",
  "message": "違反業務規則",
  "details": {
    "rule": "規則說明"
  }
}
```

### 500 Internal Server Error
```json
{
  "error": "internal_error",
  "message": "系統發生錯誤",
  "trace_id": "string"
}
```

## WebSocket 事件（即時更新）

### 訂閱主題

#### /topic/rooms/{room_id}
會議室狀態更新事件
```json
{
  "type": "room_update",
  "room_id": "string",
  "status": "active|maintenance",
  "timestamp": "ISO-8601"
}
```

#### /topic/reservations
預約狀態更新事件
```json
{
  "type": "reservation_update",
  "reservation_id": "string",
  "status": "confirmed|cancelled",
  "timestamp": "ISO-8601"
}
```

## API 版本控制
- API 版本通過 URL 路徑指定：/api/v1/...
- 目前版本：v1
- 版本變更會提前通知並提供遷移指引