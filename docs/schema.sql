-- ================================
-- 의류 쇼핑몰 DDL
-- DB: MySQL 8.x
-- ================================

CREATE DATABASE IF NOT EXISTS shopping_mall
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE shopping_mall;

-- ================================
-- 1. 회원 (member)
-- ================================
CREATE TABLE member (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    email       VARCHAR(100)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    name        VARCHAR(50)     NOT NULL,
    phone       VARCHAR(20),
    address     VARCHAR(255),
    role        ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_member_email (email)
);

-- ================================
-- 2. 카테고리 (category)
-- ================================
CREATE TABLE category (
    id      BIGINT          NOT NULL AUTO_INCREMENT,
    name    VARCHAR(50)     NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_category_name (name)
);

-- 기본 카테고리 데이터
INSERT INTO category (name) VALUES
    ('상의'),
    ('하의'),
    ('아우터'),
    ('신발'),
    ('액세서리');

-- ================================
-- 3. 상품 (item)
-- ================================
CREATE TABLE item (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    category_id BIGINT          NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    description TEXT,
    price       INT             NOT NULL,
    status      ENUM('SELLING', 'SOLD_OUT', 'STOP') NOT NULL DEFAULT 'SELLING',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES category (id)
);

-- ================================
-- 4. 상품 옵션 (item_option)
-- 사이즈 + 색상 조합별 재고 관리
-- ================================
CREATE TABLE item_option (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    item_id         BIGINT      NOT NULL,
    size            ENUM('S', 'M', 'L', 'XL') NOT NULL,
    color           VARCHAR(30) NOT NULL,
    stock_quantity  INT         NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT fk_item_option_item FOREIGN KEY (item_id) REFERENCES item (id)
);

-- ================================
-- 5. 장바구니 (cart)
-- 회원 1명당 장바구니 1개
-- ================================
CREATE TABLE cart (
    id          BIGINT  NOT NULL AUTO_INCREMENT,
    member_id   BIGINT  NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_cart_member (member_id),
    CONSTRAINT fk_cart_member FOREIGN KEY (member_id) REFERENCES member (id)
);

-- ================================
-- 6. 장바구니 상품 (cart_item)
-- ================================
CREATE TABLE cart_item (
    id              BIGINT  NOT NULL AUTO_INCREMENT,
    cart_id         BIGINT  NOT NULL,
    item_option_id  BIGINT  NOT NULL,
    quantity        INT     NOT NULL DEFAULT 1,

    PRIMARY KEY (id),
    CONSTRAINT fk_cart_item_cart        FOREIGN KEY (cart_id)        REFERENCES cart (id),
    CONSTRAINT fk_cart_item_item_option FOREIGN KEY (item_option_id) REFERENCES item_option (id)
);

-- ================================
-- 7. 주문 (orders)
-- order는 MySQL 예약어라 orders 사용
-- ================================
CREATE TABLE orders (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    member_id           BIGINT          NOT NULL,
    total_price         INT             NOT NULL,
    status              ENUM('PENDING', 'PAID', 'SHIPPING', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    delivery_address    VARCHAR(255)    NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_orders_member FOREIGN KEY (member_id) REFERENCES member (id)
);

-- ================================
-- 8. 주문 상품 (order_item)
-- order_price: 주문 당시 가격 스냅샷
-- (나중에 상품 가격이 바뀌어도 주문 내역은 유지)
-- ================================
CREATE TABLE order_item (
    id              BIGINT  NOT NULL AUTO_INCREMENT,
    order_id        BIGINT  NOT NULL,
    item_option_id  BIGINT  NOT NULL,
    quantity        INT     NOT NULL,
    order_price     INT     NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_orders      FOREIGN KEY (order_id)       REFERENCES orders (id),
    CONSTRAINT fk_order_item_item_option FOREIGN KEY (item_option_id) REFERENCES item_option (id)
);
