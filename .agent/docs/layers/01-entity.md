# Layer 1 — Entity

**Location**: `src/main/java/com/tnh/baseware/core/entities/{domain}/{Entity}.java`

## Template

```java
@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class YourEntity extends Auditable<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String name;

    // ManyToOne — always LAZY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    YourEntity parent;

    // OneToMany — MUST specify LAZY explicitly (default is EAGER in some JPA providers)
    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    Set<YourEntity> children = new HashSet<>();

    // ManyToMany — always LAZY, always @JsonIgnore on owning side if bidirectional
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "entity_roles",
        joinColumns = @JoinColumn(name = "entity_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    Set<Role> roles = new HashSet<>();
}
```

## Rules

| Rule | Detail |
|------|--------|
| `extends Auditable<String>` | Always — provides createdBy, createdDate, updatedBy, updatedDate |
| `implements Serializable` | Always — required for JPA caching and session serialization |
| `@Serial serialVersionUID` | Always — companion to Serializable |
| `FetchType.LAZY` | Mandatory on ALL relationships — `@ManyToOne`, `@OneToMany`, `@ManyToMany` |
| `@OneToMany` explicit LAZY | Must write `fetch = FetchType.LAZY` — do not rely on default |
| `@JsonIgnore` | Required on bidirectional `@OneToMany` and `@ManyToMany` sides |
| `@Builder.Default` | Required on all collection fields when using `@Builder` |
| `@FieldDefaults(PRIVATE)` | Always — replaces `private` keyword on each field |

## Reference

`src/main/java/com/tnh/baseware/core/entities/user/Menu.java`
