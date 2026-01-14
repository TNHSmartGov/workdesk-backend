# 📚 HƯỚNG DẪN KIẾN TRÚC API - Từ Entity đến Resource

> **Ví dụ minh họa**: Entity `Menu` 
> 
> Tài liệu này phân tích chi tiết cách một API được xây dựng từ Entity đến Resource endpoint.

---

## 🏗️ KIẾN TRÚC TỔNG QUAN

```
┌─────────────┐
│   Entity    │  ← Định nghĩa cấu trúc dữ liệu trong database
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Repository  │  ← Truy xuất dữ liệu từ database (Data Access Layer)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Mapper    │  ← Chuyển đổi giữa Entity ↔ DTO ↔ Form
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │  ← Business Logic Layer
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Resource   │  ← REST API Controller (Presentation Layer)
└─────────────┘
```

---

## 📦 CÁC THÀNH PHẦN CHI TIẾT

### 1️⃣ **ENTITY** - Lớp Dữ Liệu (Database Model)

**📍 Đường dẫn**: `entities/user/Menu.java`

**Mục đích**: 
- Định nghĩa cấu trúc bảng trong database
- Map với JPA/Hibernate để ORM
- Chứa các mối quan hệ (relationships)

**Ví dụ Menu Entity**:

```java
@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Menu extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(unique = true, nullable = false)
    String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_type_id", nullable = false)
    Category menuType;

    @Column(unique = true, nullable = false)
    String alias;

    String note;
    String path;
    String link;
    Integer published;
    Integer browserNav;
    String icon;
    Integer menuOrder;
    String description;

    // Self-referencing relationship (Tree structure)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Menu parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    Set<Menu> children = new HashSet<>();

    // Many-to-Many relationship
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "menus_roles",
        joinColumns = @JoinColumn(name = "menu_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    Set<Role> roles = new HashSet<>();
}
```

**🔑 Điểm quan trọng**:
- `@Entity`: Đánh dấu đây là JPA entity
- `extends Auditable<String>`: Kế thừa các trường audit (createdBy, createdDate, etc.)
- `@ManyToOne`, `@OneToMany`, `@ManyToMany`: Định nghĩa relationships
- `FetchType.LAZY`: Lazy loading để tối ưu performance
- `@JsonIgnore`: Tránh circular reference khi serialize JSON
- `@Builder.Default`: Khởi tạo giá trị mặc định cho collection

---

### 2️⃣ **DTO** - Data Transfer Object

**📍 Đường dẫn**: `dtos/user/MenuDTO.java`

**Mục đích**: 
- Định nghĩa cấu trúc dữ liệu trả về cho client
- Tránh expose toàn bộ entity (security)
- Có thể customize data structure khác với entity

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuDTO extends RepresentationModel<MenuDTO> implements Identifiable<UUID> {

    UUID id;
    String title;
    CategoryDTO menuType;  // Nested DTO
    String alias;
    String note;
    String path;
    String link;
    Integer published;
    Integer browserNav;
    String icon;
    Integer menuOrder;
    String description;

    MenuDTO parent;         // Nested DTO cho parent
    List<MenuDTO> children; // Tree structure
}
```

**🔑 Điểm quan trọng**:
- `extends RepresentationModel<MenuDTO>`: Hỗ trợ HATEOAS (Hypermedia links)
- `@JsonInclude(JsonInclude.Include.NON_NULL)`: Chỉ serialize các field không null
- Không chứa relationship phức tạp như `Set<Role>` để tránh over-fetching

---

### 3️⃣ **FORM** - Request Object

**📍 Đường dẫn**: `forms/user/MenuEditorForm.java`

**Mục đích**: 
- Định nghĩa cấu trúc dữ liệu nhận từ client khi create/update
- Validation input data
- Tách biệt request structure với entity

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MenuEditorForm {

    @NotBlank(message = "{title.not.blank}")
    String title;

    @NotNull(message = "{menu.type.id.not.null}")
    @Schema(description = "Values are retrieved from 'categories/by-field?fieldName=code&value=menuType'")
    UUID menuTypeId;

    @NotBlank(message = "{alias.not.blank}")
    String alias;

    String note;

    @NotBlank(message = "{path.not.blank}")
    String path;

    @NotBlank(message = "{link.not.blank}")
    String link;

    @NotNull(message = "{published.not.null}")
    Integer published;

    @NotNull(message = "{browser.nav.not.null}")
    Integer browserNav;

    @NotBlank(message = "{icon.not.blank}")
    String icon;

    @NotNull(message = "{menu.order.not.null}")
    Integer menuOrder;
    
    String description;
}
```

