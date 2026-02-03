package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskAgileInfoDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskAgileInfo;
import com.tnh.baseware.core.entities.task.TaskActivityLog;

import com.tnh.baseware.core.enums.task.LogActionType;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.task.TaskAgileInfoEditorForm;
import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.mappers.task.ITaskAgileInfoMapper;
import com.tnh.baseware.core.repositories.project.ISprintRepository;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskAgileInfoRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskAgileInfoService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskAgileInfoService extends
        GenericService<TaskAgileInfo, TaskAgileInfoEditorForm, TaskAgileInfoDTO, ITaskAgileInfoRepository, ITaskAgileInfoMapper, UUID>
        implements ITaskAgileInfoService {

    ISprintRepository sprintRepository;
    ITaskRepository taskRepository;
    GenericEntityFetcher fetcher;
    // Activity Logger
    ITaskActivityLogRepository taskActivityLogRepository;
    IUserRepository userRepository;

    public TaskAgileInfoService(ITaskAgileInfoRepository repository, ITaskAgileInfoMapper mapper,
            MessageService messageService, ISprintRepository sprintRepository, ITaskRepository taskRepository,
            GenericEntityFetcher fetcher,
            ITaskActivityLogRepository taskActivityLogRepository,
            IUserRepository userRepository) {
        super(repository, mapper, messageService, TaskAgileInfo.class);
        this.sprintRepository = sprintRepository;
        this.taskRepository = taskRepository;
        this.fetcher = fetcher;
        this.taskActivityLogRepository = taskActivityLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TaskAgileInfoDTO findByTaskId(UUID taskId) {
        return repository.findByTaskId(taskId)
                .map(mapper::entityToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public TaskAgileInfoDTO updateByTaskId(UUID taskId, TaskAgileInfoEditorForm form) {
        TaskAgileInfo agileInfo = repository.findById(taskId).orElse(null);
        var currentUser = getCurrentUser();
        if (agileInfo == null) {
            // Create new if not exists
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("task.not.found", taskId)));

            // Rule: CANCELLED tasks cannot be added to a sprint
            if (task.getStatus() == TaskStatus.CANCELLED && form.getSprintId() != null) {
                throw new BWCGenericRuntimeException(messageService.getMessage("task.cancelled.cannot.add.to.sprint"));
            }

            agileInfo = mapper.formToEntity(form, fetcher, sprintRepository);
            agileInfo.setTask(task);
            agileInfo.setId(taskId); // Ensure ID matches Task ID
        } else {
            // Rule: CANCELLED tasks cannot be added to a sprint
            if (agileInfo.getTask() != null && agileInfo.getTask().getStatus() == TaskStatus.CANCELLED
                    && form.getSprintId() != null) {
                throw new BWCGenericRuntimeException(messageService.getMessage("task.cancelled.cannot.add.to.sprint"));
            }

            // Update existing
            Double oldRemaining = agileInfo.getRemainingEstimate();
            mapper.updateEntityFromForm(form, agileInfo, fetcher, sprintRepository);

            // Log if remaining estimate changed (Burndown)
            if (form.getRemainingEstimate() != null && !form.getRemainingEstimate().equals(oldRemaining)) {
                TaskActivityLog log = new TaskActivityLog();
                log.setTask(agileInfo.getTask());
                log.setActor(currentUser);
                log.setActionType(LogActionType.UPDATE_FIELD);
                log.setTargetField("remainingEstimate");
                log.setOldValue(String.valueOf(oldRemaining));
                log.setNewValue(String.valueOf(form.getRemainingEstimate()));
                taskActivityLogRepository.save(log);
            }
        }

        repository.save(agileInfo);
        return mapper.entityToDTO(agileInfo);
    }
}
