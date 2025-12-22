package com.tnh.baseware.core.entities.doc;

import com.tnh.baseware.core.enums.integration.InboundStatus;
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
        name = "inbound_documents",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"source_system", "external_document_id"})
        }
)
public class InboundDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String sourceSystem;    // EOFFICE, iGate,..

    @Column(nullable = false)
    String externalDocumentId;

    @Column(columnDefinition = "jsonb", nullable = false)
    String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    InboundStatus status;

    Instant receivedAt;
    Instant processedAt;

    @Column(columnDefinition = "text")
    String errorMessage;
}

