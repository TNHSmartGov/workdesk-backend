# Workdesk Backend — TNHSmartGov

- **Stack**: Spring Boot 3.x · JPA/MapStruct · Maven · HATEOAS
- **Package**: `com.tnh.baseware.core`
- **Rule**: Mọi entity phải có đủ 8 layer — Entity → DTO → Form → Repository → Mapper → ServiceInterface → ServiceImpl → Resource
- **Reference**: `entities/user/Menu.java` (gold standard)
- **Before coding**: Đọc `.agent/docs/INDEX.md` — chứa templates cho từng layer
- **Hỏi trước, code sau** — không giả định yêu cầu
