package com.tnh.baseware.core.entities.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tnh.baseware.core.entities.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskCategory extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String code;

    @Builder.Default
    Integer orderIndex = 0;

    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    TaskCategory parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<TaskCategory> children = new HashSet<>();

}
