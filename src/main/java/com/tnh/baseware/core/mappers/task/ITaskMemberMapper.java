package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskMemberDTO;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.forms.task.TaskMemberEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskMemberMapper
        extends IGenericMapper<TaskMember, TaskMemberEditorForm, TaskMemberDTO> {

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "userId", source = "user.id")
    TaskMemberDTO entityToDTO(TaskMember entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "user", ignore = true)
    TaskMember formToEntity(TaskMemberEditorForm form);
}
