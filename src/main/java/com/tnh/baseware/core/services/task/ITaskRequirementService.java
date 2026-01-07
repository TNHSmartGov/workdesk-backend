package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.forms.task.AssignRequirementForm;
import com.tnh.baseware.core.forms.task.TaskRequirementEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.List;
import java.util.UUID;

public interface ITaskRequirementService extends IGenericService<TaskRequirement, TaskRequirementEditorForm, TaskRequirementDTO, UUID> {
    TaskRequirementDTO create(UUID taskId, TaskRequirementEditorForm form);

    TaskRequirementDTO update(UUID taskId, UUID requirementId, TaskRequirementEditorForm form);

    void delete(UUID taskId, UUID requirementId);

    TaskRequirementDTO assignToMember(UUID taskId, UUID requirementId, AssignRequirementForm form);

    void toggleComplete(UUID taskId, UUID requirementId);

    List<TaskRequirementDTO> getByTaskId(UUID taskId);
}
