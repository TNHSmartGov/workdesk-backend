package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskAgileInfoDTO;
import com.tnh.baseware.core.entities.task.TaskAgileInfo;
import com.tnh.baseware.core.forms.task.TaskAgileInfoEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskAgileInfoService
        extends IGenericService<TaskAgileInfo, TaskAgileInfoEditorForm, TaskAgileInfoDTO, UUID> {

    TaskAgileInfoDTO findByTaskId(UUID taskId);

    TaskAgileInfoDTO updateByTaskId(UUID taskId, TaskAgileInfoEditorForm form);
}
