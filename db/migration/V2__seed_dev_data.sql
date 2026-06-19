INSERT INTO app_user (username, password_hash, role)
VALUES
    ('merchant-demo', '{noop}password', 'MERCHANT'),
    ('admin-demo', '{noop}password', 'ADMIN');

INSERT INTO merchant (merchant_no, merchant_name, callback_url)
VALUES ('M10001', 'MiniPay Demo Merchant', 'http://merchant.example.local/pay/callback');

