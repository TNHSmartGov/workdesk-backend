# Layer 7 — Service Implementation

**Location**: `src/main/java/com/tnh/baseware/core/services/{domain}/imp/{Entity}Service.java`

## Template

```java
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class YourEntityService extends
        GenericService<YourEntity, YourEntityEditorForm, YourEntityDTO,
                       IYourEntityRepository, IYourEntityMapper, UUID>
        implements IYourEntityService {

    // GenericService already provides (PROTECTED — directly accessible):
    //   repository, mapper, messageService, entityClass

    // Additional dependencies declared here
    GenericEntityFetcher fetcher;
    IRelatedEntityRepository relatedEntityRepository;

    // CORRECT: use "repository" and "messageService" (not "repo", "msg")
    public YourEntityService(IYourEntityRepository repository,
                             IYourEntityMapper mapper,
                             MessageService messageService,
                             GenericEntityFetcher fetcher,
                             IRelatedEntityRepository relatedEntityRepository) {
        super(repository, mapper, messageService, YourEntity.class);
        this.fetcher = fetcher;
        this.relatedEntityRepository = relatedEntityRepository;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public YourEntityDTO create(YourEntityEditorForm form) {
        var entity = mapper.formToEntity(form, fetcher, relatedEntityRepository);
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public YourEntityDTO update(UUID id, YourEntityEditorForm form) {
        var entity = repository.findById(id).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("entity.not.found", id)));
        // CORRECT: call update{Entity}FromForm — the entity-specific mapper method name
        mapper.updateYourEntityFromForm(form, entity, fetcher, relatedEntityRepository);
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<YourEntityDTO> findByParent(UUID parentId) {
        return repository.findAllByField("parent.id", parentId).stream()
                .map(mapper::entityToDTO)
                .toList();
    }
}
```

## Rules

| Rule | Detail |
|------|--------|
| `extends GenericService<E, F, D, R, M, I>` | Required — provides base CRUD |
| `implements IYourEntityService` | Required |
| `@FieldDefaults(makeFinal = true, PRIVATE)` | Additional deps must be final private |
| Constructor injection only | No `@Autowired` field injection |
| Param names `repository`, `messageService` | No abbreviations (`repo`, `msg`) |
| `@Transactional` on EVERY public method | Write: `READ_COMMITTED` / Read: `readOnly = true` |
| `BWCNotFoundException` for not-found | Always with `messageService.getMessage(...)` |
| Call `mapper.update{Entity}FromForm(...)` | Must match the exact method name in mapper |

## GenericService PROTECTED Fields

`GenericService` declares its fields as `PROTECTED`, so they are accessible directly in subclasses:

```java
// In GenericService:
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
R repository;       // ← use directly as `repository`
M mapper;           // ← use directly as `mapper`
MessageService messageService;   // ← use directly as `messageService`
Class<E> entityClass;
```

Do NOT re-declare these in the subclass. Do NOT call getters. Use directly.

## @Transactional Reference

| Operation type | Annotation |
|---------------|-----------|
| Create, Update, Delete, Assign | `@Transactional(isolation = Isolation.READ_COMMITTED)` |
| Find, List, Search, Count | `@Transactional(readOnly = true)` |

## Reference

`src/main/java/com/tnh/baseware/core/services/user/imp/MenuService.java`
