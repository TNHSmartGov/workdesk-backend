package com.tnh.baseware.core.entities.task;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.task.LogActionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_activity_logs")
public class TaskActivityLog extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    LogActionType actionType;

    String targetField;

    @Column(columnDefinition = "text")
    String oldValue;

    @Column(columnDefinition = "text")
    String newValue;
}

