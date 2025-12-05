-- V8__add_performance_indexes.sql
-- 效能優化索引 - 針對常見查詢模式優化

-- ============================================
-- 1. 預約查詢優化索引
-- ============================================

-- 覆蓋索引: 會議室可用性查詢 (room_id + 時間範圍 + 狀態)
-- 用於快速檢查會議室在指定時間段是否有衝突預約
CREATE INDEX IF NOT EXISTS idx_reservation_room_availability 
    ON reservations(room_id, start_time, end_time, status);

-- 日期範圍查詢優化 (用於日曆視圖)
CREATE INDEX IF NOT EXISTS idx_reservation_date_range 
    ON reservations(DATE(start_time), room_id, status);

-- 用戶預約歷史查詢 (包含狀態過濾)
CREATE INDEX IF NOT EXISTS idx_reservation_user_history 
    ON reservations(user_id, start_time DESC, status);

-- 已確認預約的時間排序索引 (用於首頁和通知)
-- 注意: 移除 CURRENT_TIMESTAMP 條件，因為 PostgreSQL 要求索引謂詞必須是 IMMUTABLE
CREATE INDEX IF NOT EXISTS idx_reservation_confirmed_upcoming 
    ON reservations(start_time) 
    WHERE status = 'CONFIRMED';

-- 待處理預約索引 (用於管理員審核列表)
CREATE INDEX IF NOT EXISTS idx_reservation_pending_review 
    ON reservations(created_at DESC) 
    WHERE status = 'PENDING';

-- 即將開始的會議提醒查詢優化
CREATE INDEX IF NOT EXISTS idx_reservation_reminder 
    ON reservations(start_time, user_id) 
    WHERE status = 'CONFIRMED';

-- ============================================
-- 2. 會議室查詢優化索引
-- ============================================

-- 可用會議室搜尋 (狀態 + 容量)
CREATE INDEX IF NOT EXISTS idx_room_available_capacity 
    ON rooms(capacity, status) 
    WHERE status = 'AVAILABLE';

-- 會議室複合查詢 (樓層 + 建築物 + 狀態)
CREATE INDEX IF NOT EXISTS idx_room_location_status 
    ON rooms(building, floor, status);

-- ============================================
-- 3. 使用者查詢優化索引
-- ============================================

-- 活躍使用者複合索引 (用於登入和權限檢查)
CREATE INDEX IF NOT EXISTS idx_user_active_lookup 
    ON users(email, role) 
    WHERE is_active = TRUE;

-- 部門統計查詢優化
CREATE INDEX IF NOT EXISTS idx_user_department_stats 
    ON users(department, is_active);

-- 管理員快速查找
CREATE INDEX IF NOT EXISTS idx_user_admins 
    ON users(role, is_active) 
    WHERE role IN ('ROOM_ADMIN', 'SYSTEM_ADMIN') AND is_active = TRUE;

-- ============================================
-- 4. 通知查詢優化索引
-- ============================================

-- 待發送通知批次處理 (定時任務優化)
CREATE INDEX IF NOT EXISTS idx_notification_pending_batch 
    ON notifications(created_at, retry_count) 
    WHERE status = 'PENDING' AND retry_count < 3;

-- 通知歷史查詢 (按收件人和時間)
CREATE INDEX IF NOT EXISTS idx_notification_recipient_history 
    ON notifications(recipient_email, created_at DESC);

-- 預約相關通知查詢
CREATE INDEX IF NOT EXISTS idx_notification_reservation_lookup 
    ON notifications(reservation_id, notification_type, sent_at DESC);

-- ============================================
-- 5. 訪客申請查詢優化索引
-- ============================================

-- 待審核申請列表 (管理員視圖)
CREATE INDEX IF NOT EXISTS idx_guest_request_pending_list 
    ON guest_reservation_requests(created_at DESC) 
    WHERE status = 'PENDING';

-- 訪客申請歷史查詢 (按審核者)
CREATE INDEX IF NOT EXISTS idx_guest_request_reviewer_history 
    ON guest_reservation_requests(reviewed_by, reviewed_at DESC) 
    WHERE reviewed_by IS NOT NULL;

-- 會議室訪客申請統計
CREATE INDEX IF NOT EXISTS idx_guest_request_room_stats 
    ON guest_reservation_requests(room_id, status, created_at);

-- 訪客公司查詢 (用於重複訪客識別)
CREATE INDEX IF NOT EXISTS idx_guest_request_company_lookup 
    ON guest_reservation_requests(guest_company, guest_email);

-- ============================================
-- 6. 維護排程查詢優化索引
-- ============================================

-- 維護排程表只有基本欄位，已在 V4 中創建了基本索引
-- 如果未來需要擴展，可在此處添加額外索引

-- ============================================
-- 索引說明註解
-- ============================================

COMMENT ON INDEX idx_reservation_room_availability IS '會議室可用性查詢優化 - 衝突檢測';
COMMENT ON INDEX idx_reservation_date_range IS '日曆視圖日期範圍查詢優化';
COMMENT ON INDEX idx_reservation_user_history IS '用戶預約歷史查詢優化';
COMMENT ON INDEX idx_reservation_confirmed_upcoming IS '即將到來的已確認預約查詢';
COMMENT ON INDEX idx_reservation_pending_review IS '待審核預約列表優化';
COMMENT ON INDEX idx_reservation_reminder IS '會議提醒通知查詢優化';
COMMENT ON INDEX idx_room_available_capacity IS '可用會議室容量搜尋優化';
COMMENT ON INDEX idx_room_location_status IS '會議室位置與狀態查詢優化';
COMMENT ON INDEX idx_user_active_lookup IS '活躍用戶登入查詢優化';
COMMENT ON INDEX idx_user_department_stats IS '部門統計查詢優化';
COMMENT ON INDEX idx_user_admins IS '管理員快速查找優化';
COMMENT ON INDEX idx_notification_pending_batch IS '待發送通知批次處理優化';
COMMENT ON INDEX idx_notification_recipient_history IS '收件人通知歷史查詢優化';
COMMENT ON INDEX idx_notification_reservation_lookup IS '預約相關通知查詢優化';
COMMENT ON INDEX idx_guest_request_pending_list IS '待審核訪客申請列表優化';
COMMENT ON INDEX idx_guest_request_reviewer_history IS '審核者歷史查詢優化';
COMMENT ON INDEX idx_guest_request_room_stats IS '會議室訪客申請統計優化';
COMMENT ON INDEX idx_guest_request_company_lookup IS '訪客公司快速查找優化';
