package com.tnh.baseware.core.events.listener;

import com.tnh.baseware.core.entities.task.TaskActivityLog;
import com.tnh.baseware.core.events.type.TaskActivityEvent;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class TaskActivityEventListener {
    private final ITaskActivityLogRepository taskActivityLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TaskActivityEvent event) {
        TaskActivityLog log = TaskActivityLog.builder()
                .task(event.task())
                .actor(event.actor())
                .actionType(event.actionType())
                .targetField(event.targetField())
                .oldValue(event.oldValue())
                .newValue(event.newValue())
                .build();
        taskActivityLogRepository.save(log);
    }
}
