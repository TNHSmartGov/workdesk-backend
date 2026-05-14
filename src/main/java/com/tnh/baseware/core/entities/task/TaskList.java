package com.tnh.baseware.core.entities.task;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.enums.task.TaskListStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class TaskList extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    Integer orderIndex;

    @Column(nullable = false)
    @Builder.Default
    Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @OneToMany(mappedBy = "taskList", fetch = FetchType.LAZY)
    List<Task> tasks;

    Instant startDate;
    Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskListStatus status;

    @Column(columnDefinition = "text")
    String description;
}
