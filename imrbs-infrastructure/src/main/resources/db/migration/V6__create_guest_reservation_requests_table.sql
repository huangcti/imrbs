-- V6__create_guest_reservation_requests_table.sql
-- 創建訪客預約申請表

CREATE TABLE IF NOT EXISTS guest_reservation_requests (
    id BIGSERIAL PRIMARY KEY,
    guest_name VARCHAR(100) NOT NULL,
    guest_company VARCHAR(100) NOT NULL,
    guest_email VARCHAR(255) NOT NULL,
    guest_phone VARCHAR(20),
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    requested_start_time TIMESTAMP NOT NULL,
    requested_end_time TIMESTAMP NOT NULL CHECK (requested_end_time > requested_start_time),
    meeting_title VARCHAR(200) NOT NULL,
    meeting_purpose TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    reviewed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMP,
    rejection_reason TEXT,
    reservation_id BIGINT UNIQUE REFERENCES reservations(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_guest_request_status ON guest_reservation_requests(status);
CREATE INDEX idx_guest_request_room ON guest_reservation_requests(room_id);
CREATE INDEX idx_guest_request_email ON guest_reservation_requests(guest_email);
CREATE INDEX idx_guest_request_created_at ON guest_reservation_requests(created_at);
CREATE INDEX idx_guest_request_reviewed_by ON guest_reservation_requests(reviewed_by);

-- 註解
COMMENT ON TABLE guest_reservation_requests IS '訪客預約申請表 - 外部訪客提交的預約申請 (需審核)';
COMMENT ON COLUMN guest_reservation_requests.guest_name IS '訪客姓名';
COMMENT ON COLUMN guest_reservation_requests.guest_company IS '訪客公司';
COMMENT ON COLUMN guest_reservation_requests.guest_email IS '訪客 Email';
COMMENT ON COLUMN guest_reservation_requests.room_id IS '申請的會議室 ID (外鍵)';
COMMENT ON COLUMN guest_reservation_requests.meeting_purpose IS '會議目的 (用於審核判斷)';
COMMENT ON COLUMN guest_reservation_requests.status IS '狀態: PENDING-待審核, APPROVED-已批准, REJECTED-已拒絕';
COMMENT ON COLUMN guest_reservation_requests.reviewed_by IS '審核者 ID (外鍵, 限 ROOM_ADMIN/SYSTEM_ADMIN)';
COMMENT ON COLUMN guest_reservation_requests.reservation_id IS '批准後創建的預約 ID (外鍵, 唯一)';
