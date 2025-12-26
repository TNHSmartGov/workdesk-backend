package com.tnh.baseware.core.services.audit.imp;

import com.tnh.baseware.core.dtos.audit.CategoryDTO;
import com.tnh.baseware.core.entities.audit.Category;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.forms.audit.CategoryEditorForm;
import com.tnh.baseware.core.mappers.audit.ICategoryMapper;
import com.tnh.baseware.core.repositories.audit.ICategoryRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.audit.ICategoryService;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryService extends
        GenericService<Category, CategoryEditorForm, CategoryDTO, ICategoryRepository, ICategoryMapper, UUID>
        implements
        ICategoryService {

    public CategoryService(ICategoryRepository repository,
            ICategoryMapper mapper,
            MessageService messageService) {
        super(repository, mapper, messageService, Category.class);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new BWCBusinessException(messageService.getMessage("category.not.found", id)));
        if (Boolean.TRUE.equals(category.getIsSystem())) {
            throw new UnsupportedOperationException(
                    messageService.getMessage("category.delete.system.category.not.allowed", id));
        }
        super.delete(id);
    }
}
