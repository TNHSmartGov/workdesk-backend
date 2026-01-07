package com.tnh.baseware.core.constants;

public class MessageConstant {
    // Task constants
    public static final String TASK_LIST_NOT_FOUND = "Task list not found.";
    public static final String ERROR_CREATE_PROJECT_WITH_DEFAULT_TASK_LIST = "System error: Personal Project initialized without Default Task List";
    public static final String TASK_NOT_FOUND = "Task not found";
    public static final String PROGRESS_VALIDATE = "Progress must be 0-100";
    public static final String BLOCK_UPDATE_PROGRESS_MANUAL = "This task is managed by requirements. Please update the task's requirements";
    public static final String NOT_ASSIGNED_TO_TASK = "You are not assigned to this task";
    public static final String VALIDATE_START_ACTION = "Only TODO task can be started";
    public static final String VALIDATE_COMPLETE_ACTION = "Only IN_PROGRESS task can be completed";
    public static final String VALIDATE_APPROVE_ACTION = "Only reviewing task can be approved";
    public static final String VALIDATE_CANCEL_ACTION = "Completed task cannot be cancelled";
    public static final String NOT_ALLOW_PERFORM_ACTION = "You are not allowed to perform actions on this task";
    public static final String NOT_IN_PROJECT_TASK = "You are not a member of the project and task";
    public static final String INVALID_PROJECT_ROLE_CONFIG = "Invalid Project Role configuration";

    // User constants


    // Common constants
    public static final String UNSUPPORTED_ACTION = "Unsupported action";
}
