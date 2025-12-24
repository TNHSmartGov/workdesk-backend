package com.tnh.baseware.core.entities.project;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.entities.user.User;
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
        name = "project_attachments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"project_id", "file_id"})
        }
)
public class ProjectAttachment extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    User uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    FileDocument file;

    String description;
}
