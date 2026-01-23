package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskMapper;

import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;

import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;

import com.tnh.baseware.core.services.task.ITaskQueryService;
import com.tnh.baseware.core.specs.*;
import com.tnh.baseware.core.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.tnh.baseware.core.enums.task.LogActionType;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskCommentAttachmentRepository;
import com.tnh.baseware.core.repositories.task.ITaskCommentRepository;
import com.tnh.baseware.core.mappers.task.ITaskActivityLogMapper;
import com.tnh.baseware.core.mappers.task.ITaskCommentMapper;
import com.tnh.baseware.core.dtos.task.TaskStatisticDTO;
import com.tnh.baseware.core.dtos.task.TaskTimelineItemDTO;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskQueryService extends GenericService<Task, TaskEditorForm, TaskDTO, ITaskRepository, ITaskMapper, UUID>
                implements ITaskQueryService {

        ITaskMemberRepository taskMemberRepository;
        SecurityUtils securityUtils;
        ITaskActivityLogRepository taskActivityLogRepository;
        ITaskCommentRepository taskCommentRepository;
        ITaskCommentAttachmentRepository taskCommentAttachmentRepository;
        ITaskActivityLogMapper taskActivityLogMapper;
        ITaskCommentMapper taskCommentMapper;

        public TaskQueryService(ITaskRepository repository,
                        ITaskMapper mapper,
                        MessageService messageService,

                        ITaskMemberRepository taskMemberRepository,

                        SecurityUtils securityUtils,
                        ITaskActivityLogRepository taskActivityLogRepository,
                        ITaskCommentRepository taskCommentRepository,
                        ITaskCommentAttachmentRepository taskCommentAttachmentRepository,
                        ITaskActivityLogMapper taskActivityLogMapper,
                        ITaskCommentMapper taskCommentMapper) {
                super(repository, mapper, messageService, Task.class);

                this.taskMemberRepository = taskMemberRepository;

                this.securityUtils = securityUtils;
                this.taskActivityLogRepository = taskActivityLogRepository;
                this.taskCommentRepository = taskCommentRepository;
                this.taskCommentAttachmentRepository = taskCommentAttachmentRepository;
                this.taskActivityLogMapper = taskActivityLogMapper;
                this.taskCommentMapper = taskCommentMapper;
        }

        @Override
        @Transactional(readOnly = true)
        public List<TaskDTO> findByProjectId(UUID projectId) {
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByProjectIdAndOrgId(projectId, orgId).stream()
                                .map(mapper::entityToDTO)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> findByProjectId(UUID projectId, Pageable pageable) {
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByProjectIdAndOrgId(projectId, orgId, pageable)
                                .map(mapper::entityToDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public List<TaskDTO> findByTaskListId(UUID taskListId) {
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByTaskListIdAndOrgId(taskListId, orgId).stream()
                                .map(mapper::entityToDTO)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> findByTaskListId(UUID taskListId, Pageable pageable) {
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByTaskListIdAndOrgId(taskListId, orgId, pageable)
                                .map(mapper::entityToDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public List<TaskDTO> findAccessibleByUser() {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();
                UUID userId = currentUser.getId();
                return repository.findAccessibleByUser(orgId, userId).stream()
                                .map(mapper::entityToDTO)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> findAccessibleByUser(Pageable pageable) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();
                UUID userId = currentUser.getId();
                return repository.findAccessibleByUser(orgId, userId, pageable)
                                .map(mapper::entityToDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public List<TaskDTO> findByStatus(TaskStatus status) {
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByStatusAndOrgId(status, orgId).stream()
                                .map(mapper::entityToDTO)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> findByStatus(TaskStatus status, Pageable pageable) {
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByStatusAndOrgId(status, orgId, pageable)
                                .map(mapper::entityToDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> searchTasksCreatedByMe(SearchRequest searchRequest) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();

                List<FilterRequest> filters = new ArrayList<>();
                if (searchRequest != null && searchRequest.getFilters() != null) {
                        filters.addAll(searchRequest.getFilters());
                }

                filters.add(FilterRequest.builder()
                                .key("createdBy")
                                .operator(Operator.EQUAL)
                                .fieldType(FieldType.STRING)
                                .value(currentUser.getUsername())
                                .build());

                SearchRequest securedRequest = SearchRequest.builder()
                                .filters(filters)
                                .sorts(searchRequest != null ? searchRequest.getSorts() : null)
                                .page(searchRequest != null ? searchRequest.getPage() : null)
                                .size(searchRequest != null ? searchRequest.getSize() : null)
                                .build();
                var baseSpec = new GenericSpecification<Task>(securedRequest);

                Specification<Task> orgSpec = (root, query, cb) -> {
                        if (Boolean.TRUE.equals(securityUtils.checkIsSuperAdmin())) {
                                return cb.conjunction();
                        }
                        return cb.or(
                                        cb.isNull(root.get("project")),
                                        cb.equal(root.get("project").get("organization").get("id"), orgId));
                };

                var pageable = GenericSpecification.getPageable(securedRequest.getPage(), securedRequest.getSize());
                return repository.findAll(baseSpec.and(orgSpec), pageable).map(mapper::entityToDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> searchTasksAssignedToMe(SearchRequest searchRequest) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();

                List<FilterRequest> filters = new ArrayList<>();
                if (searchRequest != null && searchRequest.getFilters() != null) {
                        filters.addAll(searchRequest.getFilters());
                }

                SearchRequest securedRequest = SearchRequest.builder()
                                .filters(filters)
                                .sorts(searchRequest != null ? searchRequest.getSorts() : null)
                                .page(searchRequest != null ? searchRequest.getPage() : null)
                                .size(searchRequest != null ? searchRequest.getSize() : null)
                                .build();
                var baseSpec = new GenericSpecification<Task>(securedRequest);
                Specification<Task> assignedToMeSpec = (root, query, cb) -> {
                        var subquery = query.subquery(UUID.class);
                        var taskMember = subquery.from(TaskMember.class);
                        subquery.select(taskMember.get("task").get("id"))
                                        .where(
                                                        cb.equal(taskMember.get("user").get("id"), currentUser.getId()),
                                                        taskMember.get("role").in(TaskMemberRole.ASSIGNEE,
                                                                        TaskMemberRole.LEAD));
                        return root.get("id").in(subquery);
                };

                Specification<Task> orgSpec = (root, query, cb) -> {
                        if (Boolean.TRUE.equals(securityUtils.checkIsSuperAdmin())) {
                                return cb.conjunction();
                        }
                        return cb.or(
                                        cb.isNull(root.get("project")),
                                        cb.equal(root.get("project").get("organization").get("id"), orgId));
                };

                var combinedSpec = baseSpec.and(assignedToMeSpec).and(orgSpec);
                var pageable = GenericSpecification.getPageable(securedRequest.getPage(), securedRequest.getSize());
                return repository.findAll(combinedSpec, pageable).map(mapper::entityToDTO);
        }

        @Override
        @Transactional
        public TaskDTO create(TaskEditorForm form) {
                var currentUser = getCurrentUser();
                var task = repository.save(mapper.formToEntity(form));
                var taskMember = TaskMember.builder()
                                .task(task)
                                .user(currentUser)
                                .role(TaskMemberRole.OWNER)
                                .build();
                taskMemberRepository.save(taskMember);
                return mapper.entityToDTO(task);
        }

        @Override
        @Transactional(readOnly = true)
        public List<TaskTimelineItemDTO> getTaskTimeline(UUID taskId) {
                // 1. Convert logs to timeline items
                var logItems = taskActivityLogRepository.findByTaskId(taskId).stream()
                                .filter(log -> log.getActionType() != LogActionType.ADD_COMMENT)
                                .map(taskActivityLogMapper::entityToDTO)
                                .map(dto -> TaskTimelineItemDTO.builder()
                                                .type("ACTIVITY")
                                                .id(dto.getId())
                                                .timestamp(dto.getCreatedDate())
                                                .activity(dto)
                                                .build())
                                .collect(Collectors.toList());

                // 2. Fetch ROOT comments and populate counts (Lazy Loading)
                var rootComments = taskCommentRepository.findByTaskIdAndParentCommentIsNull(taskId).stream()
                                .map(comment -> {
                                        var dto = taskCommentMapper.entityToDTO(comment);
                                        dto.setReplyCount(
                                                        taskCommentRepository.countByParentComment_Id(comment.getId()));
                                        dto.setAttachmentCount(taskCommentAttachmentRepository
                                                        .countByComment_Id(comment.getId()));
                                        dto.setReplies(null);
                                        return dto;
                                })
                                .collect(Collectors.toList());

                // 3. Convert root comments to timeline items
                var commentItems = rootComments.stream()
                                .map(dto -> TaskTimelineItemDTO.builder()
                                                .type("COMMENT")
                                                .id(dto.getId())
                                                .timestamp(dto.getCreatedDate())
                                                .comment(dto)
                                                .build())
                                .collect(Collectors.toList());

                // 4. Merge and sort reverse chronologically
                List<TaskTimelineItemDTO> result = new ArrayList<>(logItems);
                result.addAll(commentItems);
                result.sort(Comparator.comparing(
                                TaskTimelineItemDTO::getTimestamp,
                                Comparator.nullsLast(Comparator.reverseOrder())));

                return result;
        }

        @Override
        public TaskStatisticDTO getDashboardStatistics() {
                UUID orgId = securityUtils.currentOrgId();
                UUID userId = securityUtils.currentUser().getId();
                java.time.Instant now = java.time.Instant.now();
                java.time.Instant future = now.plus(1, java.time.temporal.ChronoUnit.DAYS);

                TaskStatisticDTO stats = new TaskStatisticDTO();
                stats.setTotal(repository.countAccessibleByUser(orgId, userId));
                stats.setTotalNew(repository.countAccessibleByStatus(orgId, userId, TaskStatus.TODO));
                stats.setTotalInProgress(repository.countAccessibleByStatus(orgId, userId, TaskStatus.IN_PROGRESS));
                stats.setTotalReview(repository.countAccessibleByStatus(orgId, userId, TaskStatus.REVIEW));
                stats.setTotalCompleted(repository.countAccessibleByStatus(orgId, userId, TaskStatus.DONE));
                stats.setTotalOverdue(repository.countAccessibleOverdue(orgId, userId, now));
                stats.setTotalDueSoon(repository.countAccessibleDueSoon(orgId, userId, now, future));

                return stats;
        }
}
