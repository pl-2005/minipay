CREATE TABLE merchant_user (
    user_id BIGINT NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    merchant_id BIGINT NOT NULL REFERENCES merchant (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, merchant_id)
);

CREATE INDEX idx_merchant_user_merchant_id ON merchant_user (merchant_id);

INSERT INTO app_user (username, password_hash, role)
VALUES
    ('merchant-sunrise', '{noop}password', 'MERCHANT'),
    ('merchant-harbor', '{noop}password', 'MERCHANT'),
    ('merchant-northwind', '{noop}password', 'MERCHANT'),
    ('merchant-greenfield', '{noop}password', 'MERCHANT'),
    ('merchant-nova', '{noop}password', 'MERCHANT');

INSERT INTO merchant (merchant_no, merchant_name, callback_url)
VALUES
    ('M10002', 'Sunrise Market', 'http://sunrise.example.local/pay/callback'),
    ('M10003', 'Blue Harbor Hotel', 'http://harbor.example.local/pay/callback'),
    ('M10004', 'Northwind Books', 'http://northwind.example.local/pay/callback'),
    ('M10005', 'Green Field Cafe', 'http://greenfield.example.local/pay/callback'),
    ('M10006', 'Nova Digital', 'http://nova.example.local/pay/callback');

INSERT INTO merchant_user (user_id, merchant_id)
SELECT app_user.id, merchant.id
FROM app_user
CROSS JOIN merchant
WHERE app_user.username = 'merchant-demo'
  AND merchant.merchant_no IN ('M10001', 'M10002', 'M10003', 'M10004', 'M10005', 'M10006');

INSERT INTO merchant_user (user_id, merchant_id)
SELECT app_user.id, merchant.id
FROM app_user
JOIN merchant ON merchant.merchant_no = CASE app_user.username
    WHEN 'merchant-sunrise' THEN 'M10002'
    WHEN 'merchant-harbor' THEN 'M10003'
    WHEN 'merchant-northwind' THEN 'M10004'
    WHEN 'merchant-greenfield' THEN 'M10005'
    WHEN 'merchant-nova' THEN 'M10006'
END
WHERE app_user.username IN (
    'merchant-sunrise',
    'merchant-harbor',
    'merchant-northwind',
    'merchant-greenfield',
    'merchant-nova'
);

INSERT INTO pay_order (
    order_no, merchant_no, merchant_order_no, subject, amount, status,
    callback_url, expire_at, paid_at, created_at, updated_at
)
VALUES
    ('PSEED10001001', 'M10001', 'DEMO-M10001-001', 'Wireless keyboard', 299.00, 'PAID', 'http://merchant.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '7 days' + INTERVAL '10 minutes', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '6 days'),
    ('PSEED10001002', 'M10001', 'DEMO-M10001-002', 'USB-C charging dock', 189.90, 'PAID', 'http://merchant.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '8 minutes', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
    ('PSEED10001003', 'M10001', 'DEMO-M10001-003', 'Portable monitor', 1299.00, 'PENDING', 'http://merchant.example.local/pay/callback', CURRENT_TIMESTAMP + INTERVAL '2 days', NULL, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
    ('PSEED10002001', 'M10002', 'DEMO-M10002-001', 'Fresh fruit gift box', 128.00, 'PAID', 'http://sunrise.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '6 days' + INTERVAL '6 minutes', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('PSEED10002002', 'M10002', 'DEMO-M10002-002', 'Organic breakfast set', 76.50, 'PAID', 'http://sunrise.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '12 minutes', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('PSEED10002003', 'M10002', 'DEMO-M10002-003', 'Weekend grocery order', 216.80, 'PENDING', 'http://sunrise.example.local/pay/callback', CURRENT_TIMESTAMP + INTERVAL '1 day', NULL, CURRENT_TIMESTAMP - INTERVAL '90 minutes', CURRENT_TIMESTAMP - INTERVAL '90 minutes'),
    ('PSEED10003001', 'M10003', 'DEMO-M10003-001', 'Harbor view room', 688.00, 'PAID', 'http://harbor.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '8 days' + INTERVAL '15 minutes', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '7 days'),
    ('PSEED10003002', 'M10003', 'DEMO-M10003-002', 'Breakfast buffet', 98.00, 'PAID', 'http://harbor.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '4 days' + INTERVAL '5 minutes', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    ('PSEED10003003', 'M10003', 'DEMO-M10003-003', 'Late checkout service', 120.00, 'PENDING', 'http://harbor.example.local/pay/callback', CURRENT_TIMESTAMP + INTERVAL '1 day', NULL, CURRENT_TIMESTAMP - INTERVAL '70 minutes', CURRENT_TIMESTAMP - INTERVAL '70 minutes'),
    ('PSEED10004001', 'M10004', 'DEMO-M10004-001', 'Software architecture books', 356.00, 'PAID', 'http://northwind.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '7 days' + INTERVAL '9 minutes', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '6 days'),
    ('PSEED10004002', 'M10004', 'DEMO-M10004-002', 'Team reading bundle', 540.00, 'PAID', 'http://northwind.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '11 minutes', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('PSEED10004003', 'M10004', 'DEMO-M10004-003', 'Database handbook', 109.00, 'PENDING', 'http://northwind.example.local/pay/callback', CURRENT_TIMESTAMP + INTERVAL '1 day', NULL, CURRENT_TIMESTAMP - INTERVAL '50 minutes', CURRENT_TIMESTAMP - INTERVAL '50 minutes'),
    ('PSEED10005001', 'M10005', 'DEMO-M10005-001', 'Coffee tasting set', 88.00, 'PAID', 'http://greenfield.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '6 days' + INTERVAL '4 minutes', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('PSEED10005002', 'M10005', 'DEMO-M10005-002', 'Afternoon tea for two', 168.00, 'PAID', 'http://greenfield.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '7 minutes', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('PSEED10005003', 'M10005', 'DEMO-M10005-003', 'Cold brew subscription', 198.00, 'PENDING', 'http://greenfield.example.local/pay/callback', CURRENT_TIMESTAMP + INTERVAL '1 day', NULL, CURRENT_TIMESTAMP - INTERVAL '35 minutes', CURRENT_TIMESTAMP - INTERVAL '35 minutes'),
    ('PSEED10006001', 'M10006', 'DEMO-M10006-001', 'Cloud storage annual plan', 399.00, 'PAID', 'http://nova.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '3 minutes', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
    ('PSEED10006002', 'M10006', 'DEMO-M10006-002', 'Design asset package', 259.00, 'PAID', 'http://nova.example.local/pay/callback', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '6 minutes', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('PSEED10006003', 'M10006', 'DEMO-M10006-003', 'Analytics starter plan', 599.00, 'PENDING', 'http://nova.example.local/pay/callback', CURRENT_TIMESTAMP + INTERVAL '1 day', NULL, CURRENT_TIMESTAMP - INTERVAL '20 minutes', CURRENT_TIMESTAMP - INTERVAL '20 minutes');

INSERT INTO payment_record (
    payment_no, order_no, merchant_no, amount, status, idempotency_key, paid_at, created_at
)
SELECT
    'PAY' || SUBSTRING(order_no FROM 2),
    order_no,
    merchant_no,
    amount,
    'SUCCESS',
    'SEED-' || merchant_order_no,
    paid_at,
    paid_at
FROM pay_order
WHERE order_no LIKE 'PSEED%'
  AND status = 'PAID';
