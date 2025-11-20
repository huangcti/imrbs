-- V5__create_notifications_table.sql
-- 創建通知表

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT REFERENCES reservations(id) ON DELETE SET NULL,
    recipient_email VARCHAR(255) NOT NULL,
    notification_type VARCHAR(50) NOT NULL CHECK (notification_type IN (
        'RESERVATION_CREATED',
        'RESERVATION_UPDATED',
        'RESERVATION_CANCELLED',
        'MEETING_REMINDER',
        'GUEST_APPROVAL_REQUEST',
        'GUEST_APPROVAL_RESULT'
    )),
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    sent_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    error_message TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_notification_reservation ON notifications(reservation_id);
CREATE INDEX idx_notification_status ON notifications(status);
CREATE INDEX idx_notification_created_at ON notifications(created_at);
CREATE INDEX idx_notification_status_retry ON notifications(status, retry_count) WHERE status = 'PENDING';

-- 註解
COMMENT ON TABLE notifications IS '通知表 - 系統發送的通知記錄 (Email/SMS)';
COMMENT ON COLUMN notifications.reservation_id IS '相關預約 ID (外鍵, 可為 NULL)';
COMMENT ON COLUMN notifications.notification_type IS '通知類型: RESERVATION_CREATED/UPDATED/CANCELLED, MEETING_REMINDER, GUEST_APPROVAL_REQUEST/RESULT';
COMMENT ON COLUMN notifications.status IS '狀態: PENDING-待發送, SENT-已發送, FAILED-發送失敗';
COMMENT ON COLUMN notifications.retry_count IS '重試次數 (最多 3 次)';
