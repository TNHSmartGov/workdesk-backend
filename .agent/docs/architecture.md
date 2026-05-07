# Architecture — 8-Layer Pattern

## Overview

```
┌─────────────┐
│   Entity    │  ← Defines DB structure (JPA model)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Repository  │  ← Data access layer (IGenericRepository)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Mapper    │  ← Converts Entity ↔ DTO ↔ Form (MapStruct)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │  ← Business logic + transaction management
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Resource   │  ← REST API controller (GenericResource)
└─────────────┘
```

## Request Flow — CREATE Example

```
Client
  │  POST /api/menus  {body: MenuEditorForm JSON}
  ▼
Resource (MenuResource)
  │  @Valid validates form
  │  delegates to service.create(form)
  ▼
Service (MenuService)
  │  @Transactional(isolation = READ_COMMITTED)
  │  calls mapper.formToEntity(form, fetcher, repository)
  ▼
Mapper (IMenuMapper)
  │  converts form → entity
  │  fetches related entities by ID via GenericEntityFetcher
  ▼
Repository (IMenuRepository)
  │  repository.save(entity)
  ▼
Database
  │  returns saved entity with generated UUID
  ▼
Mapper
  │  mapper.entityToDTO(savedEntity)
  ▼
Service → Resource
  │  wraps DTO in ApiMessageDTO
  ▼
Client  ← ResponseEntity<ApiMessageDTO<MenuDTO>>
```

## Generic Classes (auto-provides base functionality)

| Class | Description |
|-------|-------------|
| `GenericService<E,F,D,R,M,I>` | Base service with CRUD + search + pagination |
| `GenericResource<E,F,D,I>` | Base controller with 12 standard endpoints |
| `IGenericRepository<E,I>` | Base repo extending JpaRepository + custom queries |
| `IGenericMapper<E,F,D>` | Base mapper interface (MapStruct) |
| `GenericEntityFetcher` | Fetches related entity by ID for mapper `@Context` |

## GenericService Fields (PROTECTED — accessible directly in subclass)

```java
// In GenericService:
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
R repository;
M mapper;
MessageService messageService;
Class<E> entityClass;
```

Subclasses access `repository`, `mapper`, `messageService` directly — no getter needed.

## Auto-provided Endpoints (GenericResource)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/{entities}` | Create |
| `PUT` | `/{entities}/{id}` | Update |
| `GET` | `/{entities}/{id}` | Find by ID |
| `GET` | `/{entities}` | Find all |
| `GET` | `/{entities}/pagination` | Paginated |
| `GET` | `/{entities}/active` | Active records |
| `GET` | `/{entities}/by-field` | Find by field |
| `POST` | `/{entities}/search` | Advanced search |
| `DELETE` | `/{entities}/{id}` | Hard delete |
| `DELETE` | `/{entities}/{id}/soft` | Soft delete |
| `DELETE` | `/{entities}/batch` | Batch delete |
| `GET` | `/{entities}/enum` | Enum values |
