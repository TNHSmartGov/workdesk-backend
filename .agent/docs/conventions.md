# Conventions

## Java Class Naming

| Layer | Pattern | Example |
|-------|---------|---------|
| Entity | `{Name}` | `TaskCategory` |
| DTO | `{Name}DTO` | `TaskCategoryDTO` |
| Form | `{Name}EditorForm` | `TaskCategoryEditorForm` |
| Repository | `I{Name}Repository` | `ITaskCategoryRepository` |
| Mapper | `I{Name}Mapper` | `ITaskCategoryMapper` |
| Service Interface | `I{Name}Service` | `ITaskCategoryService` |
| Service Impl | `{Name}Service` | `TaskCategoryService` |
| Resource | `{Name}Resource` | `TaskCategoryResource` |

## Mapper Update Method Naming

The update method in mapper MUST be named `update{Entity}FromForm` (not `updateFromForm`):

| Entity | Correct method name |
|--------|---------------------|
| `Menu` | `updateMenuFromForm` |
| `TaskCategory` | `updateTaskCategoryFromForm` |
| `Role` | `updateRoleFromForm` |

Reason: `IGenericMapper` already defines `void formToEntity(F form, @MappingTarget E entity)`.
Using a generic name conflicts. Always use entity-specific name.

## API Path Naming

```
Base:   /${baseware.core.system.api-prefix}/{entities}    (kebab-case, plural)
Custom: /${baseware.core.system.api-prefix}/{entities}/{action}

Examples:
  /api/task-categories
  /api/task-categories/by-parent/{id}
  /api/menus/assign-roles
  /api/menus/{id}/has-access/{roleId}
```

## Database Naming

```
Tables:      {entity_name}          (snake_case, singular)
Columns:     {column_name}          (snake_case)
Join tables: {table1}_{table2}      e.g., menus_roles
```

## Package / Domain Naming

Group by domain inside each layer:

```
entities/user/      → User, Menu, Role, Token, Privilege
entities/task/      → Task, TaskCategory, TaskDependency
entities/adu/       → Province, Commune, Country
entities/audit/     → Auditable, Category, TrackActivity
```

Same pattern applies for `dtos/`, `forms/`, `repositories/`, `mappers/`, `services/`, `resources/`.

## Constructor Parameter Naming

Always use full, descriptive names — no abbreviations:

```java
// CORRECT
public YourEntityService(IYourEntityRepository repository,
                         IYourEntityMapper mapper,
                         MessageService messageService, ...)

// WRONG
public YourEntityService(IYourEntityRepository repo,
                         IYourEntityMapper mapper,
                         MessageService msg, ...)
```
