# 📚 Project Documentation

> **Purpose**: This directory contains mandatory documentation and coding standards for the Workdesk Backend project.

---

## 📖 Available Documents

### 🔴 **MANDATORY RULES** (Must Read & Follow)

#### 1. **API_CODING_RULES.md** ⭐️⭐️⭐️
**Status**: 🔴 **STRICT ENFORCEMENT**  
**For**: All developers and AI agents

**Contains**:
- ✅ Mandatory checklist for creating new APIs
- ✅ Complete code templates for all 8 layers
- ✅ Forbidden practices
- ✅ Naming conventions
- ✅ Pre-commit checklist

**When to use**:
- Creating any new entity/API
- Adding new endpoints
- Code review
- Fixing bugs

**Quick Summary**:
Every new entity MUST have these 8 files:
1. Entity
2. DTO
3. Form
4. Repository
5. Mapper
6. Service Interface
7. Service Implementation
8. Resource/Controller

---

### 📘 **REFERENCE GUIDES** (For Learning)

#### 2. **API_ARCHITECTURE_GUIDE.md**
**Status**: 📘 Reference Documentation  
**For**: Understanding the architecture

**Contains**:
- 📐 Detailed architecture explanation
- 🔄 Complete request flow diagrams
- 💡 Real code examples from Menu entity
- 🎓 Design principles
- 📝 Step-by-step walkthrough

**When to use**:
- First time viewing the codebase
- Understanding WHY we use this pattern
- Learning how layers interact
- Need detailed explanations

---

## 🤖 For AI Agents

### Priority Order:

1. **Always check `API_CODING_RULES.md` first** when:
   - User asks to create new API
   - User asks to add endpoints
   - User asks about code structure
   
2. **Then consult `API_ARCHITECTURE_GUIDE.md`** for:
   - Detailed explanations
   - Understanding relationships
   - Complex scenarios

3. **Always reference Menu implementation** as the gold standard example

### Workflow:

```
User Request
    ↓
Check API_CODING_RULES.md
    ↓
Follow the templates
    ↓
Verify against checklist
    ↓
Reference Menu example if needed
    ↓
Deliver code
```

---

## 👨‍💻 For Human Developers

### New to the project?

1. **Read `API_ARCHITECTURE_GUIDE.md`** first
   - Understand the architecture
   - See real examples
   - Learn the flow

2. **Keep `API_CODING_RULES.md` handy**
   - Use as a template
   - Check before committing
   - Reference during code review

3. **Study the Menu implementation**
   - Best practice example
   - Complete working code
   - All patterns demonstrated

### Creating new API?

Use this workflow:

```bash
# 1. Open the coding rules
open .docs/API_CODING_RULES.md

# 2. Follow the 8-step checklist

# 3. Use Menu as reference
# Check: entities/user/Menu.java and all related files

# 4. Before commit: verify pre-commit checklist
```

---

## 📂 Document Index

| Document | Type | Purpose | Audience |
|----------|------|---------|----------|
| **API_CODING_RULES.md** | 🔴 Rules | Mandatory coding standards | All |
| **API_ARCHITECTURE_GUIDE.md** | 📘 Guide | Architecture explanation | New developers |

---

## 🔄 Keeping Documents Updated

**When to update**:
- New patterns emerge
- Architecture changes
- Best practices evolve

**Who can update**:
- Project lead approval required
- Document version in file header

**How to update**:
1. Update the content
2. Increment version
3. Update "Last Updated" date
4. Notify team

---

## ❓ FAQ

**Q: Which document should I read first?**  
A: If you're new, read `API_ARCHITECTURE_GUIDE.md`. If you're coding, use `API_CODING_RULES.md`.

**Q: Can I deviate from the rules?**  
A: No. All rules in `API_CODING_RULES.md` are mandatory. Exceptions require project lead approval.

**Q: What if I found a better pattern?**  
A: Discuss with team first. If approved, update both documents and refactor existing code.

**Q: How do AI agents use these?**  
A: AI agents are configured to automatically follow `API_CODING_RULES.md` when generating code.

**Q: Where's the reference implementation?**  
A: Menu entity is the gold standard. Check all Menu-related files across the 8 layers.

---

## 🚀 Quick Start

### For AI Agents:
```
1. User asks for new API
2. Read API_CODING_RULES.md
3. Follow 8-step template
4. Verify checklist
5. Generate code
```

### For Developers:
```
1. Read API_ARCHITECTURE_GUIDE.md (one time)
2. Open API_CODING_RULES.md (every time)
3. Follow templates
4. Check Menu example
5. Complete pre-commit checklist
6. Commit
```

---

**Maintained by**: Development Team  
**Last Updated**: 2026-01-13  
**Status**: Active
