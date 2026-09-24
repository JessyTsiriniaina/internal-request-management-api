-- V5: soft delete for request_comments
ALTER TABLE request_comments ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE request_comments ADD COLUMN deleted_at TIMESTAMPTZ;
CREATE INDEX idx_request_comments_deleted ON request_comments(deleted);
