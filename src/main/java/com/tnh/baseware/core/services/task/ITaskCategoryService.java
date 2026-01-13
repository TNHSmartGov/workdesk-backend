package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskCategoryDTO;
import com.tnh.baseware.core.entities.task.TaskCategory;
import com.tnh.baseware.core.forms.task.TaskCategoryEditorForm;
import com.tnh.baseware.core.services.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ITaskCategoryService
        extends IGenericService<TaskCategory, TaskCategoryEditorForm, TaskCategoryDTO, UUID> {

    @Override
    List<TaskCategoryDTO> findAll();

    @Override
    Page<TaskCategoryDTO> findAll(Pageable pageable);
}