**🔑 Điểm quan trọng**:
- Validation annotations: `@NotBlank`, `@NotNull`
- `@JsonNaming`: Convert snake_case ↔ camelCase
- `@Schema`: Swagger documentation
- Nhận ID của related entities (menuTypeId) thay vì object

---

### 4️⃣ **REPOSITORY** - Data Access Layer

**📍 Đường dẫn**: `repositories/user/IMenuRepository.java`

**Mục đích**: 
- Interface để truy xuất dữ liệu từ database
- Extend từ `IGenericRepository` để có sẵn CRUD operations
- Có thể thêm custom queries

```java
@Repository
public interface IMenuRepository extends IGenericRepository<Menu, UUID> {

    @EntityGraph(attributePaths = {"parent"})
    @Query("SELECT m FROM Menu m")
    List<Menu> findAllWithParent();
}
```

**IGenericRepository** cung cấp sẵn:
- `findById(UUID id)`
- `findAll()`
- `findAll(Pageable pageable)`
- `save(Menu entity)`
- `delete(Menu entity)`
- `findByIdIn(List<UUID> ids)`
- `findAllByDeletedFalse()` - Soft delete support
- `findAllByField(String fieldName, Object value)`
- `findAllParent(String collectionField)` - Tìm parent nodes
- `findAllByEntitiesContaining()` - Query với collections
- Và nhiều methods utility khác

**🔑 Điểm quan trọng**:
- `@EntityGraph`: Eager load relationships để tránh N+1 query problem
- Custom queries với `@Query`
- Kế thừa từ `IGenericRepository` để có sẵn nhiều methods

---

### 5️⃣ **MAPPER** - Object Conversion Layer

**📍 Đường dẫn**: `mappers/user/IMenuMapper.java`

