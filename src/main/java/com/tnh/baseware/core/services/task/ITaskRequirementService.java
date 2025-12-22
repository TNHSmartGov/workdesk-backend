package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.forms.task.TaskRequirementEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskRequirementService extends IGenericService<TaskRequirement, TaskRequirementEditorForm, TaskRequirementDTO, UUID> {
}
