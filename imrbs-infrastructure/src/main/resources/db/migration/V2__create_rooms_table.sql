-- V2__create_rooms_table.sql
-- 創建會議室表

CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    floor VARCHAR(10) NOT NULL,
    building VARCHAR(50),
    location_description VARCHAR(255),
    capacity INT NOT NULL CHECK (capacity > 0),
    equipment JSONB NOT NULL DEFAULT '[]'::jsonb,
    photos JSONB DEFAULT '[]'::jsonb,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'MAINTENANCE', 'DISABLED')),
    features JSONB DEFAULT '[]'::jsonb,
    booking_rule JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE UNIQUE INDEX idx_room_name ON rooms(name);
CREATE INDEX idx_room_capacity ON rooms(capacity);
CREATE INDEX idx_room_status ON rooms(status);
CREATE INDEX idx_room_building_floor ON rooms(building, floor);
CREATE INDEX idx_room_equipment_gin ON rooms USING gin(equipment jsonb_path_ops);
CREATE INDEX idx_room_features_gin ON rooms USING gin(features jsonb_path_ops);

-- 註解
COMMENT ON TABLE rooms IS '會議室表 - 可預約的會議室資源';
COMMENT ON COLUMN rooms.name IS '會議室名稱 (唯一)';
COMMENT ON COLUMN rooms.floor IS '樓層 (如 "3F", "B1")';
COMMENT ON COLUMN rooms.capacity IS '容納人數';
COMMENT ON COLUMN rooms.equipment IS '設備清單 (JSONB 格式, 如 [{"name": "投影機", "quantity": 1}])';
COMMENT ON COLUMN rooms.photos IS '照片 URL 清單 (JSONB 格式)';
COMMENT ON COLUMN rooms.status IS '狀態: AVAILABLE-可用, MAINTENANCE-維護中, DISABLED-已停用';
COMMENT ON COLUMN rooms.features IS '特色標籤 (JSONB 格式, 如 ["video_conferencing", "whiteboard"])';
COMMENT ON COLUMN rooms.booking_rule IS '預約規則 (JSONB 格式, 如 {"max_duration_hours": 4})';
