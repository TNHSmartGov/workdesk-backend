package com.tnh.baseware.core.entities.doc;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.enums.doc.DocumentType;
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
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"external_source", "external_id"})
        }
)
public class Document extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String documentNumber;   // số văn bản

    @Column(columnDefinition = "text")
    String summary;          // trích yếu

    Instant issuedDate; // ngày ban hành

    @Column(nullable = false)
    String issuedBy;    // cơ quan ban hành

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DocumentType documentType;

    // Integration
    String externalSource;   // EOFFICE, iGate,..
    String externalId;       // id bên eOffice
}

