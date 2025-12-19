package com.tnh.baseware.core.repositories.audit;

import com.tnh.baseware.core.entities.audit.Category;
import com.tnh.baseware.core.enums.CategoryCode;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICategoryRepository extends IGenericRepository<Category, UUID> {
    Optional<Category> findByCodeAndName(CategoryCode code, String name);
}
