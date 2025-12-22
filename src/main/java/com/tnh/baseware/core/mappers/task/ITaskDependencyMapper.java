package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskDependencyDTO;
import com.tnh.baseware.core.entities.task.TaskDependency;
import com.tnh.baseware.core.forms.task.TaskDependencyEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskDependencyMapper extends IGenericMapper<TaskDependency, TaskDependencyEditorForm, TaskDependencyDTO> {

    @Override
    @Mapping(target = "fromTask.id", source = "fromTaskId")
    @Mapping(target = "toTask.id", source = "toTaskId")
    TaskDependency formToEntity(TaskDependencyEditorForm form);

    @Override
    @Mapping(target = "fromTask.id", source = "fromTaskId")
    @Mapping(target = "toTask.id", source = "toTaskId")
    void formToEntity(TaskDependencyEditorForm form, @org.mapstruct.MappingTarget TaskDependency entity);
}
