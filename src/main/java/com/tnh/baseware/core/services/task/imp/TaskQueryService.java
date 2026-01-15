package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.enums.task.TaskStatus;
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
import com.tnh.baseware.core.specs.*;
import com.tnh.baseware.core.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskQueryService extends GenericService<Task, TaskEditorForm, TaskDTO, ITaskRepository, ITaskMapper, UUID>
        implements ITaskQueryService {
    ITaskListRepository taskListRepository;
    ITaskMemberRepository taskMemberRepository;
    IProjectService projectService;
    ITaskRequirementRepository taskRequirementRepository;
    SecurityUtils securityUtils;

    public TaskQueryService(ITaskRepository repository,
                            ITaskMapper mapper,
                            MessageService messageService,
                            ITaskListRepository taskListRepository,
                            ITaskMemberRepository taskMemberRepository,
                            ITaskRequirementRepository taskRequirementRepository,
                            IProjectService projectService,
                            SecurityUtils securityUtils) {
        super(repository, mapper, messageService, Task.class);
        this.taskListRepository = taskListRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.taskRequirementRepository = taskRequirementRepository;
        this.projectService = projectService;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> findByProjectId(UUID projectId) {
        return repository.findByProjectId(projectId).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> findByProjectId(UUID projectId, Pageable pageable) {
        return repository.findByProjectId(projectId, pageable)
                .map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> findByTaskListId(UUID taskListId) {
        return repository.findByTaskListId(taskListId).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> findByTaskListId(UUID taskListId, Pageable pageable) {
        return repository.findByTaskListId(taskListId, pageable)
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
        return repository.findByStatus(status).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> findByStatus(TaskStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable)
                .map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> searchTasksCreatedByMe(SearchRequest searchRequest) {
        User currentUser = getCurrentUser();
        UUID orgId = securityUtils.currentOrgId();

        if (searchRequest == null) {
            searchRequest = SearchRequest.builder().build();
        }
        if (searchRequest.getFilters() == null) {
            searchRequest.setFilters(new ArrayList<>());
        }
        searchRequest.getFilters().add(FilterRequest.builder()
                .key("createdBy")
                .operator(Operator.EQUAL)
                .fieldType(FieldType.STRING)
                .value(currentUser.getId())
                .build());
        searchRequest.getFilters().add(FilterRequest.builder()
                .key("project.organization.id")
                .operator(Operator.EQUAL)
                .fieldType(FieldType.UUID)
                .value(orgId.toString())
                .build());
        var specification = new GenericSpecification<Task>(searchRequest);
        var pageable = GenericSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize());
        return repository.findAll(specification, pageable).map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> searchTasksAssignedToMe(SearchRequest searchRequest) {
        User currentUser = getCurrentUser();
        UUID orgId = securityUtils.currentOrgId();

        if (searchRequest == null) {
            searchRequest = SearchRequest.builder().build();
        }
        if (searchRequest.getFilters() == null) {
            searchRequest.setFilters(new ArrayList<>());
        }

        searchRequest.getFilters().add(FilterRequest.builder()
                .key("project.organization.id")
                .operator(Operator.EQUAL)
                .fieldType(FieldType.UUID)
                .value(orgId.toString())
                .build());

        var baseSpec = new GenericSpecification<Task>(searchRequest);

        Specification<Task> assignedToMeSpec = (root, query, cb) -> {
            var subquery = query.subquery(UUID.class);
            var taskMember = subquery.from(TaskMember.class);
            subquery.select(taskMember.get("task").get("id"))
                    .where(
                            cb.equal(taskMember.get("user").get("id"), currentUser.getId()),
                            taskMember.get("role").in(TaskMemberRole.ASSIGNEE, TaskMemberRole.LEAD)
                    );
            return root.get("id").in(subquery);
        };
        var combinedSpec = baseSpec.and(assignedToMeSpec);
        var pageable = GenericSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize());
        return repository.findAll(combinedSpec, pageable).map(mapper::entityToDTO);
    }
}
