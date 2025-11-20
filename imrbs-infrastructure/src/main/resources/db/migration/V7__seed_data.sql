-- V7__seed_data.sql
-- 種子數據 - 測試使用者與會議室

-- 插入測試使用者
INSERT INTO users (employee_id, email, full_name, department, role, language_preference, phone_number, is_active)
VALUES
    ('EMP001', 'admin@example.com', '系統管理員', 'IT', 'SYSTEM_ADMIN', 'zh-TW', '0912-345-678', TRUE),
    ('EMP002', 'room.admin@example.com', '會議室管理員', '行政部', 'ROOM_ADMIN', 'zh-TW', '0912-345-679', TRUE),
    ('EMP003', 'john.doe@example.com', 'John Doe', '工程部', 'EMPLOYEE', 'en', '0912-345-680', TRUE),
    ('EMP004', 'jane.smith@example.com', 'Jane Smith', '業務部', 'EMPLOYEE', 'zh-TW', '0912-345-681', TRUE),
    ('EMP005', 'test.user@example.com', '測試使用者', '測試部', 'EMPLOYEE', 'zh-TW', NULL, TRUE)
ON CONFLICT (employee_id) DO NOTHING;

-- 插入測試會議室
INSERT INTO rooms (name, floor, building, location_description, capacity, equipment, photos, status, features, booking_rule)
VALUES
    (
        'A101 小型會議室',
        '1F',
        'A棟',
        '電梯旁',
        4,
        '[{"name": "白板", "quantity": 1}, {"name": "電視螢幕", "quantity": 1}]'::jsonb,
        '[]'::jsonb,
        'AVAILABLE',
        '["whiteboard", "tv_screen"]'::jsonb,
        '{"max_duration_hours": 2, "advance_booking_days": 14}'::jsonb
    ),
    (
        'A201 中型會議室',
        '2F',
        'A棟',
        '靠近茶水間',
        8,
        '[{"name": "投影機", "quantity": 1}, {"name": "白板", "quantity": 1}, {"name": "視訊會議設備", "quantity": 1, "brand": "Logitech Rally"}]'::jsonb,
        '[]'::jsonb,
        'AVAILABLE',
        '["video_conferencing", "whiteboard", "projector"]'::jsonb,
        '{"max_duration_hours": 4, "advance_booking_days": 30}'::jsonb
    ),
    (
        'A301 大型會議室',
        '3F',
        'A棟',
        '採光良好',
        16,
        '[{"name": "投影機", "quantity": 1}, {"name": "白板", "quantity": 2}, {"name": "視訊會議設備", "quantity": 1}, {"name": "無線麥克風", "quantity": 2}]'::jsonb,
        '[]'::jsonb,
        'AVAILABLE',
        '["video_conferencing", "whiteboard", "projector", "natural_light", "microphone"]'::jsonb,
        '{"max_duration_hours": 8, "advance_booking_days": 30}'::jsonb
    ),
    (
        'B101 討論室',
        '1F',
        'B棟',
        '安靜區域',
        6,
        '[{"name": "白板", "quantity": 1}, {"name": "電視螢幕", "quantity": 1}]'::jsonb,
        '[]'::jsonb,
        'AVAILABLE',
        '["whiteboard", "tv_screen", "quiet_zone"]'::jsonb,
        '{"max_duration_hours": 3, "advance_booking_days": 14}'::jsonb
    ),
    (
        'C201 VIP 會議室',
        '2F',
        'C棟',
        '高級會議室',
        10,
        '[{"name": "投影機", "quantity": 1}, {"name": "電動布幕", "quantity": 1}, {"name": "視訊會議設備", "quantity": 1}, {"name": "音響系統", "quantity": 1}]'::jsonb,
        '[]'::jsonb,
        'AVAILABLE',
        '["video_conferencing", "projector", "sound_system", "premium"]'::jsonb,
        '{"max_duration_hours": 8, "advance_booking_days": 60}'::jsonb
    )
ON CONFLICT (name) DO NOTHING;

-- 插入測試預約 (未來7天的預約)
INSERT INTO reservations (room_id, user_id, meeting_title, start_time, end_time, participants, description, status)
SELECT
    (SELECT id FROM rooms WHERE name = 'A201 中型會議室'),
    (SELECT id FROM users WHERE employee_id = 'EMP003'),
    '每週團隊會議',
    CURRENT_TIMESTAMP + INTERVAL '1 day' + TIME '10:00:00',
    CURRENT_TIMESTAMP + INTERVAL '1 day' + TIME '11:00:00',
    'jane.smith@example.com,test.user@example.com',
    '討論本週工作進度',
    'CONFIRMED'
WHERE NOT EXISTS (
    SELECT 1 FROM reservations WHERE meeting_title = '每週團隊會議'
);

INSERT INTO reservations (room_id, user_id, meeting_title, start_time, end_time, participants, status)
SELECT
    (SELECT id FROM rooms WHERE name = 'A301 大型會議室'),
    (SELECT id FROM users WHERE employee_id = 'EMP004'),
    '季度業績檢討',
    CURRENT_TIMESTAMP + INTERVAL '3 days' + TIME '14:00:00',
    CURRENT_TIMESTAMP + INTERVAL '3 days' + TIME '16:00:00',
    'john.doe@example.com,admin@example.com',
    'CONFIRMED'
WHERE NOT EXISTS (
    SELECT 1 FROM reservations WHERE meeting_title = '季度業績檢討'
);

-- 插入測試維護時程
INSERT INTO maintenance_schedules (room_id, start_time, end_time, reason, created_by, notes)
SELECT
    (SELECT id FROM rooms WHERE name = 'B101 討論室'),
    CURRENT_TIMESTAMP + INTERVAL '5 days' + TIME '18:00:00',
    CURRENT_TIMESTAMP + INTERVAL '5 days' + TIME '22:00:00',
    '設備升級維護',
    (SELECT id FROM users WHERE employee_id = 'EMP002'),
    '更換投影機與白板'
WHERE NOT EXISTS (
    SELECT 1 FROM maintenance_schedules WHERE reason = '設備升級維護'
);

-- 插入統計數據
COMMENT ON DATABASE imrbs IS 'IMRBS 會議室預約系統 - 開發環境';
