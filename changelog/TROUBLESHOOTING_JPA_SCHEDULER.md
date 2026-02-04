# Troubleshooting: JPA EntityManagerFactory Dependency Issue

## 🐛 Problem

```
UnsatisfiedDependencyException: Cannot resolve reference to bean 'jpaSharedEM_entityManagerFactory'
```

## 🔍 Root Cause

Khi implement `SchedulingConfigurer`, Spring cố gắng khởi tạo scheduler bean **RẤT SỚM** trong Spring lifecycle, TRƯỚC KHI:
- JPA EntityManagerFactory được tạo
- Repositories được khởi tạo
- Dependencies sẵn sàng

### Why `SchedulingConfigurer` Causes This?

```java
public class OrganizationStatsScheduler implements SchedulingConfigurer {
    // Spring cần initialize class này SỚM để register schedules
    // NHƯNG constructor cần:
    private final IOrganizationStatsService statsService;
    private final IOrganizationRepository organizationRepository;
    // → Các dependencies này cần JPA EntityManagerFactory
    // → Circular/Early dependency conflict!
}
```

## ✅ Solution: Add `@Lazy`

```java
@Slf4j
@Lazy  // ← This annotation delays initialization
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationStatsScheduler implements SchedulingConfigurer {
    // ...
}
```

### How `@Lazy` Fixes It

1. **Without @Lazy:**
   ```
   Spring Boot Start
   ├─ Initialize SchedulingConfigurer beans (early)
   │  └─ OrganizationStatsScheduler needs dependencies
   │     └─ Dependencies need JPA
   │        └─ JPA NOT READY YET ❌
   └─ ERROR
   ```

2. **With @Lazy:**
   ```
   Spring Boot Start
   ├─ Mark OrganizationStatsScheduler as lazy
   ├─ Initialize JPA EntityManagerFactory
   ├─ Initialize Repositories
   ├─ Initialize Services
   └─ NOW initialize OrganizationStatsScheduler ✅
   ```

## 📝 Complete Fix

### File: `OrganizationStatsScheduler.java`

```java
import org.springframework.context.annotation.Lazy;

@Slf4j
@Lazy  // Add this
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationStatsScheduler implements SchedulingConfigurer {
    
    IOrganizationStatsService statsService;
    IOrganizationRepository organizationRepository;
    OrganizationStatsSchedulerConfig config;
    
    // Rest of code...
}
```

## 🔄 Alternative Solutions

### Option 1: Use `@Scheduled` Instead of `SchedulingConfigurer`

**Pros:**
- No early initialization issue
- Simpler code

**Cons:**
- Cannot configure schedules dynamically from YAML
- Need to restart app to change cron

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationStatsScheduler {
    
    @Scheduled(cron = "0 0 12 * * MON-FRI")
    public void generateMidDayStats() {
        // ...
    }
}
```

### Option 2: Use `@DependsOn`

Force scheduler to wait for specific beans:

```java
@Slf4j
@Component
@DependsOn({"entityManagerFactory", "organizationRepository"})
@RequiredArgsConstructor
public class OrganizationStatsScheduler implements SchedulingConfigurer {
    // ...
}
```

**Issue:** May not work reliably with auto-configured beans.

### Option 3: Manual Lazy Injection

```java
@Lazy
@Autowired
private IOrganizationRepository organizationRepository;
```

**Issue:** More verbose, goes against final field immutability.

## 🎯 Recommended: Use `@Lazy` (Our Solution)

**Why:**
- ✅ Simple one-line fix
- ✅ Keeps all benefits of `SchedulingConfigurer`
- ✅ Maintains YAML-based configuration
- ✅ No code restructuring needed
- ✅ Works reliably

## 📚 Related Issues

### Similar Error Patterns

1. **Early Bean Initialization:**
   ```
   UnsatisfiedDependencyException in SchedulingConfigurer
   UnsatisfiedDependencyException in @Configuration beans
   ```

2. **JPA-Related Dependencies:**
   ```
   Cannot resolve reference to bean 'entityManagerFactory'
   Cannot find bean 'dataSource'
   ```

3. **Circular Dependencies:**
   ```
   The dependencies of some of the beans form a cycle
   ```

**All can be solved with `@Lazy` on the problematic bean.**

## 🧪 Verification

After fix, application should start with logs like:

```
[INFO] Organization stats scheduler ENABLED with 2 schedules:
[INFO]   - midday: 0 0 12 * * MON-FRI (MIDDAY)
[INFO]   - end-of-day: 0 30 17 * * MON-FRI (END_OF_DAY)
```

No `UnsatisfiedDependencyException` errors.

## 📖 References

- [Spring @Lazy Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/Lazy.html)
- [Spring Boot Scheduling Guide](https://spring.io/guides/gs/scheduling-tasks/)
- [SchedulingConfigurer Interface](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/SchedulingConfigurer.html)

---

**Fixed Date:** 2026-02-02  
**Issue:** JPA EntityManagerFactory dependency resolution
**Solution:** `@Lazy` annotation on `OrganizationStatsScheduler`
