package com.tnh.baseware.core.entities.user;

import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.audit.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@Entity
@Table(
        name = "users_organizations",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "organization_id"}
        )
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserOrganization extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    Category title;

    @ManyToOne(fetch = FetchType.LAZY)
    Category position;

    @Builder.Default
    Boolean active = true;
}

