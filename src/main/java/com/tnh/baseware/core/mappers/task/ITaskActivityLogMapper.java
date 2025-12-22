package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskActivityLogDTO;
import com.tnh.baseware.core.entities.task.TaskActivityLog;
import com.tnh.baseware.core.forms.task.TaskActivityLogEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskActivityLogMapper extends IGenericMapper<TaskActivityLog, TaskActivityLogEditorForm, TaskActivityLogDTO> {

    @Override
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "actorId", source = "actor.id")
    TaskActivityLogDTO entityToDTO(TaskActivityLog entity);

    @Override
    @Mapping(target = "task.id", source = "taskId")
    @Mapping(target = "actor.id", source = "actorId")
    TaskActivityLog formToEntity(TaskActivityLogEditorForm form);
}
