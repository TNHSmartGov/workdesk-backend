package com.tnh.baseware.core.entities.task;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.doc.Document;
import com.tnh.baseware.core.enums.task.TaskDocumentRelationType;
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
        name = "tasks_documents",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"task_id", "document_id"})
        }
)
public class TaskDocument extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id")
    Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id")
    Document document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskDocumentRelationType relationType;
}

