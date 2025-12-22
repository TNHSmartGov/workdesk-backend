package com.tnh.baseware.core.repositories;

import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import com.tnh.baseware.core.utils.SpecificationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface IGenericRepository<T, I> extends JpaRepository<T, I>, JpaSpecificationExecutor<T> {

    List<T> findByIdInAndDeletedFalse(List<I> ids);

    Page<T> findByIdInAndDeletedFalse(List<I> ids, Pageable pageable);

    List<T> findAllByDeletedFalse();

    Page<T> findAllByDeletedFalse(Pageable pageable);

    List<T> findByIdIn(List<I> ids);

    Page<T> findByIdIn(List<I> ids, Pageable pageable);

    T findReferenceById(I id);

    void deleteByIdIn(List<I> ids);

    @Transactional(readOnly = true)
    default List<T> findAllParent(String collectionField) {
        return findAll((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get(collectionField)));
    }

    @Transactional(readOnly = true)
    default Page<T> findAllParent(String collectionField, Pageable pageable) {
        return findAll((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get(collectionField)), pageable);
    }

    @Transactional(readOnly = true)
    default List<T> findAllByEntitiesIsEmpty(String collectionField) {
        return findAll((root, query, criteriaBuilder) -> criteriaBuilder.isEmpty(root.get(collectionField)));
    }

    @Transactional(readOnly = true)
    default Page<T> findAllByEntitiesIsEmpty(String collectionField, Pageable pageable) {
        return findAll((root, query, criteriaBuilder) -> criteriaBuilder.isEmpty(root.get(collectionField)), pageable);
    }

    @Transactional(readOnly = true)
    default List<T> findAllByEntitiesNotContaining(String collectionField, Object entity) {
        return findAll(
                (root, query, criteriaBuilder) -> criteriaBuilder.isNotMember(entity, root.get(collectionField)));
    }

    @Transactional(readOnly = true)
    default Page<T> findAllByEntitiesNotContaining(String collectionField, Object entity, Pageable pageable) {
        return findAll((root, query, criteriaBuilder) -> criteriaBuilder.isNotMember(entity, root.get(collectionField)),
                pageable);
    }

    @Transactional(readOnly = true)
    default List<T> findAllByEntitiesContaining(String collectionField, Object entity) {
        return findAll((root, query, criteriaBuilder) -> criteriaBuilder.isMember(entity, root.get(collectionField)));
    }

    @Transactional(readOnly = true)
    default Page<T> findAllByEntitiesContaining(String collectionField, Object entity, Pageable pageable) {
        return findAll((root, query, criteriaBuilder) -> criteriaBuilder.isMember(entity, root.get(collectionField)),
                pageable);
    }

    @Transactional(readOnly = true)
    default Optional<T> safeFindById(I id) {
        return findById(id);
    }

    @Transactional
    default void softDeleteById(I id) {
        findById(id).ifPresent(entity -> {
            try {
                entity.getClass().getMethod("setDeleted", Boolean.class).invoke(entity, Boolean.TRUE);
                save(entity);
            } catch (Exception e) {
                throw new BWCGenericRuntimeException("Entity not support soft delete", e);
            }
        });
    }

    @Transactional
    default void softDeleteByIdIn(List<I> ids) {
        findByIdIn(ids).forEach(entity -> {
            try {
                entity.getClass().getMethod("setDeleted", Boolean.class).invoke(entity, Boolean.TRUE);
            } catch (Exception e) {
                throw new BWCGenericRuntimeException("Entity not support soft delete", e);
            }
        });
        // Use saveAll instead of individual saves for better performance
        saveAll(findByIdIn(ids));
    }

    @Transactional
    default void restoreSoftDeleteById(I id) {
        findById(id).ifPresent(entity -> {
            try {
                entity.getClass().getMethod("setDeleted", Boolean.class).invoke(entity, Boolean.FALSE);
                save(entity);
            } catch (Exception e) {
                throw new BWCGenericRuntimeException("Entity not support soft delete", e);
            }
        });
    }

    @Transactional(readOnly = true)
    default Optional<T> findByField(String fieldName, Object value) {
        Specification<T> specification = createFieldSpecification(fieldName, value);
        return findOne(specification);
    }

    @Transactional(readOnly = true)
    default List<T> findAllByField(String fieldName, Object value) {
        Specification<T> specification = createFieldSpecification(fieldName, value);
        return findAll(specification);
    }

    @Transactional(readOnly = true)
    default Page<T> findAllByField(String fieldName, Object value, Pageable pageable) {
        Specification<T> specification = createFieldSpecification(fieldName, value);
        return findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    default boolean existsByField(String fieldName, Object value) {
        Specification<T> specification = createFieldSpecification(fieldName, value);
        return count(specification) > 0;
    }

    default Specification<T> createFieldSpecification(String fieldName, Object value) {
        return (root, query, criteriaBuilder) -> {
            try {
                var convertedValue = SpecificationUtils.convertValue(root.get(fieldName).getJavaType(), value);
                return criteriaBuilder.equal(root.get(fieldName), convertedValue);
            } catch (Exception e) {
                throw new BWCGenericRuntimeException("Field not found: " + fieldName, e);
            }
        };
    }
}
