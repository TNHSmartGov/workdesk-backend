package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskActivityLogDTO;
import com.tnh.baseware.core.entities.task.TaskActivityLog;
import com.tnh.baseware.core.forms.task.TaskActivityLogEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskActivityLogService extends IGenericService<TaskActivityLog, TaskActivityLogEditorForm, TaskActivityLogDTO, UUID> {
}