**Mục đích**: 
- Convert giữa Entity ↔ DTO ↔ Form
- Map nested objects
- Custom mapping logic

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IMenuMapper extends IGenericMapper<Menu, MenuEditorForm, MenuDTO> {

    // Form → Entity (for CREATE)
    @Mapping(target = "menuType", expression = "java(fetcher.formToEntity(repository, form.getMenuTypeId()))")
    Menu formToEntity(MenuEditorForm form,
                      @Context GenericEntityFetcher fetcher,
                      @Context ICategoryRepository repository);

    // Form → Entity (for UPDATE)
    @Mapping(target = "menuType", expression = "java(fetcher.formToEntity(repository, form.getMenuTypeId()))")
    void updateMenuFromForm(MenuEditorForm form,
                            @MappingTarget Menu menu,
                            @Context GenericEntityFetcher fetcher,
                            @Context ICategoryRepository repository);

    // Entity → DTO
    @Mapping(source = "parent", target = "parent", qualifiedByName = "mapParent")
    MenuDTO entityToDTO(Menu entity);

    // Custom mapping cho parent
    @Named("mapParent")
    default MenuDTO mapParent(Menu parent) {
        if (parent == null) return null;
        
        var parentDTO = MenuDTO.builder()
                .id(parent.getId())
                .title(parent.getTitle())
                .alias(parent.getAlias())
                // ... other fields
                .build();

        // Map nested menuType
        var menuType = parent.getMenuType();
        if (menuType != null) {
            var menuTypeDTO = CategoryDTO.builder()
                    .id(menuType.getId())
                    .code(menuType.getCode())
                    .name(menuType.getName())
                    .build();
            parentDTO.menuType(menuTypeDTO);
        }

        return parentDTO;
    }

    // Convert flat list to tree structure
    default List<MenuDTO> mapMenusToTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) return List.of();

        // Group children by parent ID
        var parentMap = menus.stream()
                .filter(m -> m.getParent() != null)
                .collect(Collectors.groupingBy(m -> m.getParent().getId()));

        // Build tree from root nodes
        return menus.stream()
                .filter(m -> m.getParent() == null)
                .map(m -> buildMenuTree(m, parentMap))
                .toList();
    }

    default MenuDTO buildMenuTree(Menu m, Map<UUID, List<Menu>> parentMap) {
        var dto = entityToDTO(m);
        List<Menu> children = parentMap.getOrDefault(m.getId(), List.of());

        if (!children.isEmpty()) {
            var childDTOs = children.stream()
                    .map(child -> buildMenuTree(child, parentMap))
                    .toList();
            dto.setChildren(childDTOs);
        }

        return dto;
    }
}
```

**🔑 Điểm quan trọng**:
- MapStruct `@Mapper` annotation
- `@Mapping` với expression cho custom logic
- `@Context` để inject dependencies
- `@MappingTarget` cho update operations
- `@Named` để đặt tên cho custom mapping methods
- Tree structure mapping

---

### 6️⃣ **SERVICE** - Business Logic Layer

**📍 Interface**: `services/user/IMenuService.java`  
**📍 Implementation**: `services/user/imp/MenuService.java`

**Mục đích**: 
- Chứa business logic
- Orchestrate Repository & Mapper
- Transaction management
- Authorization & validation

**Interface**:
```java
public interface IMenuService extends IGenericService<Menu, MenuEditorForm, MenuDTO, UUID> {

    @Override
    MenuDTO create(MenuEditorForm form);

    @Override
    MenuDTO update(UUID id, MenuEditorForm form);

    @Override
    List<MenuDTO> findAll();

    @Override
    Page<MenuDTO> findAll(Pageable pageable);

    // Custom business methods
    void assignMenus(UUID id, List<UUID> ids);
    void removeMenus(UUID id, List<UUID> ids);
    void assignRoles(UUID id, List<UUID> ids);
    void removeRoles(UUID id, List<UUID> ids);
    boolean hasAccess(UUID menuId, UUID roleId);
    
