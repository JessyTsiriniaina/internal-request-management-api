-- V2: Soft delete for requests (A with 2 cols)
ALTER TABLE requests ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE requests ADD COLUMN deleted_at TIMESTAMPTZ;
CREATE INDEX idx_requests_deleted ON requests(deleted) WHERE deleted = false;
