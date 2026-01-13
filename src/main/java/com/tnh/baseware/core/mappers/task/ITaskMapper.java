package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.task.ITaskCategoryRepository;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskMapper extends IGenericMapper<Task, TaskEditorForm, TaskDTO> {

    @Mapping(target = "taskList", expression = "java(form.getTaskListId() != null ? fetcher.formToEntity(taskListRepository, form.getTaskListId()) : null)")
    @Mapping(target = "taskCategory", expression = "java(form.getTaskCategoryId() != null ? fetcher.formToEntity(taskCategoryRepository, form.getTaskCategoryId()) : null)")
    Task formToEntity(TaskEditorForm form,
            @Context GenericEntityFetcher fetcher,
            @Context ITaskListRepository taskListRepository,
            @Context ITaskCategoryRepository taskCategoryRepository);

    @Mapping(target = "taskList", expression = "java(form.getTaskListId() != null ? fetcher.formToEntity(taskListRepository, form.getTaskListId()) : null)")
    @Mapping(target = "taskCategory", expression = "java(form.getTaskCategoryId() != null ? fetcher.formToEntity(taskCategoryRepository, form.getTaskCategoryId()) : null)")
    void updateFromForm(TaskEditorForm form,
            @MappingTarget Task task,
            @Context GenericEntityFetcher fetcher,
            @Context ITaskListRepository taskListRepository,
            @Context ITaskCategoryRepository taskCategoryRepository);

    @Override
    TaskDTO entityToDTO(Task entity);
}
