package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskMapper;

import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.repositories.user.IUserOrganizationRepository;
import com.tnh.baseware.core.enums.OrganizationRole;

import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;

import com.tnh.baseware.core.services.task.ITaskQueryService;
import com.tnh.baseware.core.specs.*;
import com.tnh.baseware.core.utils.SecurityUtils;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import com.tnh.baseware.core.enums.task.MemberStatus;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.tnh.baseware.core.enums.task.LogActionType;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskCommentAttachmentRepository;
import com.tnh.baseware.core.repositories.task.ITaskCommentRepository;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.mappers.task.ITaskActivityLogMapper;
import com.tnh.baseware.core.mappers.task.ITaskCommentMapper;

import com.tnh.baseware.core.dtos.task.TaskTimelineItemDTO;
import com.tnh.baseware.core.dtos.task.TaskCommentDTO;
import com.tnh.baseware.core.dtos.task.TaskActivityLogDTO;
import com.tnh.baseware.core.dtos.task.TimelineActivityLogDTO;
import com.tnh.baseware.core.dtos.task.TimelineCommentDTO;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskQueryService extends GenericService<Task, TaskEditorForm, TaskDTO, ITaskRepository, ITaskMapper, UUID>
                implements ITaskQueryService {
        ITaskListRepository taskListRepository;
        ITaskMemberRepository taskMemberRepository;
        IUserOrganizationRepository userOrganizationRepository;
        SecurityUtils securityUtils;
        ITaskActivityLogRepository taskActivityLogRepository;
        ITaskCommentRepository taskCommentRepository;
        ITaskCommentAttachmentRepository taskCommentAttachmentRepository;
        ITaskActivityLogMapper taskActivityLogMapper;
        ITaskCommentMapper taskCommentMapper;

        public TaskQueryService(ITaskRepository repository,
                        ITaskMapper mapper,
                        MessageService messageService,
                        ITaskListRepository taskListRepository,
                        ITaskMemberRepository taskMemberRepository,
                        IUserOrganizationRepository userOrganizationRepository,
                        SecurityUtils securityUtils,
                        ITaskActivityLogRepository taskActivityLogRepository,
                        ITaskCommentRepository taskCommentRepository,
                        ITaskCommentAttachmentRepository taskCommentAttachmentRepository,
                        ITaskActivityLogMapper taskActivityLogMapper,
                        ITaskCommentMapper taskCommentMapper) {
                super(repository, mapper, messageService, Task.class);
                this.taskListRepository = taskListRepository;
                this.taskMemberRepository = taskMemberRepository;
                this.userOrganizationRepository = userOrganizationRepository;
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
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByProjectIdAndOrgId(projectId, orgId).stream()
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository))
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
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByTaskListIdAndOrgId(taskListId, orgId).stream()
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository))
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> findByTaskListId(UUID taskListId, Pageable pageable) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByTaskListIdAndOrgId(taskListId, orgId, pageable)
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository));
        }

        @Override
        @Transactional(readOnly = true)
        public List<TaskDTO> findAccessibleByUser() {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();

                Specification<Task> orgSpec = getOrgSpec(orgId);
                Specification<Task> accessSpec = (root, query, cb) -> {
                        var deletedPredicate = cb.equal(root.get("deleted"), false);
                        var subquery = query.subquery(UUID.class);
                        var taskMember = subquery.from(TaskMember.class);
                        subquery.select(taskMember.get("task").get("id"))
                                        .where(cb.equal(taskMember.get("user").get("id"), currentUser.getId()));
                        return cb.and(deletedPredicate, root.get("id").in(subquery));
                };

                Specification<Task> combinedSpec = isUnitManager(currentUser.getId(), orgId)
                                ? orgSpec.or(accessSpec)
                                : accessSpec;

                return repository.findAll(combinedSpec).stream()
                                .map(mapper::entityToDTO)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> findAccessibleByUser(Pageable pageable) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();

                Specification<Task> orgSpec = getOrgSpec(orgId);
                Specification<Task> accessSpec = (root, query, cb) -> {
                        var deletedPredicate = cb.equal(root.get("deleted"), false);
                        var subquery = query.subquery(UUID.class);
                        var taskMember = subquery.from(TaskMember.class);
                        subquery.select(taskMember.get("task").get("id"))
                                        .where(cb.equal(taskMember.get("user").get("id"), currentUser.getId()));
                        return cb.and(deletedPredicate, root.get("id").in(subquery));
                };

                Specification<Task> combinedSpec = isUnitManager(currentUser.getId(), orgId)
                                ? orgSpec.or(accessSpec)
                                : accessSpec;

                return repository.findAll(combinedSpec, pageable)
                                .map(mapper::entityToDTO);
        }

        private boolean isUnitManager(UUID userId, UUID orgId) {
                return userOrganizationRepository.findByUserIdAndOrganizationId(userId, orgId)
                                .map(uo -> {
                                        OrganizationRole role = uo.getOrganizationRole();
                                        return role == OrganizationRole.UNIT_LEADER || role == OrganizationRole.DEPUTY;
                                })
                                .orElse(false);
        }

        @Override
        @Transactional(readOnly = true)
        public List<TaskDTO> findByStatus(TaskStatus status) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByStatusAndOrgId(status, orgId).stream()
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository))
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> findByStatus(TaskStatus status, Pageable pageable) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();
                return repository.findByStatusAndOrgId(status, orgId, pageable)
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository));
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
                        var projectJoin = root.<Task, Project>join("project",
                                        JoinType.LEFT);
                        var orgJoin = projectJoin.<Project, Organization>join(
                                        "organization", JoinType.LEFT);
                        return cb.or(
                                        cb.isNull(root.get("project")),
                                        cb.equal(orgJoin.get("id"), orgId));
                };

                Specification<Task> createdBySpec = (root, query, cb) -> cb.equal(root.get("createdBy"),
                                currentUser.getUsername());

                var pageable = GenericSpecification.getPageable(securedRequest.getPage(), securedRequest.getSize());
                return repository.findAll(baseSpec.and(orgSpec.or(createdBySpec)), pageable)
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository));
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
                        var deletedPredicate = cb.equal(root.get("deleted"), false);
                        var subquery = query.subquery(UUID.class);
                        var taskMember = subquery.from(TaskMember.class);
                        subquery.select(taskMember.get("task").get("id"))
                                        .where(
                                                        cb.equal(taskMember.get("user").get("id"), currentUser.getId()),
                                                        taskMember.get("role").in(TaskMemberRole.ASSIGNEE,
                                                                        TaskMemberRole.LEAD));
                        return cb.and(deletedPredicate, root.get("id").in(subquery));
                };

                Specification<Task> orgSpec = getOrgSpec(orgId);
                var combinedSpec = baseSpec.and(orgSpec.or(assignedToMeSpec));
                var pageable = GenericSpecification.getPageable(securedRequest.getPage(), securedRequest.getSize());
                return repository.findAll(combinedSpec, pageable)
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository));
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> searchAccessibleByUser(SearchRequest searchRequest) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();

                var baseSpec = new GenericSpecification<Task>(searchRequest);
                var orgSpec = getOrgSpec(orgId);

                Specification<Task> accessSpec = (root, query, cb) -> {
                        var deletedPredicate = cb.equal(root.get("deleted"), false);
                        var subquery = query.subquery(UUID.class);
                        var taskMember = subquery.from(TaskMember.class);
                        subquery.select(taskMember.get("task").get("id"))
                                        .where(cb.equal(taskMember.get("user").get("id"), currentUser.getId()));
                        return cb.and(deletedPredicate, root.get("id").in(subquery));
                };

                Specification<Task> combinedSpec = isUnitManager(currentUser.getId(), orgId)
                                ? baseSpec.and(orgSpec.or(accessSpec))
                                : baseSpec.and(accessSpec);

                var pageable = GenericSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize());
                return repository.findAll(combinedSpec, pageable)
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository));
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> searchByProjectId(UUID projectId, SearchRequest searchRequest) {
                UUID orgId = securityUtils.currentOrgId();
                var baseSpec = new GenericSpecification<Task>(searchRequest);
                var orgSpec = getOrgSpec(orgId);
                Specification<Task> projectSpec = (root, query, cb) -> cb.equal(root.get("project").get("id"),
                                projectId);

                var pageable = GenericSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize());
                return repository.findAll(baseSpec.and(orgSpec).and(projectSpec), pageable).map(mapper::entityToDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> searchByTaskListId(UUID taskListId, SearchRequest searchRequest) {
                UUID orgId = securityUtils.currentOrgId();
                var baseSpec = new GenericSpecification<Task>(searchRequest);
                var orgSpec = getOrgSpec(orgId);
                Specification<Task> taskListSpec = (root, query, cb) -> cb.equal(root.get("taskList").get("id"),
                                taskListId);

                var pageable = GenericSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize());
                return repository.findAll(baseSpec.and(orgSpec).and(taskListSpec), pageable).map(mapper::entityToDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> searchByStatus(TaskStatus status, SearchRequest searchRequest) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();

                var baseSpec = new GenericSpecification<Task>(searchRequest);
                var orgSpec = getOrgSpec(orgId);
                Specification<Task> statusSpec = (root, query, cb) -> cb.equal(root.get("status"), status);

                Specification<Task> accessSpec = (root, query, cb) -> {
                        var deletedPredicate = cb.equal(root.get("deleted"), false);
                        var subquery = query.subquery(UUID.class);
                        var taskMember = subquery.from(TaskMember.class);
                        subquery.select(taskMember.get("task").get("id"))
                                        .where(cb.equal(taskMember.get("user").get("id"), currentUser.getId()));
                        return cb.and(deletedPredicate, root.get("id").in(subquery));
                };

                Specification<Task> combinedSpec = isUnitManager(currentUser.getId(), orgId)
                                ? baseSpec.and(statusSpec).and(orgSpec.or(accessSpec))
                                : baseSpec.and(statusSpec).and(accessSpec);

                var pageable = GenericSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize());
                return repository.findAll(combinedSpec, pageable)
                                .map(mapper::entityToDTO);
        }

        private Specification<Task> getOrgSpec(UUID orgId) {
                return (root, query, cb) -> {
                        var deletedPredicate = cb.equal(root.get("deleted"), false);

                        if (Boolean.TRUE.equals(securityUtils.checkIsSuperAdmin())) {
                                return deletedPredicate;
                        }
                        var projectJoin = root.<Task, Project>join("project",
                                        jakarta.persistence.criteria.JoinType.LEFT);
                        var orgJoin = projectJoin.<Project, Organization>join(
                                        "organization", jakarta.persistence.criteria.JoinType.LEFT);

                        var orgPredicate = cb.or(
                                        cb.isNull(root.get("project")),
                                        cb.equal(orgJoin.get("id"), orgId));

                        return cb.and(deletedPredicate, orgPredicate);
                };
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
        @org.springframework.cache.annotation.Cacheable(value = "task_timeline", key = "#taskId")
        public List<TaskTimelineItemDTO> getTaskTimeline(UUID taskId) {
                // 1. Convert logs to timeline items
                var logItems = taskActivityLogRepository.findByTaskId(taskId).stream()
                                .filter(log -> log.getActionType() != LogActionType.ADD_COMMENT)
                                .map(taskActivityLogMapper::entityToDTO)
                                .map(this::toTimelineLogDTO)
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
                                .map(this::toTimelineCommentDTO)
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
        @Transactional(readOnly = true)
        public Page<TaskDTO> search(SearchRequest searchRequest) {
                UUID orgId = securityUtils.currentOrgId();
                UUID userId = getCurrentUser().getId();

                // 1. Filter theo Organization (Bắt buộc)
                Specification<Task> orgSpec = (root, query, cb) -> cb
                                .equal(root.get("project").get("organization").get("id"), orgId);

                // 2. Filter theo Quyền truy cập (Người dùng tham gia Task hoặc Project)
                Specification<Task> accessSpec = (root, query, cb) -> {
                        // Subquery cho TaskMember
                        var taskMemberSubquery = query.subquery(UUID.class);
                        var taskMember = taskMemberSubquery.from(TaskMember.class);
                        taskMemberSubquery.select(taskMember.get("task").get("id"))
                                        .where(cb.equal(taskMember.get("user").get("id"), userId));

                        // Subquery cho ProjectMember
                        var projectMemberSubquery = query.subquery(UUID.class);
                        var projectMember = projectMemberSubquery.from(ProjectMember.class);
                        projectMemberSubquery.select(projectMember.get("project").get("id"))
                                        .where(cb.equal(projectMember.get("user").get("id"), userId));

                        return cb.or(
                                        root.get("id").in(taskMemberSubquery),
                                        root.get("project").get("id").in(projectMemberSubquery));
                };

                // 3. Filter động từ SearchRequest
                var searchSpec = new GenericSpecification<Task>(searchRequest);

                // Kết hợp tất cả bằng AND
                var combinedSpec = Specification.where(orgSpec).and(accessSpec).and(searchSpec);

                var pageable = GenericSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize());
                return repository.findAll(combinedSpec, pageable)
                                .map(entity -> mapper.entityToDTO(entity, getCurrentUser(), taskMemberRepository));
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> findAll(Pageable pageable) {
                UUID orgId = securityUtils.currentOrgId();

                // Mặc định findAll trả về các task liên quan đến người dùng trong Org
                List<FilterRequest> filters = new ArrayList<>();
                filters.add(FilterRequest.builder()
                                .key("project.organization.id")
                                .operator(Operator.EQUAL)
                                .fieldType(FieldType.UUID)
                                .value(orgId.toString())
                                .build());

                return search(SearchRequest.builder()
                                .filters(filters)
                                .page(pageable.getPageNumber())
                                .size(pageable.getPageSize())
                                .build());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> searchTasksReviewingByMe(SearchRequest searchRequest) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();
                List<FilterRequest> filters = new ArrayList<>();
                if (searchRequest != null && searchRequest.getFilters() != null) {
                        filters.addAll(searchRequest.getFilters());
                }
                // Bắt buộc lọc theo trạng thái REVIEW
                // Nếu muốn tìm linh hoạt thì bỏ đoạn này và để Client truyền filter
                filters.add(FilterRequest.builder()
                                .key("status")
                                .operator(Operator.EQUAL)
                                .fieldType(FieldType.STRING)
                                .value(TaskStatus.REVIEW.getValue())
                                .build());
                SearchRequest securedRequest = SearchRequest.builder()
                                .filters(filters)
                                .sorts(searchRequest != null ? searchRequest.getSorts() : null)
                                .page(searchRequest != null ? searchRequest.getPage() : null)
                                .size(searchRequest != null ? searchRequest.getSize() : null)
                                .build();

                var baseSpec = new GenericSpecification<Task>(securedRequest);
                // Spec lọc theo Role: OWNER hoặc LEAD
                Specification<Task> reviewingSpec = (root, query, cb) -> {
                        var subquery = query.subquery(UUID.class);
                        var taskMember = subquery.from(TaskMember.class);
                        subquery.select(taskMember.get("task").get("id"))
                                        .where(
                                                        cb.equal(taskMember.get("user").get("id"), currentUser.getId()),
                                                        taskMember.get("role").in(TaskMemberRole.OWNER,
                                                                        TaskMemberRole.LEAD));
                        return root.get("id").in(subquery);
                };
                // Spec lọc theo Organization
                Specification<Task> orgSpec = getOrgSpec(orgId);
                var combinedSpec = baseSpec.and(reviewingSpec).and(orgSpec);
                var pageable = GenericSpecification.getPageable(securedRequest.getPage(), securedRequest.getSize());

                return repository.findAll(combinedSpec, pageable)
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository));
        }

        private TimelineActivityLogDTO toTimelineLogDTO(TaskActivityLogDTO dto) {
                if (dto == null)
                        return null;
                return TimelineActivityLogDTO.builder()
                                .id(dto.getId())
                                .taskId(dto.getTaskId())
                                .actorId(dto.getActorId())
                                .actionType(dto.getActionType())
                                .targetField(dto.getTargetField())
                                .oldValue(dto.getOldValue())
                                .newValue(dto.getNewValue())
                                .createdDate(dto.getCreatedDate())
                                .build();
        }

        private TimelineCommentDTO toTimelineCommentDTO(TaskCommentDTO dto) {
                if (dto == null)
                        return null;
                return TimelineCommentDTO.builder()
                                .id(dto.getId())
                                .task(dto.getTask())
                                .user(dto.getUser())
                                .content(dto.getContent())
                                .type(dto.getType())
                                .createdDate(dto.getCreatedDate())
                                .parentId(dto.getParentId())
                                // .replies(dto.getReplies()) // Recursion not needed for top level or handling
                                // separately
                                .replyCount(dto.getReplyCount())
                                .attachmentCount(dto.getAttachmentCount())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TaskDTO> searchByMemberStatus(SearchRequest searchRequest) {
                User currentUser = getCurrentUser();
                UUID orgId = securityUtils.currentOrgId();

                // Extract và remove filter "members.status" từ searchRequest (nếu có)
                // vì Task entity không có relationship với TaskMember nên GenericSpecification
                // không thể xử lý
                List<FilterRequest> otherFilters = new ArrayList<>();
                FilterRequest memberStatusFilter = null;

                if (searchRequest != null && searchRequest.getFilters() != null) {
                        for (FilterRequest filter : searchRequest.getFilters()) {
                                if (filter.getKey().equals("members.status")) {
                                        memberStatusFilter = filter;
                                } else {
                                        otherFilters.add(filter);
                                }
                        }
                }

                // Tạo SearchRequest mới không có filter "members.status"
                SearchRequest modifiedRequest = SearchRequest.builder()
                                .filters(otherFilters)
                                .sorts(searchRequest != null ? searchRequest.getSorts() : null)
                                .page(searchRequest != null ? searchRequest.getPage() : null)
                                .size(searchRequest != null ? searchRequest.getSize() : null)
                                .build();

                var baseSpec = new GenericSpecification<Task>(modifiedRequest);
                var orgSpec = getOrgSpec(orgId);

                // Specification để filter tasks mà current user là member
                // Nếu có memberStatusFilter thì apply vào subquery
                final FilterRequest finalMemberStatusFilter = memberStatusFilter;
                Specification<Task> memberSpec = (root, query, cb) -> {
                        var subquery = query.subquery(UUID.class);
                        var taskMember = subquery.from(TaskMember.class);

                        var userPredicate = cb.equal(taskMember.get("user").get("id"), currentUser.getId());

                        if (finalMemberStatusFilter != null) {
                                // Manually build predicate cho status vì Operator.build() không hoạt động đúng
                                // với subquery
                                // Key là "members.status" nhưng trong subquery ta chỉ cần "status"
                                var statusPath = taskMember.<MemberStatus>get("status");
                                Predicate statusPredicate;

                                switch (finalMemberStatusFilter.getOperator()) {
                                        case EQUAL:
                                                var statusValue = MemberStatus.fromValue(
                                                                finalMemberStatusFilter.getValue().toString());
                                                statusPredicate = cb.equal(statusPath, statusValue);
                                                break;
                                        case NOT_EQUAL:
                                                var notEqualValue = MemberStatus.fromValue(
                                                                finalMemberStatusFilter.getValue().toString());
                                                statusPredicate = cb.notEqual(statusPath, notEqualValue);
                                                break;
                                        case IN:
                                                var inValues = finalMemberStatusFilter.getValues().stream()
                                                                .map(v -> MemberStatus.fromValue(v.toString()))
                                                                .toArray(MemberStatus[]::new);
                                                statusPredicate = statusPath.in((Object[]) inValues);
                                                break;
                                        case NOT_IN:
                                                var notInValue = MemberStatus.fromValue(
                                                                finalMemberStatusFilter.getValue().toString());
                                                statusPredicate = cb.not(statusPath.in(notInValue));
                                                break;
                                        default:
                                                // Nếu operator không được hỗ trợ, bỏ qua filter này
                                                statusPredicate = cb.conjunction();
                                                break;
                                }

                                subquery.select(taskMember.get("task").get("id"))
                                                .where(cb.and(userPredicate, statusPredicate));
                        } else {
                                subquery.select(taskMember.get("task").get("id"))
                                                .where(userPredicate);
                        }

                        return root.get("id").in(subquery);
                };

                var pageable = GenericSpecification.getPageable(modifiedRequest.getPage(), modifiedRequest.getSize());
                return repository.findAll(baseSpec.and(orgSpec).and(memberSpec), pageable)
                                .map(entity -> mapper.entityToDTO(entity, currentUser, taskMemberRepository));
        }
}
