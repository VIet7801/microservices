CREATE TABLE IF NOT EXISTS outbox (
    id UUID PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    message_key VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS outbox_pending ON outbox(created_at) WHERE sent_at IS NULL;

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(100),
    sku VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    total_amount NUMERIC(12, 2),
    status VARCHAR(30) NOT NULL
);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_id VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12, 2);
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;

CREATE TABLE IF NOT EXISTS order_saga (
    order_id UUID PRIMARY KEY,
    step VARCHAR(30) NOT NULL,
    payment_declined BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS processed_message (
    id UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
