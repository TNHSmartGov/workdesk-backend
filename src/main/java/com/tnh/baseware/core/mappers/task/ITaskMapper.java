package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskMapper extends IGenericMapper<Task, TaskEditorForm, TaskDTO> {
    @Mapping(target = "taskList", expression = "java(fetcher.formToEntity(taskListRepository, form.getTaskListId()))")
    Task formToEntity(TaskEditorForm form,
                      @Context GenericEntityFetcher fetcher,
                      @Context ITaskListRepository taskListRepository);

    @Override
    TaskDTO entityToDTO(Task entity);
}
