-- Drop existing check constraint
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;

-- Add updated check constraint with all notification types
ALTER TABLE notifications
    ADD CONSTRAINT notifications_type_check CHECK (
        type IN (
            'TASK_ASSIGNED',
            'TASK_COMPLETED',
            'TASK_COMMENT',
            'TASK_STATUS_CHANGED',
            'TASK_MEMBER_REMOVED',
            'TASK_MEMBER_ROLE_CHANGED',
            'TASK_MEMBER_STATUS_CHANGED',
            'TASK_DUE_SOON',
            'TASK_OVERDUE',
            'MENTION',
            'SYSTEM_ALERT'
        )
    );
