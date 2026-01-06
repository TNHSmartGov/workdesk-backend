package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.enums.task.TaskAction;

import java.util.UUID;

public interface ITaskCommandService extends ITaskService {
    void performAction(UUID id, TaskAction action);
    void calculateProgressFromRequirements(UUID taskId);
    void updatePersonalProgress(UUID taskId, Integer progress);
}
