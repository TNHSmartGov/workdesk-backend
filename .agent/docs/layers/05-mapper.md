# Layer 5 — Mapper

**Location**: `src/main/java/com/tnh/baseware/core/mappers/{domain}/I{Entity}Mapper.java`

## Template

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IYourEntityMapper extends IGenericMapper<YourEntity, YourEntityEditorForm, YourEntityDTO> {

    // CREATE: overload formToEntity with @Context for relationship resolution
    @Mapping(target = "relatedEntity", expression = "java(fetcher.formToEntity(repository, form.getRelatedEntityId()))")
    YourEntity formToEntity(YourEntityEditorForm form,
                            @Context GenericEntityFetcher fetcher,
                            @Context IRelatedEntityRepository repository);

    // UPDATE: name MUST be update{Entity}FromForm — entity-specific name
    // NEVER use "updateFromForm" — conflicts with IGenericMapper.formToEntity(F, @MappingTarget E)
    @Mapping(target = "relatedEntity", expression = "java(fetcher.formToEntity(repository, form.getRelatedEntityId()))")
    void updateYourEntityFromForm(YourEntityEditorForm form,
                                  @MappingTarget YourEntity entity,
                                  @Context GenericEntityFetcher fetcher,
                                  @Context IRelatedEntityRepository repository);

    // Entity → DTO (override only if custom mapping needed)
    YourEntityDTO entityToDTO(YourEntity entity);
}
```

## CRITICAL: Update Method Naming

The update method must always be named `update{Entity}FromForm`:

| Entity | Correct update method name |
|--------|---------------------------|
| `Menu` | `updateMenuFromForm` |
| `TaskCategory` | `updateTaskCategoryFromForm` |
| `Role` | `updateRoleFromForm` |

**Why**: `IGenericMapper` already defines `void formToEntity(F form, @MappingTarget E entity)`.
Naming the update method `updateFromForm` would be a new method name but is confusing — use entity-specific names consistently. The Service Impl must call this same custom name.

## Rules

| Rule | Detail |
|------|--------|
| `extends IGenericMapper<E, F, D>` | Required base interface |
| `@Mapper(componentModel = "spring")` | Required for Spring DI injection |
| `unmappedTargetPolicy = ReportingPolicy.IGNORE` | Suppress warnings for unmapped fields |
| `@Context` for dependencies | Pass `GenericEntityFetcher` and repositories via `@Context` |
| `@MappingTarget` on update | Marks the existing entity to update in-place |
| `@Named` for custom methods | Use when `entityToDTO` needs special logic (e.g., parent mapping) |

## Tree Structure Pattern (when entity has parent-child)

```java
default List<YourEntityDTO> mapToTree(List<YourEntity> entities) {
    if (entities == null || entities.isEmpty()) return List.of();

    var parentMap = entities.stream()
            .filter(e -> e.getParent() != null)
            .collect(Collectors.groupingBy(e -> e.getParent().getId()));

    return entities.stream()
            .filter(e -> e.getParent() == null)
            .map(e -> buildTree(e, parentMap))
            .toList();
}

default YourEntityDTO buildTree(YourEntity e, Map<UUID, List<YourEntity>> parentMap) {
    var dto = entityToDTO(e);
    var children = parentMap.getOrDefault(e.getId(), List.of());
    if (!children.isEmpty()) {
        dto.setChildren(children.stream().map(c -> buildTree(c, parentMap)).toList());
    }
    return dto;
}
```

## Reference

`src/main/java/com/tnh/baseware/core/mappers/user/IMenuMapper.java`
