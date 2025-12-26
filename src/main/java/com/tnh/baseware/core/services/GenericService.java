package com.tnh.baseware.core.services;

import com.tnh.baseware.core.dtos.audit.EnumDTO;
import com.tnh.baseware.core.entities.user.CustomUserDetails;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.IGenericRepository;
import com.tnh.baseware.core.specs.GenericSpecification;
import com.tnh.baseware.core.specs.SearchRequest;
import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class GenericService<E, F, D, R extends IGenericRepository<E, I>, M extends IGenericMapper<E, F, D>, I>
        implements IGenericService<E, F, D, I> {

    R repository;
    M mapper;
    MessageService messageService;
    Class<E> entityClass;

    private static final Map<String, Class<?>> ENUM_CACHE = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public D create(F f) {
        var entity = mapper.formToEntity(f);
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public D update(I id, F f) {
        return repository.findById(id)
                .map(existingEntity -> {
                    mapper.formToEntity(f, existingEntity);
                    return mapper.entityToDTO(repository.save(existingEntity));
                })
                .orElseThrow(() -> new BWCNotFoundException(
                        messageService.getMessage("entity.not.found", entityClass.getSimpleName(), id.toString())));
    }

    @Override
    @Transactional
    public void delete(I id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<I> ids) {
        repository.deleteByIdIn(ids);
    }

    @Override
    @Transactional
    public void softDeleteById(I id) {
        repository.softDeleteById(id);
    }

    @Override
    @Transactional
    public void softDeleteAllByIds(List<I> ids) {
        repository.softDeleteByIdIn(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public D findById(I id) {
        return repository.findById(id)
                .map(mapper::entityToDTO)
                .orElseThrow(() -> new BWCNotFoundException(
                        messageService.getMessage("entity.not.found", entityClass.getSimpleName(), id.toString())));
    }

    @Override
    @Transactional(readOnly = true)
    public D safeFindById(I id) {
        return repository.safeFindById(id)
                .map(mapper::entityToDTO)
                .orElseThrow(() -> new BWCNotFoundException(
                        messageService.getMessage("entity.not.found", entityClass.getSimpleName(), id.toString())));
    }

    @Override
    @Transactional(readOnly = true)
    public E getReferenceById(I id) {
        return repository.findReferenceById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> findAll() {
        var sort = getDefaultSort();
        return repository.findAll(sort).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> findAll(Pageable pageable) {
        var sortedPageable = getSortedPageable(pageable);
        return repository.findAll(sortedPageable).map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> findAllActive() {
        return repository.findAllByDeletedFalse().stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> findAllActive(Pageable pageable) {
        var sortedPageable = getSortedPageable(pageable);
        return repository.findAllByDeletedFalse(sortedPageable).map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> findAllByIds(List<I> ids) {
        return repository.findByIdIn(ids).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> findAllByIds(List<I> ids, Pageable pageable) {
        var sortedPageable = getSortedPageable(pageable);
        return repository.findByIdIn(ids, sortedPageable).map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> findAllByIdsAndNotDeleted(List<I> ids) {
        return repository.findByIdInAndDeletedFalse(ids).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> findAllByIdsAndNotDeleted(List<I> ids, Pageable pageable) {
        var sortedPageable = getSortedPageable(pageable);
        return repository.findByIdInAndDeletedFalse(ids, sortedPageable).map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public D findByField(String fieldName, Object value) {
        return repository.findByField(fieldName, value)
                .map(mapper::entityToDTO)
                .orElseThrow(() -> new BWCNotFoundException(
                        messageService.getMessage("entity.not.found", entityClass.getSimpleName(), fieldName, value)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> findAllByField(String fieldName, Object value) {
        return repository.findAllByField(fieldName, value).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> findAllByField(String fieldName, Object value, Pageable pageable) {
        var sortedPageable = getSortedPageable(pageable);
        return repository.findAllByField(fieldName, value, sortedPageable).map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByField(String fieldName, Object value) {
        return repository.existsByField(fieldName, value);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> search(SearchRequest searchRequest) {
        var specification = new GenericSpecification<E>(searchRequest);
        var pageable = GenericSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize());
        return repository.findAll(specification, pageable).map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<? extends EnumDTO<?>> getEnumValues(String enumName) {

        try {
            Class<?> enumClass = ENUM_CACHE.get(enumName);

            if (enumClass == null) {
                String basePackage = "com.tnh.baseware.core.enums";

                ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                        false);

                scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);

                for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                    Class<?> clazz = Class.forName(bd.getBeanClassName());

                    if (clazz.isEnum()
                            && BaseEnum.class.isAssignableFrom(clazz)
                            && clazz.getSimpleName().equals(enumName)) {

                        enumClass = clazz;
                        break;
                    }
                }

                ENUM_CACHE.put(enumName, Objects.requireNonNullElse(enumClass, Void.class));
            }

            if (enumClass == null || enumClass == Void.class) {
                throw new BWCNotFoundException(
                        messageService.getMessage(
                                "enum.not.found",
                                entityClass.getSimpleName(),
                                enumName));
            }

            Object[] constants = enumClass.getEnumConstants();

            return Arrays.stream(constants)
                    .map(e -> {
                        BaseEnum<?> baseEnum = (BaseEnum<?>) e;
                        String displayName = extractDisplayName(e, enumName);
                        return new EnumDTO<>(
                                baseEnum.getValue(),
                                ((Enum<?>) e).name(),
                                displayName);
                    })
                    .toList();

        } catch (Exception e) {
            log.debug("Error retrieving enum values: {}", e.getMessage(), e);
            throw new BWCGenericRuntimeException(
                    messageService.getMessage(
                            "enum.error",
                            entityClass.getSimpleName(),
                            enumName),
                    e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BWCGenericRuntimeException(messageService.getMessage("user.not.authenticated"));
        }
        return userDetails.getUser();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isUserSystem() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BWCGenericRuntimeException(messageService.getMessage("user.not.authenticated"));
        }
        var user = userDetails.getUser();
        if (user.getSuperAdmin())
            return true;
        var orgSys = user.getOrganizations().stream()
                .filter(org -> Boolean.TRUE.equals(org.getOrganization().getIsSystem()))
                .findFirst().orElse(null);

        return orgSys != null;
    }

    private String extractDisplayName(Object enumConstant, String enumName) {
        try {
            var field = enumConstant.getClass().getDeclaredField("displayName");
            field.setAccessible(true);
            return (String) field.get(enumConstant);
        } catch (Exception ex) {
            log.error(LogStyleHelper.error("Missing displayName on enum {}"), enumName);
            return "";
        }
    }

    private Sort getDefaultSort() {
        return Sort.by(Sort.Order.desc("createdDate"));
    }

    private Pageable getSortedPageable(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), getDefaultSort());
    }
}
