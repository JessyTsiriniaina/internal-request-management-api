-- V4: Workflow reasons and timestamps for strict state machine
ALTER TABLE requests ADD COLUMN rejection_reason TEXT;
ALTER TABLE requests ADD COLUMN cancellation_reason TEXT;
ALTER TABLE requests ADD COLUMN approved_at TIMESTAMPTZ;
ALTER TABLE requests ADD COLUMN rejected_at TIMESTAMPTZ;
ALTER TABLE requests ADD COLUMN cancelled_at TIMESTAMPTZ;
ALTER TABLE requests ADD COLUMN started_at TIMESTAMPTZ;
