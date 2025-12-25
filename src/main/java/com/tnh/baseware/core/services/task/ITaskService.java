package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.enums.task.TaskAction;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskService extends IGenericService<Task, TaskEditorForm, TaskDTO, UUID> {
    public void performAction(UUID id, TaskAction action);
}
