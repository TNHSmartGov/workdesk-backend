package com.tnh.baseware.core.services.audit;

import com.tnh.baseware.core.dtos.audit.CategoryDTO;
import com.tnh.baseware.core.entities.audit.Category;
import com.tnh.baseware.core.forms.audit.CategoryEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ICategoryService extends IGenericService<Category, CategoryEditorForm, CategoryDTO, UUID> {

}