    List<MenuDTO> findAllWithoutRoles();
    List<MenuDTO> findAllWithoutRole(UUID id);
    List<MenuDTO> findAllByRole(UUID id);
    Page<MenuDTO> findAllByRole(UUID id, Pageable pageable);
}
```

**Implementation (một số methods quan trọng)**:
```java
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MenuService extends
        GenericService<Menu, MenuEditorForm, MenuDTO, IMenuRepository, IMenuMapper, UUID> 
        implements IMenuService {

    IRoleRepository roleRepository;
    ICategoryRepository categoryRepository;
    GenericEntityFetcher fetcher;

    public MenuService(IMenuRepository repository,
                       IMenuMapper mapper,
                       MessageService messageService,
                       IRoleRepository roleRepository,
                       ICategoryRepository categoryRepository,
                       GenericEntityFetcher fetcher) {
        super(repository, mapper, messageService, Menu.class);
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.fetcher = fetcher;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MenuDTO create(MenuEditorForm form) {
        // 1. Convert Form → Entity
        var menu = mapper.formToEntity(form, fetcher, categoryRepository);
        
        // 2. Save to database
        var savedMenu = repository.save(menu);
        
        // 3. Convert Entity → DTO
        return mapper.entityToDTO(savedMenu);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MenuDTO update(UUID id, MenuEditorForm form) {
        // 1. Find existing entity
        var menu = repository.findById(id).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("menu.not.found", id)));
        
        // 2. Update entity from form
        mapper.updateMenuFromForm(form, menu, fetcher, categoryRepository);
        
        // 3. Save & return DTO
        return mapper.entityToDTO(repository.save(menu));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDTO> findAll() {
        // Return as tree structure
        return mapper.mapMenusToTree(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MenuDTO> findAll(Pageable pageable) {
        var sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("createdDate"))
        );

        var allMenus = repository.findAllWithParent();
        var parentMenus = repository.findAllParent("parent", sortedPageable);

        // Build tree structure
        var parentMap = allMenus.stream()
                .filter(m -> m.getParent() != null)
                .collect(Collectors.groupingBy(m -> m.getParent().getId()));

        var tree = parentMenus.getContent().stream()
                .map(m -> mapper.buildMenuTree(m, parentMap))
                .toList();
                
        return new PageImpl<>(tree, sortedPageable, parentMenus.getTotalElements());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void assignRoles(UUID id, List<UUID> ids) {
        if (BasewareUtils.isBlank(ids)) return;

        // Find menu
        var menu = repository.findById(id).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("menu.not.found", id)));

        // Find roles
        var roles = roleRepository.findAllById(ids);
        if (BasewareUtils.isBlank(roles)) return;

        // Assign roles to menu
        menu.getRoles().addAll(roles);
        repository.save(menu);
    }
}
```

**GenericService** cung cấp sẵn:
- `create(F form)`: Tạo mới
- `update(I id, F form)`: Cập nhật
- `delete(I id)`: Xóa vĩnh viễn
- `softDeleteById(I id)`: Xóa mềm
- `findById(I id)`: Tìm theo ID
- `findAll()`: Lấy tất cả
- `findAll(Pageable)`: Lấy có phân trang
- `findAllActive()`: Lấy các record active
- `findByField(String, Object)`: Tìm theo field
- `search(SearchRequest)`: Tìm kiếm động
- `getEnumValues(String)`: Lấy enum values

**🔑 Điểm quan trọng**:
- `@Transactional`: Quản lý transaction
- `isolation = Isolation.READ_COMMITTED`: Isolation level
- `readOnly = true`: Optimize cho read operations
- Error handling với custom exceptions
- Message service cho i18n

---

### 7️⃣ **RESOURCE** - REST API Controller

**📍 Đường dẫn**: `resources/user/MenuResource.java`

**Mục đích**: 
- Expose REST API endpoints
- Handle HTTP requests/responses
- Validation
- API documentation (Swagger)

```java
@Tag(name = "Menus", description = "API for managing menus")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/menus")
public class MenuResource extends GenericResource<Menu, MenuEditorForm, MenuDTO, UUID> {

    IMenuService menuService;

    public MenuResource(IGenericService<Menu, MenuEditorForm, MenuDTO, UUID> service,
                        MessageService messageService, 
                        IMenuService menuService,
                        SystemProperties systemProperties) {
        super(service, messageService, systemProperties.getApiPrefix() + "/menus");
        this.menuService = menuService;
    }

