package com.tnh.baseware.core.services.task;

import java.util.UUID;

import com.tnh.baseware.core.forms.task.CreateTaskReportForm;
import com.tnh.baseware.core.forms.task.TaskActionForm;

public interface ITaskCommandService extends ITaskService {
    void performAction(UUID id, TaskActionForm form);

    void calculateProgressFromRequirements(UUID taskId);

    void updatePersonalProgress(UUID taskId, Integer progress);

    void recalculateTaskProgress(UUID taskId);

    void reportProgress(UUID taskId, CreateTaskReportForm form);
}
