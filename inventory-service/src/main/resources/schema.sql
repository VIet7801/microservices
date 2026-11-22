CREATE TABLE IF NOT EXISTS outbox (
    id UUID PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    message_key VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS outbox_pending ON outbox(created_at) WHERE sent_at IS NULL;

CREATE TABLE IF NOT EXISTS stock (
    sku VARCHAR(100) PRIMARY KEY,
    available INTEGER NOT NULL CHECK (available >= 0)
);
CREATE TABLE IF NOT EXISTS reservation (
    order_id UUID PRIMARY KEY,
    sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL
);
INSERT INTO stock(sku, available) VALUES ('SKU-001', 6), ('SKU-002', 6), ('SKU-003', 6)
ON CONFLICT (sku) DO NOTHING;

CREATE TABLE IF NOT EXISTS processed_message (
    id UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
