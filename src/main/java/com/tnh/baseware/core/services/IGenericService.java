package com.tnh.baseware.core.services;

import com.tnh.baseware.core.dtos.audit.EnumDTO;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.specs.SearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IGenericService<E, F, D, I> {

    D create(F f);

    D update(I id, F f);

    void delete(I id);

    void deleteAllByIds(List<I> ids);

    void softDeleteById(I id);

    void softDeleteAllByIds(List<I> ids);

    D findById(I id);

    D safeFindById(I id);

    E getReferenceById(I id);

    List<D> findAll();

    Page<D> findAll(Pageable pageable);

    List<D> findAllActive();

    Page<D> findAllActive(Pageable pageable);

    List<D> findAllByIds(List<I> ids);

    Page<D> findAllByIds(List<I> ids, Pageable pageable);

    List<D> findAllByIdsAndNotDeleted(List<I> ids);

    Page<D> findAllByIdsAndNotDeleted(List<I> ids, Pageable pageable);

    D findByField(String fieldName, Object value);

    List<D> findAllByField(String fieldName, Object value);

    Page<D> findAllByField(String fieldName, Object value, Pageable pageable);

    boolean existsByField(String fieldName, Object value);

    Page<D> search(SearchRequest searchRequest);

    List<? extends EnumDTO<?>> getEnumValues(String enumName);

    User getCurrentUser();

    Boolean isUserSystem();
}
