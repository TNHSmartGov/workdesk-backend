package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.enums.task.TaskAction;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskMapper;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskService extends GenericService<Task, TaskEditorForm, TaskDTO, ITaskRepository, ITaskMapper, UUID> implements ITaskService {

    public TaskService(ITaskRepository repository, ITaskMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, Task.class);
    }

//    @Override
//    public TaskDTO create(TaskEditorForm form) {
//        TaskDTO dto = new TaskDTO();
////        if (form.getTaskListId() == null) {
////
////        }
//    }

    @Override
    public void performAction(UUID id, TaskAction action) {

    }
}
