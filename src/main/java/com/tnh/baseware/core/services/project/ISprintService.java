package com.tnh.baseware.core.services.project;

import com.tnh.baseware.core.dtos.project.SprintDTO;
import com.tnh.baseware.core.forms.project.SprintEditorForm;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.entities.project.Sprint;

import java.util.List;
import java.util.UUID;

public interface ISprintService extends IGenericService<Sprint, SprintEditorForm, SprintDTO, UUID> {

    List<SprintDTO> findAllByProjectId(UUID projectId);

    void startSprint(UUID id);

    void completeSprint(UUID id);
}
