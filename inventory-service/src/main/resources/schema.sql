CREATE TABLE IF NOT EXISTS outbox (
    id UUID PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    message_key VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS outbox_pending ON outbox(created_at) WHERE sent_at IS NULL;

CREATE TABLE IF NOT EXISTS product (
    sku VARCHAR(100) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    available INTEGER NOT NULL CHECK (available >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS reservation (
    order_id UUID PRIMARY KEY,
    sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    amount NUMERIC(12, 2)
);
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS amount NUMERIC(12, 2);
INSERT INTO product(sku, name, price, available) VALUES
    ('SKU-001', 'Ban phim co', 850000, 6),
    ('SKU-002', 'Chuot khong day', 320000, 6),
    ('SKU-003', 'Tai nghe', 1250000, 6)
ON CONFLICT (sku) DO NOTHING;

CREATE TABLE IF NOT EXISTS processed_message (
    id UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
