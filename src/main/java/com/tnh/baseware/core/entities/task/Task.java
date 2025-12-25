package com.tnh.baseware.core.entities.task;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.enums.task.TaskPriority;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.enums.task.TaskType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import lombok.*;
import lombok.Builder.Default;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "text")
    String description;

    Instant startDate;
    Instant dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_list_id")
    TaskList taskList;

    // bổ sung các trường tạo gantt
    @Builder.Default
    @Min(0) @Max(100)
    Integer progress = 0;
}
