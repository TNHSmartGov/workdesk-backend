package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskAgileInfoDTO;
import com.tnh.baseware.core.entities.task.TaskAgileInfo;
import com.tnh.baseware.core.forms.task.TaskAgileInfoEditorForm;
import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.mappers.project.ISprintMapper;
import com.tnh.baseware.core.repositories.project.ISprintRepository;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { ISprintMapper.class })
public interface ITaskAgileInfoMapper extends IGenericMapper<TaskAgileInfo, TaskAgileInfoEditorForm, TaskAgileInfoDTO> {

    @Mapping(target = "sprint", expression = "java(fetcher.formToEntity(sprintRepo, form.getSprintId()))")
    TaskAgileInfo formToEntity(TaskAgileInfoEditorForm form, @Context GenericEntityFetcher fetcher,
            @Context ISprintRepository sprintRepo);

    @Mapping(target = "sprint", expression = "java(fetcher.formToEntity(sprintRepo, form.getSprintId()))")
    void updateEntityFromForm(TaskAgileInfoEditorForm form, @MappingTarget TaskAgileInfo entity,
            @Context GenericEntityFetcher fetcher, @Context ISprintRepository sprintRepo);

    @Mapping(source = "task.id", target = "taskId")
    TaskAgileInfoDTO entityToDTO(TaskAgileInfo entity);
}
