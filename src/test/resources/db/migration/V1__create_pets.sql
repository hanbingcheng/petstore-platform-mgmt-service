-- ユーザテーブル
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'ユーザーID',
    name       VARCHAR(100) NOT NULL                            COMMENT 'ユーザー名',
    email      VARCHAR(255) NOT NULL                            COMMENT 'メールアドレス',
    password_hash VARCHAR(255) NOT NULL                         COMMENT 'パスワード（ハッシュ化）',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '作成日時',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',

    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB COMMENT='ユーザー';

-- 注文テーブル
CREATE TABLE IF NOT EXISTS orders (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '注文ID',
    user_id      BIGINT       NOT NULL                            COMMENT 'ユーザーID',
    status       VARCHAR(20)  NOT NULL DEFAULT 'placed'           COMMENT 'ステータス: placed, approved, delivered, cancelled',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00             COMMENT '合計金額',
    order_date   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '注文日時',

    KEY idx_orders_user_id (user_id),
    KEY idx_orders_status (status)
) ENGINE=InnoDB COMMENT='注文';

-- 注文明細テーブル
CREATE TABLE IF NOT EXISTS order_items (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '明細ID',
    order_id   BIGINT       NOT NULL                            COMMENT '注文ID',
    pet_id     BIGINT       NOT NULL                            COMMENT 'ペットID (mgmt-service 管理)',
    pet_name   VARCHAR(100) NOT NULL                            COMMENT 'ペット名（注文時のスナップショット）',
    quantity   INT          NOT NULL                            COMMENT '数量',
    unit_price DECIMAL(12,2) NOT NULL                           COMMENT '単価',

    KEY idx_order_items_order_id (order_id),
    CONSTRAINT fk_order_items_order_id FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='注文明細';

-- カテゴリテーブル
CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'カテゴリID',
    name        VARCHAR(50)  NOT NULL                            COMMENT 'カテゴリ名',
    description VARCHAR(255)                                      COMMENT '説明',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '作成日時',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',

    UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB COMMENT='カテゴリ';

-- ペットテーブル
CREATE TABLE IF NOT EXISTS pets (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'ペットID',
    name        VARCHAR(100) NOT NULL                            COMMENT 'ペット名',
    category_id BIGINT       NOT NULL                            COMMENT 'カテゴリID',
    status      VARCHAR(20)  NOT NULL DEFAULT 'available'        COMMENT 'ステータス: available, pending, sold',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '作成日時',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',

    UNIQUE KEY uk_pets_name_category (name, category_id),
    KEY idx_pets_status (status),
    CONSTRAINT fk_pets_category_id FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB COMMENT='ペット';

-- ペットタグテーブル
CREATE TABLE IF NOT EXISTS pet_tags (
    id     BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'タグID',
    pet_id BIGINT       NOT NULL                            COMMENT 'ペットID',
    tag    VARCHAR(100) NOT NULL                            COMMENT 'タグ名',

    KEY idx_pet_tags_pet_id (pet_id),
    CONSTRAINT fk_pet_tags_pet_id FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='ペットタグ';