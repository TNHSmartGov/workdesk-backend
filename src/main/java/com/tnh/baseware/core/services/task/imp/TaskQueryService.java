package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskMapper;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.repositories.task.ITaskRequirementRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectService;
import com.tnh.baseware.core.services.task.ITaskQueryService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskQueryService extends GenericService<Task, TaskEditorForm, TaskDTO, ITaskRepository, ITaskMapper, UUID>
        implements ITaskQueryService {
    ITaskListRepository taskListRepository;
    ITaskMemberRepository taskMemberRepository;
    IProjectService projectService;
    ITaskRequirementRepository taskRequirementRepository;

    public TaskQueryService(ITaskRepository repository,
            ITaskMapper mapper,
            MessageService messageService,
            ITaskListRepository taskListRepository,
            ITaskMemberRepository taskMemberRepository,
            ITaskRequirementRepository taskRequirementRepository,
            IProjectService projectService) {
        super(repository, mapper, messageService, Task.class);
        this.taskListRepository = taskListRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.taskRequirementRepository = taskRequirementRepository;
        this.projectService = projectService;
    }

}
