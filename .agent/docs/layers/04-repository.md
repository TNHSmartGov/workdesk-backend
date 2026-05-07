# Layer 4 — Repository

**Location**: `src/main/java/com/tnh/baseware/core/repositories/{domain}/I{Entity}Repository.java`

## Template

```java
@Repository
public interface IYourEntityRepository extends IGenericRepository<YourEntity, UUID> {

    // Custom queries — use @EntityGraph to prevent N+1
    @EntityGraph(attributePaths = {"parent"})
    @Query("SELECT e FROM YourEntity e")
    List<YourEntity> findAllWithParent();

    // Derived query example
    Optional<YourEntity> findByCode(String code);
}
```

## Rules

| Rule | Detail |
|------|--------|
| `extends IGenericRepository<YourEntity, UUID>` | Required — provides all base CRUD + utility methods |
| Interface prefix `I` | Always: `IYourEntityRepository` |
| `@EntityGraph` on relation queries | Prevents N+1 query problem |
| JPQL preferred | Use `@Query` with JPQL; native SQL only when unavoidable |

## Methods Already Provided by IGenericRepository

No need to declare these:

```
findById(UUID)
findAll()
findAll(Pageable)
save(E)
saveAll(List<E>)
delete(E)
deleteById(UUID)
findAllById(List<UUID>)
findAllByDeletedFalse()
findByField(String, Object)
findAllByField(String, Object)
findAllParent(String collectionField)
findAllParent(String collectionField, Pageable)
findAllByEntitiesContaining(String, E)
findAllByEntitiesContaining(String, E, Pageable)
findAllByEntitiesNotContaining(String, E)
findAllByEntitiesIsEmpty(String)
```

## Reference

`src/main/java/com/tnh/baseware/core/repositories/user/IMenuRepository.java`
