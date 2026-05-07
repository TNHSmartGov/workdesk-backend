# Pre-Commit Checklist

Run through this before submitting any new API code.

## Structure
- [ ] All 8 files exist in correct locations
- [ ] Naming follows conventions (see `conventions.md`)

## Layer 1 — Entity
- [ ] `extends Auditable<String> implements Serializable`
- [ ] `@Serial private static final long serialVersionUID = 1L`
- [ ] `@FieldDefaults(level = AccessLevel.PRIVATE)`
- [ ] All relationships use `FetchType.LAZY` (explicit — including `@OneToMany`)
- [ ] `@JsonIgnore` on all bidirectional sides
- [ ] `@Builder.Default` on all collections

## Layer 2 — DTO
- [ ] `extends RepresentationModel<YourEntityDTO>`
- [ ] `implements Identifiable<UUID>`
- [ ] `@JsonInclude(JsonInclude.Include.NON_NULL)`
- [ ] `@EqualsAndHashCode(callSuper = true)`
- [ ] No Entity objects (nested DTOs only)

## Layer 3 — Form
- [ ] `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)`
- [ ] Validation annotations on all required fields
- [ ] Message keys (i18n) in all validation messages: `{key.name}`
- [ ] `@Schema` description on relationship ID fields
- [ ] Only UUID IDs for relationships (no Entity objects)

## Layer 4 — Repository
- [ ] `extends IGenericRepository<YourEntity, UUID>`
- [ ] Interface name prefixed with `I`
- [ ] `@EntityGraph` on custom queries that load relations

## Layer 5 — Mapper
- [ ] `extends IGenericMapper<YourEntity, YourEntityEditorForm, YourEntityDTO>`
- [ ] `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`
- [ ] Update method named `update{Entity}FromForm` (NOT `updateFromForm`)
- [ ] `@Context` for injected repositories and fetcher
- [ ] `@MappingTarget` on update method parameter

## Layer 6 — Service Interface
- [ ] `extends IGenericService<YourEntity, YourEntityEditorForm, YourEntityDTO, UUID>`
- [ ] Interface name prefixed with `I`
- [ ] `@Override` declared for any generic methods being customized

## Layer 7 — Service Implementation
- [ ] `extends GenericService<YourEntity, YourEntityEditorForm, YourEntityDTO, IYourEntityRepository, IYourEntityMapper, UUID>`
- [ ] `implements IYourEntityService`
- [ ] `@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)`
- [ ] Constructor injection only (no `@Autowired` field injection)
- [ ] Constructor params use `repository` and `messageService` (not `repo`/`msg`)
- [ ] `@Transactional` on EVERY public method
  - Write: `@Transactional(isolation = Isolation.READ_COMMITTED)`
  - Read: `@Transactional(readOnly = true)`
- [ ] `BWCNotFoundException` + `messageService.getMessage(...)` for errors
- [ ] Calls `mapper.update{Entity}FromForm(...)` (correct custom method name)

## Layer 8 — Resource
- [ ] `extends GenericResource<YourEntity, YourEntityEditorForm, YourEntityDTO, UUID>`
- [ ] `@Tag(name = "...", description = "...")`
- [ ] `@RequestMapping` uses `${baseware.core.system.api-prefix}/your-entities`
- [ ] All responses wrapped in `ApiMessageDTO<T>`
- [ ] Every custom endpoint has `@Operation` + `@ApiResponse` with full `content = @Content(...)`
- [ ] No business logic in Resource
- [ ] No direct Repository access in Resource
