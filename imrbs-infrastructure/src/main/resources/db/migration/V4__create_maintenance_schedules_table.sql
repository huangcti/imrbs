-- V4__create_maintenance_schedules_table.sql
-- 創建維護時程表

CREATE TABLE IF NOT EXISTS maintenance_schedules (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL CHECK (end_time > start_time),
    reason VARCHAR(255) NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_maintenance_room_time ON maintenance_schedules(room_id, start_time, end_time);
CREATE INDEX idx_maintenance_created_by ON maintenance_schedules(created_by);
CREATE INDEX idx_maintenance_start_time ON maintenance_schedules(start_time);

-- 註解
COMMENT ON TABLE maintenance_schedules IS '維護時程表 - 會議室維護時段記錄';
COMMENT ON COLUMN maintenance_schedules.room_id IS '會議室 ID (外鍵)';
COMMENT ON COLUMN maintenance_schedules.start_time IS '維護開始時間';
COMMENT ON COLUMN maintenance_schedules.end_time IS '維護結束時間';
COMMENT ON COLUMN maintenance_schedules.reason IS '維護原因';
COMMENT ON COLUMN maintenance_schedules.created_by IS '創建者 ID (外鍵, 限 ROOM_ADMIN/SYSTEM_ADMIN)';
