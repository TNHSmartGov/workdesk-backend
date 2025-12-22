package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskDependencyDTO;
import com.tnh.baseware.core.entities.task.TaskDependency;
import com.tnh.baseware.core.forms.task.TaskDependencyEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskDependencyService extends IGenericService<TaskDependency, TaskDependencyEditorForm, TaskDependencyDTO, UUID> {
}
