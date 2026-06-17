-- ===========================================================
-- 家庭 + 今日主厨 模型
-- 推倒重来的全新 schema，删除 role/boundCookUserId
-- ===========================================================

-- ---- 家庭 ----
CREATE TABLE IF NOT EXISTS families (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_code VARCHAR(8) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    creator_user_id VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- 用户 ----
CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    nickname VARCHAR(32),
    avatar VARCHAR(1000),
    family_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_family (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- 家庭成员（冗余表，便于查"某家庭所有成员"和"某用户所属家庭"） ----
CREATE TABLE IF NOT EXISTS family_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_family_user (family_id, user_id),
    INDEX idx_member_user (user_id),
    INDEX idx_member_family (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- 家庭邀请 ----
CREATE TABLE IF NOT EXISTS family_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    inviter_user_id VARCHAR(64) NOT NULL,
    invitee_user_id VARCHAR(64) NOT NULL,
    invitee_phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_type VARCHAR(24) NOT NULL DEFAULT 'INVITE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_invitation_family (family_id),
    INDEX idx_invitation_invitee (invitee_user_id),
    INDEX idx_invitation_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- 每日主厨 ----
CREATE TABLE IF NOT EXISTS daily_cooks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    cook_user_id VARCHAR(64) NOT NULL,
    cook_date DATE NOT NULL,
    set_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_family_date (family_id, cook_date),
    INDEX idx_cook_user (cook_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- 菜品 ----
CREATE TABLE IF NOT EXISTS dishes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(19,2) NOT NULL,
    image VARCHAR(255),
    category VARCHAR(255),
    cook_user_id VARCHAR(64),
    cook_date DATE,
    meal_time VARCHAR(255),
    created_by_user_id VARCHAR(64),
    available BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dish_family (family_id),
    INDEX idx_dish_cook_date (family_id, cook_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- 菜品分类 ----
CREATE TABLE IF NOT EXISTS dish_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    sort_order INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- 修改申请 ----
CREATE TABLE IF NOT EXISTS dish_edit_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_id BIGINT,
    user_id VARCHAR(64),
    original_dish_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(19,2) NOT NULL,
    image VARCHAR(255),
    category VARCHAR(255),
    meal_time VARCHAR(255),
    status INT DEFAULT 0,
    reject_reason VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_request_family (family_id),
    INDEX idx_request_status (status),
    FOREIGN KEY (original_dish_id) REFERENCES dishes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- 订单 ----
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    family_id BIGINT,
    user_id VARCHAR(64),
    total_price DECIMAL(19,2),
    status INT DEFAULT 0,
    address VARCHAR(500),
    phone VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_family (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    dish_id BIGINT,
    quantity INT DEFAULT 1,
    price DECIMAL(19,2),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (dish_id) REFERENCES dishes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- 文件资源 ----
CREATE TABLE IF NOT EXISTS file_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255),
    stored_name VARCHAR(255),
    content_type VARCHAR(255),
    size BIGINT,
    relative_path VARCHAR(500),
    url VARCHAR(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
