package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskListDTO;
import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.forms.task.TaskListEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskListService extends IGenericService<TaskList, TaskListEditorForm, TaskListDTO, UUID> {
}
