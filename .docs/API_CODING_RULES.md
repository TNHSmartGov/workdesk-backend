# ⚖️ API Development Rules - MANDATORY

> **Status**: 🔴 **STRICT ENFORCEMENT**  
> **Applies to**: All developers and AI agents  
> **Last Updated**: 2026-01-13

---

## 🎯 PURPOSE

This document defines **MANDATORY rules** that ALL developers and AI agents MUST follow when developing APIs in this project. These rules ensure consistency, maintainability, and quality.

**Reference Architecture**: See `.docs/API_ARCHITECTURE_GUIDE.md` for detailed explanations and examples.

---

## ✅ MANDATORY CHECKLIST FOR NEW API

When creating a new entity/API, you MUST create ALL 8 components in this exact order:

```
1. Entity (entities/)
2. DTO (dtos/)  
3. Form (forms/)
4. Repository (repositories/)
5. Mapper (mappers/)
6. Service Interface (services/)
7. Service Implementation (services/imp/)
8. Resource/Controller (resources/)
```

### 🔴 RULE: NO SHORTCUTS
- ALL 8 files must exist
- ALL must follow the exact pattern
- NO exceptions without project lead approval

---

## 📐 COMPONENT TEMPLATES

### 1️⃣ ENTITY - Database Model

**Location**: `src/main/java/com/tnh/baseware/core/entities/{domain}/{EntityName}.java`

**Required Pattern**:
```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table  // Optional: specify table name
@FieldDefaults(level = AccessLevel.PRIVATE)
public class YourEntity extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)  // Add constraints
    String name;
    
    // Relationships MUST use LAZY loading
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    YourEntity parent;
    
    // Collections MUST have @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    Set<YourEntity> children = new HashSet<>();
}
```

**🔴 RULES**:
- ✅ MUST extend `Auditable<String>`
- ✅ MUST use Lombok annotations
- ✅ MUST use `@FieldDefaults(level = AccessLevel.PRIVATE)`
- ✅ MUST use `FetchType.LAZY` for ALL relationships
- ✅ MUST use `@Builder.Default` for collections
- ✅ MUST use `@JsonIgnore` on bidirectional relationships
- ❌ NEVER use `FetchType.EAGER`
- ❌ NEVER expose entity directly in API response

---

### 2️⃣ DTO - Data Transfer Object

**Location**: `src/main/java/com/tnh/baseware/core/dtos/{domain}/{EntityName}DTO.java`

**Required Pattern**:
```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YourEntityDTO extends RepresentationModel<YourEntityDTO> 
        implements Identifiable<UUID> {

    UUID id;
    String name;
    
    // Nested DTOs, NOT entities
    YourEntityDTO parent;
    List<YourEntityDTO> children;
}
```

**🔴 RULES**:
- ✅ MUST extend `RepresentationModel<YourEntityDTO>` (HATEOAS)
- ✅ MUST implement `Identifiable<UUID>`
- ✅ MUST use `@JsonInclude(JsonInclude.Include.NON_NULL)`
- ✅ MUST use nested DTOs for relationships
- ❌ NEVER include Entity objects
- ❌ NEVER expose sensitive data

---

### 3️⃣ FORM - Request/Input Object

**Location**: `src/main/java/com/tnh/baseware/core/forms/{domain}/{EntityName}EditorForm.java`

**Required Pattern**:
```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class YourEntityEditorForm {

    @NotBlank(message = "{name.not.blank}")
    String name;

    @NotNull(message = "{parent.id.not.null}")
    @Schema(description = "ID of parent entity")
    UUID parentId;  // Use ID, not object!
    
    @Min(0)
    @Max(100)
    Integer orderIndex;
    
    String description;  // Optional fields
}
```

**🔴 RULES**:
- ✅ MUST use validation annotations (`@NotBlank`, `@NotNull`, `@Min`, `@Max`, etc.)
- ✅ MUST use `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)`
- ✅ MUST use `@Schema` for Swagger documentation
- ✅ MUST use message keys (e.g., `{name.not.blank}`) for i18n
- ✅ MUST use **IDs** for relationships (e.g., `parentId`), NOT objects
- ❌ NEVER use Entity objects in Form

---

### 4️⃣ REPOSITORY - Data Access Layer

**Location**: `src/main/java/com/tnh/baseware/core/repositories/{domain}/I{EntityName}Repository.java`

