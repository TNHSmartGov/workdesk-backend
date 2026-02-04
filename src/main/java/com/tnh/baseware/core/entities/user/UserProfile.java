package com.tnh.baseware.core.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tnh.baseware.core.entities.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile extends Auditable<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    @JsonIgnore
    User user;

    String coverUrl;

    Instant joinDate;

    // --- Statistics (Cached) ---

    @Builder.Default
    Integer totalTasks = 0;

    @Builder.Default
    Integer participatedTasks = 0;

    @Builder.Default
    Integer completedTasks = 0;

    @Builder.Default
    Integer completedProjects = 0;

    @Builder.Default
    Double performance = 0.0;

    Instant lastStatsUpdate;
}
