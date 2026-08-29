CREATE TABLE product (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品主键',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    main_image_url VARCHAR(512) NULL COMMENT '商品主图 URL',
    price DECIMAL(10, 2) NOT NULL COMMENT '销售价格',
    stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '库存数量',
    description TEXT NULL COMMENT '商品详情描述',
    status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品状态：0-下架，1-上架',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    created_time DATETIME(3) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_time DATETIME(3) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (id),

    CONSTRAINT chk_product_price
        CHECK (price >= 0),
    CONSTRAINT chk_product_status
        CHECK (status IN (0, 1)),
    CONSTRAINT chk_product_deleted
        CHECK (deleted IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '商品表';
