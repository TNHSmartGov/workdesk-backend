# Layer 2 — DTO

**Location**: `src/main/java/com/tnh/baseware/core/dtos/{domain}/{Entity}DTO.java`

## Template

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YourEntityDTO extends RepresentationModel<YourEntityDTO> implements Identifiable<UUID> {

    UUID id;
    String name;

    // Nested DTOs for relationships — NEVER Entity objects
    YourEntityDTO parent;
    List<YourEntityDTO> children;

    AnotherEntityDTO relatedEntity;
}
```

## Rules

| Rule | Detail |
|------|--------|
| `extends RepresentationModel<YourEntityDTO>` | Required for HATEOAS link support |
| `implements Identifiable<UUID>` | Required by GenericResource for model assembly |
| `@JsonInclude(NON_NULL)` | Only serialize non-null fields — keeps response clean |
| `@EqualsAndHashCode(callSuper = true)` | Include parent class fields in equals/hashCode |
| No Entity objects | Use nested DTOs (`YourEntityDTO parent`, not `YourEntity parent`) |
| No sensitive data | Strip passwords, tokens, internal flags |

## Reference

`src/main/java/com/tnh/baseware/core/dtos/user/MenuDTO.java`
