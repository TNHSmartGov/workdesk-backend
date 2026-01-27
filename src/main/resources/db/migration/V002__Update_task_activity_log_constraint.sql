ALTER TABLE task_activity_logs DROP CONSTRAINT IF EXISTS task_activity_logs_action_type_check;

ALTER TABLE task_activity_logs ADD CONSTRAINT task_activity_logs_action_type_check CHECK (action_type IN (
    'CREATE_TASK',
    'UPDATE_INFO',
    'UPDATE_STATUS',
    'UPDATE_FIELD',
    'UPDATE_PROGRESS',
    'SYSTEM_UPDATE',
    'ASSIGN_MEMBER',
    'REMOVE_MEMBER',
    'MEMBER_SUBMIT',
    'UPLOAD_FILE',
    'ADD_COMMENT',
    'UPDATE_MEMBER_ROLE',
    'UPDATE_MEMBER_STATUS',
    'ADD_REQUIREMENT',
    'ASSIGN_REQUIREMENT',
    'COMPLETE_REQUIREMENT',
    'UNCOMPLETE_REQUIREMENT',
    'CLOSE_TASK'
));
