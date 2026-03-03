package com.tnh.baseware.core.entities.project;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.user.User;
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
                @UniqueConstraint(columnNames = { "project_id", "user_id" })
})
public class ProjectMember extends Auditable<String> {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "project_id", nullable = false)
        Project project;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        User user;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "project_role_id", nullable = false)
        ProjectRole projectRole;

        @Column(nullable = false, updatable = false)
        Instant joinedAt;

        Boolean isOwner;

        @PrePersist
        protected void onJoin() {
                if (this.joinedAt == null) {
                        this.joinedAt = Instant.now();
                }
        }

}