**Required Pattern**:
```java
@Repository
public interface IYourEntityRepository extends IGenericRepository<YourEntity, UUID> {
    
    // Add custom queries if needed
    @EntityGraph(attributePaths = {"parent"})
    @Query("SELECT e FROM YourEntity e WHERE e.deleted = false")
    List<YourEntity> findAllWithParent();
    
    // Dynamic field queries (provided by IGenericRepository)
    // findByField(String fieldName, Object value)
    // findAllByField(String fieldName, Object value)
}
```

**🔴 RULES**:
- ✅ MUST extend `IGenericRepository<YourEntity, UUID>`
- ✅ MUST use `@EntityGraph` to prevent N+1 queries
- ✅ MUST prefix interface with `I` (e.g., `IYourEntityRepository`)
- ✅ MUST use JPQL for queries
- ❌ AVOID native SQL unless absolutely necessary

**Available Generic Methods** (no need to declare):
- `findById(UUID id)`
- `findAll()`, `findAll(Pageable pageable)`
- `save(Entity)`, `saveAll(List<Entity>)`
- `delete(Entity)`, `deleteById(UUID)`
- `findByField(String, Object)`
- `findAllByField(String, Object)`
- `findAllParent(String collectionField)`
- And many more...

---

### 5️⃣ MAPPER - Object Converter

**Location**: `src/main/java/com/tnh/baseware/core/mappers/{domain}/I{EntityName}Mapper.java`

**Required Pattern**:
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IYourEntityMapper extends IGenericMapper<YourEntity, YourEntityEditorForm, YourEntityDTO> {

    // Form → Entity (for CREATE)
    @Mapping(target = "parent", expression = "java(fetcher.formToEntity(repository, form.getParentId()))")
    YourEntity formToEntity(YourEntityEditorForm form,
                           @Context GenericEntityFetcher fetcher,
                           @Context IYourEntityRepository repository);

    // Form → Entity (for UPDATE)
    @Mapping(target = "parent", expression = "java(fetcher.formToEntity(repository, form.getParentId()))")
    void updateFromForm(YourEntityEditorForm form,
                       @MappingTarget YourEntity entity,
                       @Context GenericEntityFetcher fetcher,
                       @Context IYourEntityRepository repository);

    // Entity → DTO
    YourEntityDTO entityToDTO(YourEntity entity);
    
    // List conversion
    List<YourEntityDTO> entitiesToDTOs(List<YourEntity> entities);
}
```

**🔴 RULES**:
- ✅ MUST extend `IGenericMapper<Entity, Form, DTO>`
- ✅ MUST use `@Mapper(componentModel = "spring")`
- ✅ MUST implement 3 core methods: `formToEntity()`, `updateFromForm()`, `entityToDTO()`
- ✅ MUST use `GenericEntityFetcher` to resolve IDs to entities
- ✅ MUST use `@Context` for injecting repositories
- ✅ MUST use `@MappingTarget` for update operations

---

### 6️⃣ SERVICE INTERFACE

**Location**: `src/main/java/com/tnh/baseware/core/services/{domain}/I{EntityName}Service.java`

**Required Pattern**:
```java
public interface IYourEntityService extends IGenericService<YourEntity, YourEntityEditorForm, YourEntityDTO, UUID> {

    // Override generic methods if needed
    @Override
    YourEntityDTO create(YourEntityEditorForm form);

    @Override
    YourEntityDTO update(UUID id, YourEntityEditorForm form);

