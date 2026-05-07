---
name: workdesk-api
description: Workdesk Backend coding guide — kiến trúc 8-layer Spring Boot. Dùng khi tạo API mới, thêm endpoint, debug lỗi layer. Load templates cho Entity/DTO/Form/Repository/Mapper/Service/Resource và workflow hỏi-trước-code-sau.
user-invocable: true
---

# AGENT SKILL — Workdesk Backend (TNHSmartGov)

> **Version**: 3.0 | **Updated**: 2026-05-07
> **Applies**: All AI agents on `workdesk-backend`

---

## 1. PROJECT IDENTITY

| Info | Value |
|------|-------|
| **Project** | Workdesk Backend – TNHSmartGov |
| **Framework** | Spring Boot 3.x |
| **ORM** | JPA / Hibernate + MapStruct |
| **Build** | Maven |
| **API Style** | RESTful + HATEOAS (`spring-hateoas`) |
| **API Docs** | Swagger / OpenAPI 3 |
| **Validation** | Jakarta Bean Validation |
| **Base package** | `com.tnh.baseware.core` |
| **Reference entity** | `Menu` (gold standard) |

---

## 2. CORE RULES

```
RULE 1: Ask before code — NEVER assume requirements
RULE 2: 8-layer pattern — NEVER skip any layer
RULE 3: Menu entity is the standard — reference when unsure
RULE 4: No exceptions without project lead approval
```

---

## 3. MANDATORY DOCS (read before writing any code)

### Templates (read the relevant layer before coding):

| Layer | Template file |
|-------|--------------|
| 1 — Entity | `.agent/docs/layers/01-entity.md` |
| 2 — DTO | `.agent/docs/layers/02-dto.md` |
| 3 — Form | `.agent/docs/layers/03-form.md` |
| 4 — Repository | `.agent/docs/layers/04-repository.md` |
| 5 — Mapper | `.agent/docs/layers/05-mapper.md` |
| 6 — Service Interface | `.agent/docs/layers/06-service-interface.md` |
| 7 — Service Impl | `.agent/docs/layers/07-service-impl.md` |
| 8 — Resource | `.agent/docs/layers/08-resource.md` |

### Supporting docs:

| Doc | Purpose |
|-----|---------|
| `.agent/docs/architecture.md` | 8-layer overview + request flow |
| `.agent/docs/conventions.md` | Naming conventions, API paths, DB naming |
| `.agent/docs/forbidden.md` | Forbidden practices — must not violate |
| `.agent/docs/checklist.md` | Pre-commit checklist |

---

## 4. 8-LAYER STRUCTURE

```
src/main/java/com/tnh/baseware/core/
├── entities/{domain}/{Entity}.java                    ← Layer 1
├── dtos/{domain}/{Entity}DTO.java                     ← Layer 2
├── forms/{domain}/{Entity}EditorForm.java             ← Layer 3
├── repositories/{domain}/I{Entity}Repository.java    ← Layer 4
├── mappers/{domain}/I{Entity}Mapper.java              ← Layer 5
├── services/{domain}/I{Entity}Service.java            ← Layer 6
├── services/{domain}/imp/{Entity}Service.java         ← Layer 7
└── resources/{domain}/{Entity}Resource.java           ← Layer 8
```

Request flow:
```
Client → Resource → Service → Mapper → Repository → DB
                  ↑                 ↓
              (Form)             (Entity)
                  ↓
                 DTO → Client
```

---

## 5. AGENT WORKFLOW

### 5.1 New API request
```
STEP 1: Ask requirements (fields, constraints, relationships, custom endpoints, soft delete?)
STEP 2: Confirm understanding with user
STEP 3: Read relevant layer templates from .agent/docs/layers/
STEP 4: Create all 8 files
STEP 5: Verify against .agent/docs/checklist.md
```

### 5.2 Add endpoint
```
Ask: input? output? business logic?
→ Add: Service Interface → Service Impl → Resource
```

### 5.3 Debug / fix
```
Check: .agent/docs/forbidden.md violations
Look for: missing @Transactional, EAGER loading, Entity directly exposed
```

### 5.4 Uncertain
```
→ Check Menu reference files (see section 6)
→ Ask user — never assume
```

---

## 6. REFERENCE FILES (Menu — Gold Standard)

| Layer | File |
|-------|------|
| Entity | `src/main/java/com/tnh/baseware/core/entities/user/Menu.java` |
| DTO | `src/main/java/com/tnh/baseware/core/dtos/user/MenuDTO.java` |
| Form | `src/main/java/com/tnh/baseware/core/forms/user/MenuEditorForm.java` |
| Repository | `src/main/java/com/tnh/baseware/core/repositories/user/IMenuRepository.java` |
| Mapper | `src/main/java/com/tnh/baseware/core/mappers/user/IMenuMapper.java` |
| Service Interface | `src/main/java/com/tnh/baseware/core/services/user/IMenuService.java` |
| Service Impl | `src/main/java/com/tnh/baseware/core/services/user/imp/MenuService.java` |
| Resource | `src/main/java/com/tnh/baseware/core/resources/user/MenuResource.java` |
