# Forbidden Practices

## Never Do → Always Do Instead

| Never | Instead |
|-------|---------|
| `FetchType.EAGER` on any relationship | `FetchType.LAZY` — explicit on all relations |
| Return `Entity` directly in API | Wrap in `ApiMessageDTO<YourEntityDTO>` |
| Business logic inside Resource | Move to Service |
| Call Repository from Resource | Only call Service from Resource |
| Object in Form for relationship | Use `UUID parentId` (ID only) |
| Hardcode string in exception | `messageService.getMessage("key")` |
| Missing `@Transactional` on Service method | Required on every public Service method |
| Field injection `@Autowired` | Constructor injection only |
| Skip any of the 8 layers | All 8 files are mandatory |
| `updateFromForm` as mapper method name | `update{Entity}FromForm` (entity-specific name) |
| Abbreviate constructor params (`repo`, `msg`) | Full names: `repository`, `messageService` |
| `@ApiResponse` without `content = @Content(...)` | Include full Swagger content definition |

## Code Examples

### EAGER loading
```java
// WRONG
@ManyToOne(fetch = FetchType.EAGER)
@OneToMany(mappedBy = "parent")                  // default is EAGER — must specify LAZY explicitly

// CORRECT
@ManyToOne(fetch = FetchType.LAZY)
@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
```

### Entity in API response
```java
// WRONG
public ResponseEntity<YourEntity> getById(@PathVariable UUID id) { ... }

// CORRECT
public ResponseEntity<ApiMessageDTO<YourEntityDTO>> getById(@PathVariable UUID id) { ... }
```

### Repository from Resource
```java
// WRONG — in Resource
yourEntityRepository.save(entity);

// CORRECT — delegate to service
yourEntityService.create(form);
```

### Hardcoded string
```java
// WRONG
throw new BWCNotFoundException("Entity not found");

// CORRECT
throw new BWCNotFoundException(messageService.getMessage("entity.not.found", id));
```

### Missing @Transactional
```java
// WRONG
public YourEntityDTO create(YourEntityEditorForm form) { ... }

// CORRECT — write operations
@Transactional(isolation = Isolation.READ_COMMITTED)
public YourEntityDTO create(YourEntityEditorForm form) { ... }

// CORRECT — read operations
@Transactional(readOnly = true)
public List<YourEntityDTO> findAll() { ... }
```

### Mapper update method name conflict
```java
// WRONG — conflicts with IGenericMapper.formToEntity(F, @MappingTarget E)
void updateFromForm(YourEntityEditorForm form, @MappingTarget YourEntity entity, ...);

// CORRECT — entity-specific name
void updateYourEntityFromForm(YourEntityEditorForm form, @MappingTarget YourEntity entity, ...);
```
