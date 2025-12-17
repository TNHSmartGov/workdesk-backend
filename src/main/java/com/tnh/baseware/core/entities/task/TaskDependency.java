package com.tnh.baseware.core.entities.task;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.enums.task.TaskDependencyType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"from_task_id", "to_task_id", "dependency_type"})
        }
)
public class TaskDependency extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_task_id", nullable = false)
    private Task fromTask;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_task_id", nullable = false)
    private Task toTask;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false)
    private TaskDependencyType dependencyType;
}
