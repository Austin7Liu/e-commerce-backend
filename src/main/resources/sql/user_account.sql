CREATE TABLE user_account (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    username VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '加盐后的密码哈希，禁止保存明文密码',
    nickname VARCHAR(100) NOT NULL COMMENT '用户昵称',
    phone VARCHAR(20) NULL COMMENT '手机号码',
    email VARCHAR(254) NULL COMMENT '电子邮箱',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    created_time DATETIME(3) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_time DATETIME(3) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_account_username (username),
    UNIQUE KEY uk_user_account_phone (phone),
    UNIQUE KEY uk_user_account_email (email),

    CONSTRAINT chk_user_account_username
        CHECK (CHAR_LENGTH(TRIM(username)) > 0),
    CONSTRAINT chk_user_account_nickname
        CHECK (CHAR_LENGTH(TRIM(nickname)) > 0),
    CONSTRAINT chk_user_account_status
        CHECK (status IN (0, 1)),
    CONSTRAINT chk_user_account_deleted
        CHECK (deleted IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户账号表';
