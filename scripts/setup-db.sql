-- 建立 IMRBS 資料庫和用戶

-- 刪除既有資料庫和用戶 (如果存在)
DROP DATABASE IF EXISTS imrbs;
DROP USER IF EXISTS imrbs_user;

-- 建立用戶
CREATE USER imrbs_user WITH PASSWORD 'imrbs_pass';

-- 建立資料庫
CREATE DATABASE imrbs 
    OWNER imrbs_user 
    ENCODING 'UTF8'
    TEMPLATE template0;

-- 授予權限
GRANT ALL PRIVILEGES ON DATABASE imrbs TO imrbs_user;

-- 顯示結果
\l imrbs
