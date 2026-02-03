package com.tnh.baseware.core.entities.task;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.project.Sprint;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Persistable;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskAgileInfo extends Auditable<String> implements Persistable<UUID>, Serializable {

    @Id
    UUID id;

    @Transient
    @Builder.Default
    boolean isNewEntity = true;

    @Override
    public boolean isNew() {
        return isNewEntity;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNewEntity = false;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    Sprint sprint;

    Integer storyPoints;

    Double originalEstimate; // hours
    Double remainingEstimate; // hours
    Double completedHours; // hours

    @Column(name = "board_position")
    Double boardPosition;

    // Kanban fields could go here (e.g., Swimlane ID)
}
