package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskCommentDTO;
import com.tnh.baseware.core.entities.task.TaskComment;
import com.tnh.baseware.core.forms.task.TaskCommentEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskCommentMapper extends IGenericMapper<TaskComment, TaskCommentEditorForm, TaskCommentDTO> {

    @Override
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "userId", source = "user.id")
    TaskCommentDTO entityToDTO(TaskComment entity);

    @Override
    @Mapping(target = "task.id", source = "taskId")
    @Mapping(target = "user.id", source = "userId")
    TaskComment formToEntity(TaskCommentEditorForm form);
}
