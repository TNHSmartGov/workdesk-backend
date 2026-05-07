package com.tnh.baseware.core.entities.project;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.enums.project.ProjectStatus;
import com.tnh.baseware.core.enums.project.ProjectType;
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
                @UniqueConstraint(columnNames = "code")
})
public class Project extends Auditable<String> {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        UUID id;

        @Column(nullable = false, length = 50)
        String code;

        @Column(nullable = false)
        String name;

        @Column(columnDefinition = "text")
        String description;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "organization_id")
        Organization organization;

        Instant startDate;
        Instant endDate;

        Instant archivedAt;

        @Enumerated(EnumType.STRING)
        @Column(nullable = true)
        ProjectType type;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        ProjectStatus status;
}
