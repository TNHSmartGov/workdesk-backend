package com.tnh.baseware.core.resources;

import com.tnh.baseware.core.dtos.audit.EnumDTO;
import com.tnh.baseware.core.dtos.user.ApiMessageDTO;
import com.tnh.baseware.core.specs.SearchRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IGenericResource<E, F, D, I> {

    ResponseEntity<ApiMessageDTO<D>> create(F f);

    ResponseEntity<ApiMessageDTO<D>> update(I id, F f);

    ResponseEntity<ApiMessageDTO<Integer>> deleteById(I id);

    ResponseEntity<ApiMessageDTO<Integer>> deleteAllByIds(List<I> ids);

    ResponseEntity<ApiMessageDTO<Integer>> softDeleteById(I id);

    ResponseEntity<ApiMessageDTO<Integer>> softDeleteAllByIds(List<I> ids);

    ResponseEntity<ApiMessageDTO<D>> findById(I id);

    ResponseEntity<ApiMessageDTO<D>> safeFindById(I id);

    ResponseEntity<ApiMessageDTO<E>> getReferenceById(I id);

    ResponseEntity<ApiMessageDTO<List<D>>> findAll();

    ResponseEntity<ApiMessageDTO<PagedModel<D>>> findAllWithPagination(Pageable pageable,
            PagedResourcesAssembler<D> assembler);

    ResponseEntity<ApiMessageDTO<List<D>>> findAllActive();

    ResponseEntity<ApiMessageDTO<PagedModel<D>>> findAllActiveWithPagination(Pageable pageable,
            PagedResourcesAssembler<D> assembler);

    ResponseEntity<ApiMessageDTO<List<D>>> findAllByIds(List<I> ids);

    ResponseEntity<ApiMessageDTO<List<D>>> findAllByIdsAndNotDeleted(List<I> ids);

    ResponseEntity<ApiMessageDTO<PagedModel<D>>> findAllByIdsAndNotDeletedWithPagination(List<I> ids, Pageable pageable,
            PagedResourcesAssembler<D> assembler);

    ResponseEntity<ApiMessageDTO<List<D>>> findAllByField(String fieldName, String value);

    ResponseEntity<ApiMessageDTO<PagedModel<D>>> findAllByFieldWithPagination(String fieldName, String value,
            Pageable pageable, PagedResourcesAssembler<D> assembler);

    ResponseEntity<ApiMessageDTO<Boolean>> existsByField(String fieldName, String value);

    ResponseEntity<ApiMessageDTO<PagedModel<D>>> search(SearchRequest searchRequest,
            PagedResourcesAssembler<D> assembler);

    ResponseEntity<ApiMessageDTO<List<? extends EnumDTO<?>>>> getEnumValues(String enumName);
}