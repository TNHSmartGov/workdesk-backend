package com.tnh.baseware.core.events.listener;

import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.enums.project.ProjectMemberRole;
import com.tnh.baseware.core.enums.project.ProjectStatus;
import com.tnh.baseware.core.enums.project.ProjectType;
import com.tnh.baseware.core.events.type.UserCreatedEvent;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.repositories.project.IProjectMemberRepository;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class ProjectEventListener {

    private final IProjectRepository projectRepository;
    private final IProjectMemberRepository projectMemberRepository;
    private final IUserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserCreatedEvent event) {

        String projectCode = "PERS_" + event.userId();
        Project project;
        try {
            project = projectRepository.save(
                    Project.builder()
                            .name("Personal Project")
                            .code(projectCode)
                            .type(ProjectType.PERSONAL)
                            .status(ProjectStatus.ACTIVE)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            project = projectRepository.findByCode(projectCode)
                    .orElseThrow(() -> new BWCNotFoundException("Project not found after duplicate error"));
        }

        try {
            projectMemberRepository.save(
                    ProjectMember.builder()
                            .project(project)
                            .user(userRepository.getReferenceById(event.userId()))
                            .role(ProjectMemberRole.OWNER)
                            .build()
            );
        } catch (DataIntegrityViolationException ignore) {
        }
    }
}
