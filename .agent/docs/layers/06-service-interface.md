# Layer 6 — Service Interface

**Location**: `src/main/java/com/tnh/baseware/core/services/{domain}/I{Entity}Service.java`

## Template

```java
public interface IYourEntityService extends IGenericService<YourEntity, YourEntityEditorForm, YourEntityDTO, UUID> {

    // Declare @Override only when Service Impl customizes a generic method
    @Override
    YourEntityDTO create(YourEntityEditorForm form);

    @Override
    YourEntityDTO update(UUID id, YourEntityEditorForm form);

    // Custom business methods — declare signature only, no implementation
    List<YourEntityDTO> findByParent(UUID parentId);

    void assignRoles(UUID id, List<UUID> roleIds);

    void removeRoles(UUID id, List<UUID> roleIds);
}
```

## Rules

| Rule | Detail |
|------|--------|
| `extends IGenericService<E, F, D, UUID>` | Required |
| Interface prefix `I` | Always: `IYourEntityService` |
| Signatures only | No implementation in interface |
| `@Override` for customized generics | When Service Impl overrides a generic method, declare it here too |

## Methods Already Provided by IGenericService

No need to declare these unless customizing:

```
create(F form)
update(I id, F form)
delete(I id)
softDeleteById(I id)
findById(I id)
findAll()
findAll(Pageable)
findAllActive()
findByField(String, Object)
search(SearchRequest)
getEnumValues(String)
```

## Reference

`src/main/java/com/tnh/baseware/core/services/user/IMenuService.java`
