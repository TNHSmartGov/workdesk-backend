-- Refactor organization_daily_stats to single daily row
-- Drop old constraint if exists
ALTER TABLE organization_daily_stats DROP CONSTRAINT IF EXISTS uk_org_date_type;
-- Drop snapshot_type column
ALTER TABLE organization_daily_stats DROP COLUMN IF EXISTS snapshot_type;
-- Add new unique constraint (organization_id, snapshot_date)
ALTER TABLE organization_daily_stats ADD CONSTRAINT uk_org_date UNIQUE (organization_id, snapshot_date);