    // Custom business methods
    List<YourEntityDTO> findByParent(UUID parentId);
    void assignToParent(UUID entityId, UUID parentId);
}
```

**🔴 RULES**:
- ✅ MUST extend `IGenericService<Entity, Form, DTO, UUID>`
- ✅ MUST prefix with `I`
- ✅ ONLY declare method signatures (no implementation)

---

### 7️⃣ SERVICE IMPLEMENTATION

**Location**: `src/main/java/com/tnh/baseware/core/services/{domain}/imp/{EntityName}Service.java`

**Required Pattern**:
```java
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class YourEntityService extends 
        GenericService<YourEntity, YourEntityEditorForm, YourEntityDTO, 
                      IYourEntityRepository, IYourEntityMapper, UUID>
        implements IYourEntityService {

    // Additional dependencies
    GenericEntityFetcher fetcher;
    
    public YourEntityService(IYourEntityRepository repository,
                            IYourEntityMapper mapper,
                            MessageService messageService,
                            GenericEntityFetcher fetcher) {
        super(repository, mapper, messageService, YourEntity.class);
        this.fetcher = fetcher;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public YourEntityDTO create(YourEntityEditorForm form) {
        var entity = mapper.formToEntity(form, fetcher, repository);
        var saved = repository.save(entity);
        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public YourEntityDTO update(UUID id, YourEntityEditorForm form) {
        var entity = repository.findById(id).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("entity.not.found", id)));
        mapper.updateFromForm(form, entity, fetcher, repository);
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

**🔴 RULES**:
- ✅ MUST extend `GenericService<...>`
- ✅ MUST use `@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)`
- ✅ MUST use **Constructor Injection** (NOT field injection)
- ✅ MUST add `@Transactional` on ALL public methods
  - Write operations: `@Transactional(isolation = Isolation.READ_COMMITTED)`
  - Read operations: `@Transactional(readOnly = true)`
- ✅ MUST throw `BWCNotFoundException` for not found
- ✅ MUST use `MessageService` for all messages
- ❌ NEVER put business logic in Resource/Controller
- ❌ NEVER access Repository from Resource directly

---

### 8️⃣ RESOURCE - REST API Controller

**Location**: `src/main/java/com/tnh/baseware/core/resources/{domain}/{EntityName}Resource.java`

**Required Pattern**:
```java
@Tag(name = "YourEntities", description = "API for managing your entities")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/your-entities")
public class YourEntityResource extends GenericResource<YourEntity, YourEntityEditorForm, YourEntityDTO, UUID> {

    IYourEntityService yourEntityService;

    public YourEntityResource(IGenericService<YourEntity, YourEntityEditorForm, YourEntityDTO, UUID> service,
                             MessageService messageService,
                             IYourEntityService yourEntityService,
                             SystemProperties systemProperties) {
        super(service, messageService, systemProperties.getApiPrefix() + "/your-entities");
        this.yourEntityService = yourEntityService;
    }

    // Custom endpoint
    @Operation(summary = "Find entities by parent")
    @ApiResponse(responseCode = "200", description = "Entities retrieved successfully")
    @GetMapping("/by-parent/{parentId}")
    public ResponseEntity<ApiMessageDTO<List<YourEntityDTO>>> findByParent(
            @PathVariable UUID parentId) {
        var entities = yourEntityService.findByParent(parentId);
        return ResponseEntity.ok(ApiMessageDTO.<List<YourEntityDTO>>builder()
                .data(entities)
                .result(true)
                .message(messageService.getMessage("entities.found"))
                .code(HttpStatus.OK.value())
                .build());
    }
}
```

**🔴 RULES**:
- ✅ MUST extend `GenericResource<Entity, Form, DTO, UUID>`
- ✅ MUST use `@RestController`
- ✅ MUST use `${baseware.core.system.api-prefix}` in path
- ✅ MUST use kebab-case, plural for base path (e.g., `/your-entities`)
- ✅ MUST wrap ALL responses in `ApiMessageDTO`
- ✅ MUST add Swagger annotations (`@Tag`, `@Operation`, `@ApiResponse`)
- ✅ MUST use proper HTTP status codes
- ❌ NEVER put business logic in Resource
- ❌ NEVER access Repository directly

**Generic Endpoints Available** (auto-inherited):
- `POST /your-entities` - Create
- `PUT /your-entities/{id}` - Update
- `GET /your-entities/{id}` - Find by ID
- `GET /your-entities` - Find all
- `GET /your-entities/pagination` - Find with pagination
- `DELETE /your-entities/{id}` - Delete
- `POST /your-entities/search` - Advanced search
- And more...

---

## 🚫 FORBIDDEN PRACTICES

### ❌ NEVER DO:

1. **Expose Entity in API**
   ```java
   // ❌ WRONG
   public ResponseEntity<YourEntity> getEntity() { ... }
   
   // ✅ CORRECT
   public ResponseEntity<ApiMessageDTO<YourEntityDTO>> getEntity() { ... }
   ```

2. **Use EAGER Loading**
   ```java
   // ❌ WRONG
   @ManyToOne(fetch = FetchType.EAGER)
   
   // ✅ CORRECT
   @ManyToOne(fetch = FetchType.LAZY)
   ```

3. **Business Logic in Controller**
   ```java
   // ❌ WRONG - in Resource
   public ResponseEntity<...> create() {
       var entity = new Entity();
       entity.setName("...");
       repository.save(entity);  // Direct DB access!
   }
   
   // ✅ CORRECT - in Resource
   public ResponseEntity<...> create(Form form) {
       var dto = service.create(form);  // Delegate to service
       return ResponseEntity.ok(...);
   }
   ```

4. **Skip Validation**
   ```java
   // ❌ WRONG
   public class Form {
       String name;  // No validation!
   }
   
   // ✅ CORRECT
   public class Form {
       @NotBlank(message = "{name.not.blank}")
       String name;
   }
   ```

5. **Hardcode Strings**
   ```java
   // ❌ WRONG
   throw new Exception("Entity not found");
   
   // ✅ CORRECT
   throw new BWCNotFoundException(messageService.getMessage("entity.not.found", id));
   ```

6. **Use Objects in Form for Relationships**
   ```java
   // ❌ WRONG
   public class Form {
       YourEntity parent;  // Full object!
   }
   
   // ✅ CORRECT
   public class Form {
       UUID parentId;  // Just the ID
   }
   ```

7. **Forget @Transactional**
   ```java
   // ❌ WRONG
   public YourEntityDTO create(Form form) { ... }
   
   // ✅ CORRECT
   @Transactional(isolation = Isolation.READ_COMMITTED)
   public YourEntityDTO create(Form form) { ... }
   ```

---

## 📋 NAMING CONVENTIONS

### Java Classes:
| Component | Pattern | Example |
|-----------|---------|---------|
| Entity | `{Name}` | `TaskCategory` |
| DTO | `{Name}DTO` | `TaskCategoryDTO` |
| Form | `{Name}EditorForm` | `TaskCategoryEditorForm` |
| Repository | `I{Name}Repository` | `ITaskCategoryRepository` |
| Mapper | `I{Name}Mapper` | `ITaskCategoryMapper` |
| Service Interface | `I{Name}Service` | `ITaskCategoryService` |
| Service Impl | `{Name}Service` | `TaskCategoryService` |
| Resource | `{Name}Resource` | `TaskCategoryResource` |

### API Paths:
```
Base: /api/{entities}          (kebab-case, plural)
Custom: /api/{entities}/{action}
Examples:
  - /api/task-categories
  - /api/task-categories/by-parent/{id}
  - /api/menus/assign-roles
```

### Database:
```
Tables: {entity_name}          (snake_case, singular*)
Columns: {column_name}         (snake_case)
Join Tables: {table1}_{table2} (plural_plural)

*Some entities use plural, check existing patterns
```

---

## 🔍 PRE-COMMIT CHECKLIST

Before committing code for a new API:

- [ ] Created ALL 8 required files
- [ ] Followed exact naming conventions
- [ ] Entity extends `Auditable<String>`
- [ ] DTO extends `RepresentationModel` and implements `Identifiable`
- [ ] Form has validation annotations
- [ ] Form uses IDs for relationships (not objects)
- [ ] Repository extends `IGenericRepository`
- [ ] Mapper extends `IGenericMapper`
- [ ] Service has `@Transactional` on all methods
- [ ] Service uses constructor injection
- [ ] Resource extends `GenericResource`
- [ ] Resource wraps responses in `ApiMessageDTO`
- [ ] All responses use `MessageService`
- [ ] Swagger documentation added (`@Operation`, `@ApiResponse`)
- [ ] No business logic in Resource
- [ ] No Entity objects in DTO/Form
- [ ] All relationships use `LAZY` loading
- [ ] Tested basic CRUD operations

---

## 🤖 AI AGENT INSTRUCTIONS

### 🔴 MANDATORY: Requirement Clarification

**BEFORE generating ANY code, you MUST gather complete requirements:**

When user says: **"Tạo API cho TaskCategory"** or **"Create API for {Entity}"**

**❌ DO NOT immediately generate code!**

**✅ MUST ask these questions:**

```
1. Entity Fields:
   - "Bạn cần những thuộc tính nào cho {Entity}?"
   - "Có trường nào bắt buộc (required) không?"
   - "Có trường nào unique không?"
   
2. Relationships:
   - "Entity này có quan hệ với entity nào khác không?"
   - "Quan hệ là OneToOne, OneToMany, ManyToOne hay ManyToMany?"
   - "Có cần tree structure (parent-child) không?"
   
3. Business Logic:
   - "Có logic đặc biệt nào cần implement không?"
   - "Có validation rules nào ngoài required/unique không?"
   - "Có custom endpoints nào ngoài CRUD cơ bản không?"
   
4. Other Details:
   - "Có constraints nào về giá trị (min/max, length, pattern) không?"
   - "Có cần soft delete không?"
   - "Có cần audit trail (created_by, updated_by) không?" (Default: Yes)
```

**Example Good Response:**

```markdown
Tôi sẽ tạo API cho TaskCategory. Trước khi bắt đầu, cho tôi biết:

1. **Các thuộc tính cần có:**
   - `name` (String, required)
   - `code` (String, required, unique)  
   - `description` (String, optional)
   - `orderIndex` (Integer, optional)
   - Còn thuộc tính nào khác không?

2. **Relationships:**
   - Tôi thấy trong code bạn đã có parent-child relationship
   - Có cần quan hệ nào khác không? (ví dụ: liên kết với Task, Project, etc.)

3. **Business Logic:**
   - Có cần validation đặc biệt không? (ví dụ: code phải theo format ABC-XXX)
   - Có cần custom endpoints không? (ví dụ: findByParent, assignCategories)
   - Có cần sắp xếp theo orderIndex không?

4. **Other:**
   - Có cần soft delete? (Default: Yes)
   - Có giới hạn độ sâu của tree không?

Sau khi có đầy đủ thông tin, tôi sẽ tạo đủ 8 files theo đúng chuẩn.
```

### 🔴 RULE: Never Assume

**❌ NEVER do this:**
```
User: "Tạo API TaskCategory"
AI: [Immediately generates 8 files with assumed fields]
```

**✅ ALWAYS do this:**
```
User: "Tạo API TaskCategory"
AI: [Asks clarifying questions first]
User: [Provides requirements]
AI: [Generates 8 files with EXACT specifications]
```

---

### When User Requests:

**1. "Create a new API for {Entity}"**
- 🔴 **STOP! Ask clarifying questions FIRST** (see above)
- ✅ Wait for user to provide requirements
- ✅ Confirm understanding before generating code
- ✅ Create ALL 8 files following the patterns above
- ✅ Generate complete working code
- ✅ Verify against this rules document

**2. "Add a new endpoint"**
- ❓ Ask: "Endpoint này nhận input gì và trả về gì?"
- ❓ Ask: "Có business logic gì đặc biệt không?"
- ✅ Determine if it's generic (already exists) or custom
- ✅ If custom: Add to Service Interface → Service Impl → Resource
- ✅ Follow naming and documentation standards

**3. "Fix/modify existing API"**
- ✅ Check existing code first
- ✅ Maintain consistency with current patterns
- ✅ Don't break existing functionality

**4. "Why is this not working?"**
- ✅ Check if code violates any of these rules
- ✅ Look for: missing @Transactional, EAGER loading, direct Entity exposure, etc.

**5. "User provides incomplete info"**
- ❓ Ask specific questions about missing details
- ❓ Suggest based on similar entities (e.g., "Menu có tree structure, bạn có cần tương tự không?")
- ✅ Wait for confirmation before proceeding

### ALWAYS:
- **ASK before generating** when requirements are unclear
- Reference Menu implementation as example
- Follow the exact pattern
- Ask for clarification if unclear
- Proactively suggest improvements based on existing patterns
- Confirm understanding: "Tôi hiểu là... Đúng không?"

### NEVER:
- Generate code with assumed requirements
- Skip any layer
- Create shortcuts
- Assume patterns without checking
- Violate these rules without explicit approval
- Make up business logic without user confirmation

---

## 📖 REFERENCE IMPLEMENTATION

**Best Example**: `Menu` entity

Check these files:
- `entities/user/Menu.java`
- `dtos/user/MenuDTO.java`
- `forms/user/MenuEditorForm.java`
- `repositories/user/IMenuRepository.java`
- `mappers/user/IMenuMapper.java`
- `services/user/IMenuService.java`
- `services/user/imp/MenuService.java`
- `resources/user/MenuResource.java`

---

**Enforcement**: 🔴 **MANDATORY - NO EXCEPTIONS**  
**Questions**: Contact project lead  
**Detailed Guide**: `.docs/API_ARCHITECTURE_GUIDE.md`
