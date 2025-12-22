package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskDependencyDTO;
import com.tnh.baseware.core.entities.task.TaskDependency;
import com.tnh.baseware.core.forms.task.TaskDependencyEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskDependencyMapper;
import com.tnh.baseware.core.repositories.task.ITaskDependencyRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskDependencyService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskDependencyService extends GenericService<TaskDependency, TaskDependencyEditorForm, TaskDependencyDTO, ITaskDependencyRepository, ITaskDependencyMapper, UUID> implements ITaskDependencyService {

    public TaskDependencyService(ITaskDependencyRepository repository, ITaskDependencyMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, TaskDependency.class);
    }
}
