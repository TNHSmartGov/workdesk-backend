# Layer 3 — Form (EditorForm)

**Location**: `src/main/java/com/tnh/baseware/core/forms/{domain}/{Entity}EditorForm.java`

## Template

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

    @NotBlank(message = "{code.not.blank}")
    String code;

    // Relationship — use UUID ID only, never Entity object
    @NotNull(message = "{parent.id.not.null}")
    @Schema(description = "ID of parent entity")
    UUID parentId;

    // Optional with constraints
    @Min(0)
    @Max(9999)
    Integer orderIndex;

    String description;
}
```

## Rules

| Rule | Detail |
|------|--------|
| `@JsonNaming(SnakeCaseStrategy)` | Client sends snake_case JSON, Java receives camelCase |
| Validation on all required fields | `@NotBlank`, `@NotNull`, `@Min`, `@Max`, `@Size`, etc. |
| i18n message keys | Always `{key.name}` format — never hardcoded strings |
| `@Schema` on relationship IDs | Documents where the client can get valid IDs |
| UUID IDs only for relationships | `UUID parentId` not `YourEntity parent` |

## Custom Validation Annotations (project-specific)

The project provides these in `com.tnh.baseware.core.annotations`:

| Annotation | Purpose |
|-----------|---------|
| `@NotBlankWithFieldName` | @NotBlank with dynamic field name in message |
| `@NotNullWithFieldName` | @NotNull with dynamic field name |
| `@EmailWithFieldName` | Email validation with field name |
| `@PhoneWithFieldName` | Phone format validation |
| `@UsernameWithFieldName` | Username format validation |
| `@PasswordWithFieldName` | Password strength validation |
| `@SizeWithFieldName` | Size constraint with field name |

Use these instead of standard annotations when field name context is needed in error messages.

## Reference

`src/main/java/com/tnh/baseware/core/forms/user/MenuEditorForm.java`
