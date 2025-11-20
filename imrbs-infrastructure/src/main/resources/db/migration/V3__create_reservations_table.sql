-- V3__create_reservations_table.sql
-- 創建預約表

-- 啟用 btree_gist 擴展 (用於時間範圍排他約束)
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    meeting_title VARCHAR(200) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL CHECK (end_time > start_time),
    participants TEXT,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    cancellation_reason TEXT,
    external_meeting_link VARCHAR(500),
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurring_rule JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP
);

-- 唯一性約束 (時間重疊排他約束)
-- 同一會議室在同一時段只能有一個有效預約 (排除已取消的預約)
ALTER TABLE reservations
ADD CONSTRAINT reservations_no_overlap
EXCLUDE USING gist (
    room_id WITH =,
    tsrange(start_time, end_time) WITH &&
)
WHERE (status <> 'CANCELLED');

-- 索引
CREATE INDEX idx_reservation_room_time ON reservations(room_id, start_time, end_time);
CREATE INDEX idx_reservation_user ON reservations(user_id);
CREATE INDEX idx_reservation_status ON reservations(status);
CREATE INDEX idx_reservation_start_time ON reservations(start_time);
CREATE INDEX idx_reservation_user_status ON reservations(user_id, status);

-- 註解
COMMENT ON TABLE reservations IS '預約表 - 會議室預約記錄';
COMMENT ON COLUMN reservations.room_id IS '會議室 ID (外鍵)';
COMMENT ON COLUMN reservations.user_id IS '預約者 ID (外鍵)';
COMMENT ON COLUMN reservations.meeting_title IS '會議主題';
COMMENT ON COLUMN reservations.participants IS '參與者 Email 清單 (逗號分隔)';
COMMENT ON COLUMN reservations.status IS '狀態: PENDING-待審核, CONFIRMED-已確認, CANCELLED-已取消';
COMMENT ON COLUMN reservations.is_recurring IS '是否為週期性預約';
COMMENT ON COLUMN reservations.recurring_rule IS '週期規則 (JSONB 格式, 如 {"frequency": "weekly", "days": ["MON"]})';
COMMENT ON CONSTRAINT reservations_no_overlap ON reservations IS '防止同一會議室時間衝突 (排除已取消的預約)';
