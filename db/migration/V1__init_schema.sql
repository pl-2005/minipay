CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE merchant (
    id BIGSERIAL PRIMARY KEY,
    merchant_no VARCHAR(64) NOT NULL UNIQUE,
    merchant_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    callback_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pay_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    merchant_no VARCHAR(64) NOT NULL,
    merchant_order_no VARCHAR(64) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    amount NUMERIC(18, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    callback_url VARCHAR(255),
    expire_at TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_pay_order_merchant_order UNIQUE (merchant_no, merchant_order_no)
);

CREATE INDEX idx_pay_order_merchant_status ON pay_order (merchant_no, status);
CREATE INDEX idx_pay_order_created_at ON pay_order (created_at);

CREATE TABLE payment_record (
    id BIGSERIAL PRIMARY KEY,
    payment_no VARCHAR(64) NOT NULL UNIQUE,
    order_no VARCHAR(64) NOT NULL,
    merchant_no VARCHAR(64) NOT NULL,
    amount NUMERIC(18, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_payment_idempotency UNIQUE (order_no, idempotency_key)
);

CREATE INDEX idx_payment_record_order_no ON payment_record (order_no);
CREATE INDEX idx_payment_record_merchant_no ON payment_record (merchant_no);

CREATE TABLE payment_event (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL UNIQUE,
    event_type VARCHAR(64) NOT NULL,
    aggregate_no VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_event_aggregate_no ON payment_event (aggregate_no);
CREATE INDEX idx_payment_event_status ON payment_event (status);

CREATE TABLE notify_record (
    id BIGSERIAL PRIMARY KEY,
    notify_no VARCHAR(64) NOT NULL UNIQUE,
    merchant_no VARCHAR(64) NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    callback_url VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    response_body TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notify_record_order_no ON notify_record (order_no);
CREATE INDEX idx_notify_record_status ON notify_record (status);

