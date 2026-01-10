package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskListDTO;
import com.tnh.baseware.core.dtos.task.TaskListProjectDTO;
import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.forms.task.TaskListEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskListMapper;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskListService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskListService
        extends GenericService<TaskList, TaskListEditorForm, TaskListDTO, ITaskListRepository, ITaskListMapper, UUID>
        implements ITaskListService {

    public TaskListService(ITaskListRepository repository, ITaskListMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, TaskList.class);
    }

    @Override
    public List<TaskListProjectDTO> getTaskListsByProjectId(UUID projectId) {
        List<TaskList> defaultTaskList = repository.findByProjectIdOrderByOrderIndexAsc(projectId);
        return defaultTaskList.stream().map(mapper::toTaskListProjectDTO).collect(Collectors.toList());
    }
}