    // Custom endpoint: Assign menus to parent
    @Operation(summary = "Assign menus to a parent unit")
    @ApiResponse(responseCode = "200", description = "Menus assigned")
    @PostMapping("/{id}/assign-menus")
    public ResponseEntity<ApiMessageDTO<Integer>> assignMenus(
            @PathVariable UUID id,
            @RequestBody List<UUID> ids) {
        menuService.assignMenus(id, ids);
        return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                .data(1)
                .result(true)
                .message(messageService.getMessage("menus.assigned"))
                .code(HttpStatus.OK.value())
                .build());
    }

    // Custom endpoint: Assign roles to menu
    @Operation(summary = "Assign roles to a menu")
    @ApiResponse(responseCode = "200", description = "Roles assigned")
    @PostMapping("/{id}/assign-roles")
    public ResponseEntity<ApiMessageDTO<Integer>> assignRoles(
            @PathVariable UUID id,
            @RequestBody List<UUID> ids) {
        menuService.assignRoles(id, ids);
        return ResponseEntity.ok(ApiMessageDTO.<Integer>builder()
                .data(1)
                .result(true)
                .message(messageService.getMessage("roles.assigned"))
                .code(HttpStatus.OK.value())
                .build());
    }

    // Custom endpoint: Find menus by role
    @Operation(summary = "Find all menus by role ID")
    @ApiResponse(responseCode = "200", description = "Menus retrieved successfully")
    @GetMapping("/by-role/{roleId}")
    public ResponseEntity<ApiMessageDTO<List<MenuDTO>>> findAllByRole(
            @PathVariable UUID roleId) {
        var menus = menuService.findAllByRole(roleId);
        return ResponseEntity.ok(ApiMessageDTO.<List<MenuDTO>>builder()
                .data(menus)
                .result(true)
                .message(messageService.getMessage("menus.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    // With pagination
    @Operation(summary = "Find all menus by role ID with pagination")
    @GetMapping("/by-role/{roleId}/pagination")
    public ResponseEntity<ApiMessageDTO<PagedModel<MenuDTO>>> findAllByRoleWithPagination(
            @PathVariable UUID roleId,
            Pageable pageable,
            PagedResourcesAssembler<MenuDTO> assembler) {
        var menus = menuService.findAllByRole(roleId, pageable);
        var pagedModel = assembler.toModel(menus, this::toModel);
        return ResponseEntity.ok(ApiMessageDTO.<PagedModel<MenuDTO>>builder()
                .data(pagedModel)
                .result(true)
                .message(messageService.getMessage("menus.found"))
                .code(HttpStatus.OK.value())
                .build());
    }
}
```

**GenericResource** cung cấp sẵn các endpoints**:

| HTTP Method | Endpoint | Description |
|------------|----------|-------------|
| `POST` | `/menus` | Create new menu |
| `PUT` | `/menus/{id}` | Update menu |
| `DELETE` | `/menus/{id}` | Delete menu (hard delete) |
| `DELETE` | `/menus/batch` | Delete multiple menus |
| `DELETE` | `/menus/{id}/soft` | Soft delete menu |
| `GET` | `/menus/{id}` | Get menu by ID |
| `GET` | `/menus` | Get all menus |
| `GET` | `/menus/pagination` | Get all with pagination |
| `GET` | `/menus/active` | Get all active menus |
| `GET` | `/menus/by-field` | Find by field name & value |
| `POST` | `/menus/search` | Advanced search |
| `GET` | `/menus/enum` | Get enum values |

**🔑 Điểm quan trọng**:
- `@RestController`: Kết hợp `@Controller` + `@ResponseBody`
- `@RequestMapping`: Base path cho tất cả endpoints
- `@Operation`, `@ApiResponse`: Swagger documentation
- `@PathVariable`, `@RequestBody`, `@RequestParam`: Parameter binding
- Response wrapped trong `ApiMessageDTO` cho consistent API response
- HATEOAS support với `PagedModel`

---

## 🔄 FLOW HOÀN CHỈNH CỦA 1 REQUEST

### **CREATE Menu** - `POST /api/menus`

```
┌──────────────────────────────────────────────────────┐
│  1. CLIENT gửi HTTP Request                          │
│     POST /api/menus                                  │
│     Body: {                                          │
│       "title": "Dashboard",                          │
│       "menu_type_id": "uuid-123",                   │
│       "alias": "dashboard",                          │
│       ...                                            │
│     }                                                │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  2. RESOURCE (MenuResource)                          │
│     - Nhận request                                   │
│     - Validate @Valid MenuEditorForm                 │
│     - Call service                                   │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  3. SERVICE (MenuService)                            │
│     - Start transaction                              │
│     - Business logic validation                      │
│     - Call mapper.formToEntity()                     │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  4. MAPPER (IMenuMapper)                             │
│     - Convert MenuEditorForm → Menu Entity           │
│     - Fetch related entities (Category by ID)        │
│     - Build relationships                            │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  5. REPOSITORY (IMenuRepository)                     │
│     - Save entity to database                        │
│     - Return saved entity with generated ID          │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  6. MAPPER (IMenuMapper)                             │
│     - Convert Menu Entity → MenuDTO                  │
│     - Map nested objects                             │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  7. SERVICE (MenuService)                            │
│     - Commit transaction                             │
│     - Return MenuDTO                                 │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  8. RESOURCE (MenuResource)                          │
│     - Wrap DTO in ApiMessageDTO                      │
│     - Return ResponseEntity                          │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  9. CLIENT nhận HTTP Response                        │
│     Status: 201 Created                              │
│     Body: {                                          │
│       "result": true,                                │
│       "code": 201,                                   │
│       "message": "Menu created successfully",        │
│       "data": {                                      │
│         "id": "generated-uuid",                      │
│         "title": "Dashboard",                        │
│         ...                                          │
│       }                                              │
│     }                                                │
└──────────────────────────────────────────────────────┘
```

---

## 📋 CHECKLIST TẠO MỘT API MỚI

Khi tạo một entity mới (ví dụ: `TaskCategory`), cần tạo các file sau:

### ✅ **Step 1: Entity** 
- [ ] `entities/task/TaskCategory.java`
- [ ] Định nghĩa fields, relationships
- [ ] Extends `Auditable<String>` nếu cần audit
- [ ] Add JPA annotations

### ✅ **Step 2: DTO**
- [ ] `dtos/task/TaskCategoryDTO.java`
- [ ] Copy fields cần thiết từ Entity
- [ ] Extends `RepresentationModel<TaskCategoryDTO>`
- [ ] Add `@JsonInclude(JsonInclude.Include.NON_NULL)`

### ✅ **Step 3: Form**
- [ ] `forms/task/TaskCategoryEditorForm.java`
- [ ] Định nghĩa fields cho create/update
- [ ] Add validation annotations
- [ ] Add Swagger `@Schema` cho documentation

### ✅ **Step 4: Repository**
- [ ] `repositories/task/ITaskCategoryRepository.java`
- [ ] Extends `IGenericRepository<TaskCategory, UUID>`
- [ ] Add custom query methods nếu cần

### ✅ **Step 5: Mapper**
- [ ] `mappers/task/ITaskCategoryMapper.java`
- [ ] Extends `IGenericMapper<TaskCategory, TaskCategoryEditorForm, TaskCategoryDTO>`
- [ ] Implement `formToEntity()`, `updateFromForm()`, `entityToDTO()`
- [ ] Add custom mapping nếu có logic phức tạp

### ✅ **Step 6: Service Interface**
- [ ] `services/task/ITaskCategoryService.java`
- [ ] Extends `IGenericService<TaskCategory, TaskCategoryEditorForm, TaskCategoryDTO, UUID>`
- [ ] Declare custom business methods

### ✅ **Step 7: Service Implementation**
- [ ] `services/task/imp/TaskCategoryService.java`
- [ ] Extends `GenericService<...>`
- [ ] Implements `ITaskCategoryService`
- [ ] Implement business logic
- [ ] Add `@Transactional` annotations

### ✅ **Step 8: Resource (Controller)**
- [ ] `resources/task/TaskCategoryResource.java`
- [ ] Extends `GenericResource<...>`
- [ ] Add `@RestController`, `@RequestMapping`
- [ ] Add custom endpoints nếu cần
- [ ] Add Swagger annotations

---

## 🎯 NGUYÊN TẮC THIẾT KẾ

### **1. Separation of Concerns (SoC)**
- Mỗi layer có một trách nhiệm rõ ràng
- Entity không biết về DTO/Form
- Controller không access trực tiếp Repository

### **2. Dependency Injection**
- Sử dụng Constructor Injection
- Tất cả dependencies được inject qua constructor
- `@FieldDefaults(makeFinal = true)` để ensure immutability

### **3. Single Responsibility Principle**
- Entity: Chỉ định nghĩa cấu trúc dữ liệu
- Service: Chỉ chứa business logic
- Repository: Chỉ truy xuất dữ liệu
- Mapper: Chỉ convert objects

### **4. Open/Closed Principle**
- Generic classes (`GenericService`, `GenericRepository`) cung cấp base functionality
- Extend và customize qua inheritance

### **5. Don't Repeat Yourself (DRY)**
- Generic base classes tránh code duplication
- Mapper tự động generate code
- Reuse utility methods

### **6. Transaction Management**
- `@Transactional(readOnly = true)`: Read operations
- `@Transactional(isolation = Isolation.READ_COMMITTED)`: Write operations
- Keep transactions short

### **7. Error Handling**
- Throw custom exceptions (`BWCNotFoundException`)
- Use MessageService cho internationalization
- Return proper HTTP status codes

### **8. Performance**
- Use `FetchType.LAZY` cho relationships
- Use `@EntityGraph` để avoid N+1 queries
- Pagination cho large datasets
- DTOs để tránh over-fetching

---

## 🚀 VÍ DỤ ÁP DỤNG CHO TASKCATEGORY

Dựa trên kiến trúc của Menu, đây là cách implement TaskCategory:

```java
// 1. Entity (đã có)
@Entity
public class TaskCategory extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    
    String name;
    String code;
    Integer orderIndex;
    String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    TaskCategory parent;
    
    @OneToMany(mappedBy = "parent")
    Set<TaskCategory> children;
}

// 2. DTO
public class TaskCategoryDTO extends RepresentationModel<TaskCategoryDTO> {
    UUID id;
    String name;
    String code;
    Integer orderIndex;
    String description;
    TaskCategoryDTO parent;
    List<TaskCategoryDTO> children;
}

// 3. Form
public class TaskCategoryEditorForm {
    @NotBlank String name;
    @NotBlank String code;
    Integer orderIndex;
    String description;
    UUID parentId;  // NOT parent object, just ID
}

// 4. Repository
@Repository
public interface ITaskCategoryRepository extends IGenericRepository<TaskCategory, UUID> {
    @EntityGraph(attributePaths = {"parent"})
    List<TaskCategory> findAllWithParent();
}

// 5. Mapper
@Mapper(componentModel = "spring")
public interface ITaskCategoryMapper extends IGenericMapper<TaskCategory, TaskCategoryEditorForm, TaskCategoryDTO> {
    // MapStruct sẽ tự generate implementation
    // Chỉ cần define custom logic nếu cần
}


// 6. Service Interface
public interface ITaskCategoryService extends IGenericService<TaskCategory, TaskCategoryEditorForm, TaskCategoryDTO, UUID> {
    List<TaskCategoryDTO> findAllAsTree();
    List<TaskCategoryDTO> findByParent(UUID parentId);
}

// 7. Service Implementation
@Service
public class TaskCategoryService extends GenericService<...> implements ITaskCategoryService {
    // Constructor với dependencies
    // Implement custom methods
}

// 8. Resource
@RestController
@RequestMapping("${baseware.core.system.api-prefix}/task-categories")
public class TaskCategoryResource extends GenericResource<...> {
    // Constructor
    // Custom endpoints
}
```

---

## 📖 TÀI LIỆU THAM KHẢO

- **Generic Classes**: Xem source code của `GenericService`, `GenericRepository`, `GenericResource`
- **MapStruct**: https://mapstruct.org/
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **HATEOAS**: https://spring.io/projects/spring-hateoas

---

**Tác giả**: AI Assistant  
**Ngày tạo**: 2026-01-13  
**Version**: 1.0
