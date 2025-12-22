package com.tnh.baseware.core.repositories.doc;

import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IFileDocumentRepository extends IGenericRepository<FileDocument, UUID> {

}
