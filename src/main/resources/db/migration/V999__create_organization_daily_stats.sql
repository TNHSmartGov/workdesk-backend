-- Migration: Create Organization Daily Stats Table
-- Version: V{next}__create_organization_daily_stats.sql

-- Create organization_daily_stats table
CREATE TABLE IF NOT EXISTS organization_daily_stats (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Foreign Keys
    organization_id UUID NOT NULL,
    
    -- Snapshot Info
    snapshot_date DATE NOT NULL,
    snapshot_type VARCHAR(20) NOT NULL CHECK (snapshot_type IN ('MIDDAY', 'END_OF_DAY')),
    
    -- Task Metrics (7 fields)
    total_tasks INTEGER,
    new_tasks_today INTEGER,
    completed_today INTEGER,
    overdue_tasks INTEGER,
    due_in_next_3_days INTEGER,
    in_progress_tasks INTEGER,
    avg_progress_rate DOUBLE PRECISION,
    
    -- Project Metrics (4 fields)
    total_projects INTEGER,
    active_projects INTEGER,
    overdue_projects INTEGER,
    completed_projects_today INTEGER,
    
    -- Performance Metrics (4 fields)
    completion_rate DOUBLE PRECISION,
    overdue_rate DOUBLE PRECISION,
    active_user_count INTEGER,
    avg_completion_time_hours DOUBLE PRECISION,
    
    -- Flexible Extended Metrics (JSONB)
    extended_metrics JSONB,
    
    -- Metadata
    calculated_at TIMESTAMP WITH TIME ZONE,
    is_archived BOOLEAN DEFAULT FALSE,
    archived_at TIMESTAMP WITH TIME ZONE,
    
    -- Audit Fields
    created_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    modified_date TIMESTAMP WITH TIME ZONE,
    modified_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    
    -- Constraints
    CONSTRAINT fk_org_stats_organization FOREIGN KEY (organization_id) 
        REFERENCES organization(id) ON DELETE CASCADE,
    CONSTRAINT uk_org_date_type UNIQUE (organization_id, snapshot_date, snapshot_type)
);

-- Create Indexes for Query Optimization

-- Index for querying by organization + date + type (most common query)
CREATE INDEX IF NOT EXISTS idx_org_stats_org_date_type 
    ON organization_daily_stats(organization_id, snapshot_date DESC, snapshot_type);

-- Index for date-based queries (trend analysis)
CREATE INDEX IF NOT EXISTS idx_org_stats_date 
    ON organization_daily_stats(snapshot_date DESC);

-- Partial index for archived records (archival queries)
CREATE INDEX IF NOT EXISTS idx_org_stats_archived 
    ON organization_daily_stats(is_archived, archived_at) 
    WHERE is_archived = TRUE;

-- GIN index for JSONB extended_metrics queries
CREATE INDEX IF NOT EXISTS idx_org_stats_extended_metrics 
    ON organization_daily_stats USING GIN (extended_metrics);

-- Add comments for documentation
COMMENT ON TABLE organization_daily_stats IS 'Daily statistics snapshots for organizations. Two snapshots per day: MIDDAY (12PM) and END_OF_DAY (5:30PM)';
COMMENT ON COLUMN organization_daily_stats.snapshot_type IS 'Type of snapshot: MIDDAY or END_OF_DAY';
COMMENT ON COLUMN organization_daily_stats.total_tasks IS 'Cumulative total tasks (excluding deleted)';
COMMENT ON COLUMN organization_daily_stats.new_tasks_today IS 'Tasks created on snapshot date';
COMMENT ON COLUMN organization_daily_stats.completed_today IS 'Tasks completed on snapshot date';
COMMENT ON COLUMN organization_daily_stats.overdue_tasks IS 'Tasks past due date (not DONE/CANCELLED)';
COMMENT ON COLUMN organization_daily_stats.due_in_next_3_days IS 'Tasks due within 3 days from snapshot time';
COMMENT ON COLUMN organization_daily_stats.completion_rate IS 'Percentage: completed_today / new_tasks_today * 100';
COMMENT ON COLUMN organization_daily_stats.overdue_rate IS 'Percentage: overdue_tasks / total_tasks * 100';
COMMENT ON COLUMN organization_daily_stats.extended_metrics IS 'Flexible JSON field for custom metrics (e.g., tasksByPriority, sprintMetrics)';
COMMENT ON COLUMN organization_daily_stats.is_archived IS 'TRUE if data is older than 2 years and moved to archive';
