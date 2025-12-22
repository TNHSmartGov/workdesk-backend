package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskActivityLogDTO;
import com.tnh.baseware.core.entities.task.TaskActivityLog;
import com.tnh.baseware.core.forms.task.TaskActivityLogEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskActivityLogMapper;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskActivityLogService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskActivityLogService extends GenericService<TaskActivityLog, TaskActivityLogEditorForm, TaskActivityLogDTO, ITaskActivityLogRepository, ITaskActivityLogMapper, UUID> implements ITaskActivityLogService {

    public TaskActivityLogService(ITaskActivityLogRepository repository, ITaskActivityLogMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, TaskActivityLog.class);
    }
}
