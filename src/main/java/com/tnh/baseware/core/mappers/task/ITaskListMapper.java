package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskListDTO;
import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.forms.task.TaskListEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskListMapper extends IGenericMapper<TaskList, TaskListEditorForm, TaskListDTO> {

    @Override
    @Mapping(target = "project.id", source = "projectId")
    TaskList formToEntity(TaskListEditorForm form);

    @Override
    @Mapping(target = "project.id", source = "projectId")
    void formToEntity(TaskListEditorForm form, @org.mapstruct.MappingTarget TaskList entity);
}
