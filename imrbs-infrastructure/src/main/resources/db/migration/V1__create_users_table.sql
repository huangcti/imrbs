-- V1__create_users_table.sql
-- 創建使用者表

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('EMPLOYEE', 'ROOM_ADMIN', 'SYSTEM_ADMIN')),
    language_preference VARCHAR(5) NOT NULL DEFAULT 'zh-TW' CHECK (language_preference IN ('zh-TW', 'en')),
    phone_number VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_user_employee_id ON users(employee_id);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_is_active ON users(is_active);

-- 註解
COMMENT ON TABLE users IS '使用者表 - 包含員工與管理員';
COMMENT ON COLUMN users.employee_id IS '員工編號 (來自 SSO)';
COMMENT ON COLUMN users.email IS 'Email (加密儲存)';
COMMENT ON COLUMN users.role IS '角色: EMPLOYEE-一般員工, ROOM_ADMIN-會議室管理員, SYSTEM_ADMIN-系統管理員';
COMMENT ON COLUMN users.language_preference IS '語言偏好: zh-TW-繁體中文, en-英文';
COMMENT ON COLUMN users.is_active IS '帳號狀態 (軟刪除標記)';
