package com.tnh.baseware.core.services.task.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnh.baseware.core.dtos.notification.NotificationMessage;
import com.tnh.baseware.core.dtos.task.TaskCommentDTO;
import com.tnh.baseware.core.entities.task.TaskComment;
import com.tnh.baseware.core.enums.notification.NotificationType;
import com.tnh.baseware.core.forms.task.TaskCommentEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskCommentMapper;
import com.tnh.baseware.core.repositories.task.ITaskCommentRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.notification.INotificationService;
import com.tnh.baseware.core.services.task.ITaskCommentService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import com.tnh.baseware.core.events.factory.TaskActivityEventFactory;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskCommentService extends
        GenericService<TaskComment, TaskCommentEditorForm, TaskCommentDTO, ITaskCommentRepository, ITaskCommentMapper, UUID>
        implements ITaskCommentService {

    ApplicationEventPublisher eventPublisher;
    IUserRepository userRepository;
    INotificationService notificationService;
    ObjectMapper objectMapper;

    static Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9._-]{3,})");

    public TaskCommentService(ITaskCommentRepository repository, ITaskCommentMapper mapper,
            MessageService messageService, ApplicationEventPublisher eventPublisher,
            IUserRepository userRepository, INotificationService notificationService,
            ObjectMapper objectMapper) {
        super(repository, mapper, messageService, TaskComment.class);
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "task_timeline", key = "#form.taskId")
    public TaskCommentDTO create(TaskCommentEditorForm form) {
        if (form.getParentCommentId() != null) {
            var parent = repository.findById(form.getParentCommentId())
                    .orElseThrow(() -> new BWCNotFoundException(
                            messageService.getMessage("comment.not.found")));

            if (parent.getParentComment() != null) {
                // Determine if we should error or flatten. User requested "max 2 levels".
                // Often this implies strict validation.
                throw new BWCBusinessException(
                        messageService.getMessage("task.comment.max.depth.exceeded"));
            }
        }

        TaskCommentDTO dto = super.create(form);
        TaskComment savedComment = repository.findById(dto.getId()).orElse(null);

        if (savedComment != null) {
            eventPublisher.publishEvent(
                    TaskActivityEventFactory.commentAdded(
                            savedComment.getTask(),
                            savedComment.getCreatedBy(),
                            savedComment.getContent()));
            notifyMentions(savedComment);
        }

        return dto;
    }

    private void notifyMentions(TaskComment comment) {
        if (comment.getContent() == null || comment.getTask() == null) {
            return;
        }

        var matcher = MENTION_PATTERN.matcher(comment.getContent());
        Set<String> usernames = new HashSet<>();
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }

        if (usernames.isEmpty()) {
            return;
        }

        for (var username : usernames) {
            var recipient = userRepository.findByUsername(username).orElse(null);
            if (recipient == null) {
                continue;
            }
            if (comment.getCreatedBy() != null && comment.getCreatedBy().equalsIgnoreCase(username)) {
                continue;
            }

            String contentJson = buildMentionContent(comment, username);
            if (contentJson == null) {
                continue;
            }

            var message = NotificationMessage.builder()
                    .recipientId(recipient.getId())
                    .senderId(userRepository.findByUsername(comment.getCreatedBy())
                            .map(u -> u.getId()).orElse(null))
                    .type(NotificationType.MENTION)
                    .content(contentJson)
                    .dedupKey("mention_" + comment.getId() + "_" + recipient.getId())
                    .build();

            // NotificationService now handles publishing to Redis after transaction commit
            notificationService.createNotification(message);
        }
    }

    private String buildMentionContent(TaskComment comment, String mentionedUsername) {
        try {
            var payload = new HashMap<String, Object>();
            payload.put("taskId", comment.getTask().getId());
            payload.put("commentId", comment.getId());
            payload.put("actor", comment.getCreatedBy());
            payload.put("mention", mentionedUsername);
            payload.put("content", comment.getContent());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
