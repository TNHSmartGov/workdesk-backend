package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskDocument;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ITaskDocumentRepository extends IGenericRepository<TaskDocument, UUID> {
    List<TaskDocument> findAllByTask_Id(UUID taskId);

    List<TaskDocument> findAllByDocument_Id(UUID documentId);
}
