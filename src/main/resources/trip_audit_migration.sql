-- Trip audit: who started / completed each LR
ALTER TABLE lrs ADD COLUMN IF NOT EXISTS started_by_user_id BIGINT REFERENCES users(id);
ALTER TABLE lrs ADD COLUMN IF NOT EXISTS completed_by_user_id BIGINT REFERENCES users(id);
