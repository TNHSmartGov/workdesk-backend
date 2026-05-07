# Project Documentation

## Structure

```
.agent/              ← AI agent configuration + code structure docs
├── skills/
│   └── SKILL.md    ← Agent entry point (load this first)
└── docs/
    ├── INDEX.md    ← Full index of all agent docs
    ├── architecture.md
    ├── conventions.md
    ├── forbidden.md
    ├── checklist.md
    └── layers/
        ├── 01-entity.md
        ├── 02-dto.md
        ├── 03-form.md
        ├── 04-repository.md
        ├── 05-mapper.md
        ├── 06-service-interface.md
        ├── 07-service-impl.md
        └── 08-resource.md

.docs/               ← Project-level documentation (business, requirements)
└── requirements.md
```

## For AI Agents

Start with `.agent/skills/SKILL.md`.

## For Developers

- Architecture: `.agent/docs/architecture.md`
- Layer templates: `.agent/docs/layers/`
- Conventions: `.agent/docs/conventions.md`
- What not to do: `.agent/docs/forbidden.md`
- Before commit: `.agent/docs/checklist.md`
- Business requirements: `.docs/requirements.md`
