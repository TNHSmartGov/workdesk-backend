# Layer 8 — Resource (Controller)

**Location**: `src/main/java/com/tnh/baseware/core/resources/{domain}/{Entity}Resource.java`

## Template

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

    // Custom endpoint — full Swagger annotation required
    @Operation(summary = "Find entities by parent")
    @ApiResponse(responseCode = "200", description = "Entities retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiMessageDTO.class)))
    @GetMapping("/by-parent/{parentId}")
    public ResponseEntity<ApiMessageDTO<List<YourEntityDTO>>> findByParent(
            @PathVariable UUID parentId) {
        var result = yourEntityService.findByParent(parentId);
        return ResponseEntity.ok(ApiMessageDTO.<List<YourEntityDTO>>builder()
                .data(result)
                .result(true)
                .message(messageService.getMessage("entities.found"))
                .code(HttpStatus.OK.value())
                .build());
    }

    // Custom endpoint with pagination
    @Operation(summary = "Find by role with pagination")
    @ApiResponse(responseCode = "200", description = "Retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiMessageDTO.class)))
    @GetMapping("/by-role/{roleId}/pagination")
    public ResponseEntity<ApiMessageDTO<PagedModel<YourEntityDTO>>> findByRolePaginated(
            @PathVariable UUID roleId,
            Pageable pageable,
            PagedResourcesAssembler<YourEntityDTO> assembler) {
        var page = yourEntityService.findByRole(roleId, pageable);
        var pagedModel = assembler.toModel(page, this::toModel);
        return ResponseEntity.ok(ApiMessageDTO.<PagedModel<YourEntityDTO>>builder()
                .data(pagedModel)
                .result(true)
                .message(messageService.getMessage("entities.found"))
                .code(HttpStatus.OK.value())
                .build());
    }
}
```

## Rules

| Rule | Detail |
|------|--------|
| `extends GenericResource<E, F, D, UUID>` | Required — provides 12 standard endpoints |
| `@Tag` | Required for Swagger grouping |
| `@RequestMapping` | Use `${baseware.core.system.api-prefix}/your-entities` |
| Kebab-case plural path | `/task-categories`, `/menus`, `/your-entities` |
| All responses in `ApiMessageDTO<T>` | Never return DTO or Entity directly |
| `@Operation` + full `@ApiResponse` | Required on every custom endpoint |
| `content = @Content(...)` in `@ApiResponse` | Must include `mediaType` + `@Schema(implementation = ApiMessageDTO.class)` |
| No business logic | Delegate everything to service |
| No direct repository access | Only call service methods |

## @ApiResponse — Full Form (mandatory)

```java
@ApiResponse(responseCode = "200", description = "Your description here",
        content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiMessageDTO.class)))
```

Never omit `content = @Content(...)`. The shorthand form without content is incomplete.

## Custom Annotation: @ApiOkResponse

The project has a convenience annotation `@ApiOkResponse` in `com.tnh.baseware.core.annotations`:

```java
@ApiOkResponse(value = YourEntityDTO.class, type = ApiResponseType.LIST)
@GetMapping("/custom-endpoint")
public ResponseEntity<...> customEndpoint() { ... }
```

Check `ApiResponseType` enum for available types. Use when the standard `@ApiResponse` pattern is repetitive.

## Reference

`src/main/java/com/tnh/baseware/core/resources/user/MenuResource.java`
