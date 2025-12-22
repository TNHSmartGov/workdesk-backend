package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.forms.task.TaskRequirementEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskRequirementMapper extends IGenericMapper<TaskRequirement, TaskRequirementEditorForm, TaskRequirementDTO> {

    @Override
    @Mapping(target = "taskId", source = "task.id")
    TaskRequirementDTO entityToDTO(TaskRequirement entity);

    @Override
    @Mapping(target = "task.id", source = "taskId")
    TaskRequirement formToEntity(TaskRequirementEditorForm form);
}
