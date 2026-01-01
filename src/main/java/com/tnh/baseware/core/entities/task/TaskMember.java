package com.tnh.baseware.core.entities.task;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.task.MemberStatus;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(uniqueConstraints = {
                @UniqueConstraint(columnNames = { "task_id", "user_id" })
})
public class TaskMember extends Auditable<String> {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "task_id", nullable = false)
        Task task;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        User user;

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        TaskMemberRole role;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        MemberStatus status;

        Instant joinedAt;
        Instant completedAt;

        // bổ sung cho gantt
        @Builder.Default
        Integer personalProgress = 0;
        Integer weight;
}
