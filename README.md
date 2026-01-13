# Workdesk Backend - TNHSmartGov

> **Backend API for Workdesk Management System**

---

## 🏗️ Architecture

This project follows a **strict layered architecture pattern**.

**All developers and AI assistants MUST follow the coding standards.**

---

## 📚 **MANDATORY DOCUMENTATION**

### 🔴 Before writing ANY code, read:

**📖 `.docs/API_CODING_RULES.md`** - Mandatory coding standards (STRICT)

**📘 `.docs/API_ARCHITECTURE_GUIDE.md`** - Architecture explanation & examples

**📋 `.docs/README.md`** - Documentation index

---

## 🎯 Quick Reference

### Creating a New API?

**Required Files** (ALL 8 are mandatory):
```
1. entities/{domain}/{Entity}.java
2. dtos/{domain}/{Entity}DTO.java
3. forms/{domain}/{Entity}EditorForm.java
4. repositories/{domain}/I{Entity}Repository.java
5. mappers/{domain}/I{Entity}Mapper.java
6. services/{domain}/I{Entity}Service.java
7. services/{domain}/imp/{Entity}Service.java
8. resources/{domain}/{Entity}Resource.java
```

**Reference Implementation**: Check `Menu` entity (all layers)

**Detailed Rules**: See `.docs/API_CODING_RULES.md`

---

## 🤖 AI Assistant Instructions

When assisting with this project:

1. ✅ **ALWAYS** read `.docs/API_CODING_RULES.md` before generating code
2. ✅ **ALWAYS** follow the 8-layer pattern
3. ✅ **ALWAYS** use Menu as reference example
4. ✅ **ALWAYS** verify against the mandatory checklist
5. ❌ **NEVER** skip any layer
6. ❌ **NEVER** deviate from the pattern without explicit approval

---

## 🚀 Tech Stack

- **Framework**: Spring Boot 3.x
- **Database**: JPA/Hibernate
- **ORM**: Spring Data JPA
- **Mapping**: MapStruct
- **API**: RESTful with HATEOAS
- **Documentation**: Swagger/OpenAPI
- **Validation**: Jakarta Bean Validation
- **Build**: Maven

---

## 📖 Documentation

See `.docs/` directory for:
- API Coding Rules (mandatory)
- Architecture Guide
- Development workflows

---

## 👥 Contributing

All code must follow the standards in `.docs/API_CODING_RULES.md`.

No exceptions without project lead approval.

---

**Project**: TNHSmartGov Workdesk Backend  
**Maintained by**: Development Team  
**Documentation**: `.docs/`
