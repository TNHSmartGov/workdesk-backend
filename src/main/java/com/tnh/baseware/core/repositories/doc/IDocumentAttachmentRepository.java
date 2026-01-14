package com.tnh.baseware.core.repositories.doc;

import com.tnh.baseware.core.entities.doc.DocumentAttachment;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IDocumentAttachmentRepository extends IGenericRepository<DocumentAttachment, UUID> {
    List<DocumentAttachment> findAllByDocument_Id(UUID documentId);
}
