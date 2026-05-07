-- Change snapshot_date (date) to snapshot_time (timestamp)
-- Drop constraint
ALTER TABLE organization_daily_stats DROP CONSTRAINT IF EXISTS uk_org_date;

-- Rename column to preserve data (optional, or just alter type)
-- Strategy: Add new column, migrate data, drop old
ALTER TABLE organization_daily_stats ADD COLUMN snapshot_time TIMESTAMP WITH TIME ZONE;

-- Migrate existing data (Assume UTC start of day for existing dates)
UPDATE organization_daily_stats SET snapshot_time = CAST(snapshot_date AS TIMESTAMP WITH TIME ZONE);

-- Drop old column
ALTER TABLE organization_daily_stats DROP COLUMN snapshot_date;

-- Add new constraint
-- Note: Enforcing unique "Day" with Instant is harder in SQL without functional index.
-- However, we can create a unique index on the snapshot_time if we guarantee normalization in code.
-- Or we can just index (organization_id, snapshot_time)
CREATE UNIQUE INDEX uk_org_snapshot_time ON organization_daily_stats (organization_id, snapshot_time);
