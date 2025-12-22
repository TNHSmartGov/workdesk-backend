package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskRequirementDTO;
import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.forms.task.TaskRequirementEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskRequirementMapper;
import com.tnh.baseware.core.repositories.task.ITaskRequirementRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskRequirementService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskRequirementService extends GenericService<TaskRequirement, TaskRequirementEditorForm, TaskRequirementDTO, ITaskRequirementRepository, ITaskRequirementMapper, UUID> implements ITaskRequirementService {

    public TaskRequirementService(ITaskRequirementRepository repository, ITaskRequirementMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, TaskRequirement.class);
    }
}
