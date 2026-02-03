# Organization Daily Statistics - Quick Start Guide

**Feature:** Hệ thống thống kê hằng ngày cho tổ chức với phân quyền phân cấp

---

## 🎯 Overview

- **Dual Daily Snapshots:** 12:00 PM & 17:30 PM (T2-T6)
- **17 Core Metrics:** Tasks, Projects, Performance
- **Hierarchical Authorization:** 3-tier system (Super Admin → Unit Leader → Regular User)
- **Auto-Scheduled:** Tự động tính toán mỗi ngày

---

## 🔐 Phân Quyền (Authorization)

### Rule 1: Super Admin / System Org → Access ALL
```
✅ User có superAdmin = true
✅ User thuộc org có is_System = true
→ Xem TẤT CẢ organizations
```

### Rule 2: Unit Leader / Deputy → Access Hierarchy
```
✅ Org có is_unit = true
✅ User có title: UNIT_LEADER hoặc DEPUTY
✅ Category: ORGANIZATION_TITLE
→ Xem org của mình + TẤT CẢ org con (recursive)
```

### Rule 3: Regular User → Access Own Only
```
⚠️ Org có is_unit = false HOẶC không có leadership title
→ CHỈ xem org của chính mình
```

---

## 📡 API Endpoints

### 1. Get Daily Stats
```http
GET /api/v1/organization-stats/{orgId}/daily?date=2026-02-02&type=END_OF_DAY
Authorization: Bearer {token}
```

### 2. Get Trend
```http
GET /api/v1/organization-stats/{orgId}/trend?from=2026-01-01&to=2026-02-02
```

### 3. Compare Organizations
```http
POST /api/v1/organization-stats/compare
Body: { "orgIds": [...], "date": "2026-02-02", "type": "END_OF_DAY" }
```

### 4. Get Latest
```http
GET /api/v1/organization-stats/{orgId}/latest
```

### 5. Recalculate
```http
POST /api/v1/organization-stats/{orgId}/recalculate?date=2026-02-02&type=MIDDAY
```

---

## 📊 17 Metrics

### Task Metrics (7)
- Total, New Today, Completed Today, Overdue, Due in 3 Days, In Progress, Avg Progress

### Project Metrics (4)  
- Total, Active, Overdue, Completed Today

### Performance Metrics (4)
- Completion Rate, Overdue Rate, Active Users, Avg Completion Time

### Extended Metrics (JSON)
- Flexible custom metrics

---

## 🚀 Quick Deploy

```bash
# 1. Run migration
mvn flyway:migrate

# 2. Rebuild
mvn clean install

# 3. Start app & check logs at 12PM or 5:30PM
```

---

## 🧪 Test Authorization

```bash
# Case 1: Own org
curl GET /api/v1/organization-stats/{ownOrgId}/latest
# Expected: 200 OK

# Case 2: Different org (regular user)
curl GET /api/v1/organization-stats/{otherOrgId}/latest  
# Expected: 403 Forbidden

# Case 3: Child org (Unit Leader)
curl GET /api/v1/organization-stats/{childOrgId}/latest
# Expected: 200 OK
```

---

## 📚 Files Created

- **12 Java files:** Entities, Repos, Services, Scheduler, API
- **1 SQL migration:** V999__create_organization_daily_stats.sql
- **2 i18n files:** messages.properties (EN/VI)

---

## ❗ Important Notes

1. **PostgreSQL Required:** Uses Recursive CTE for hierarchy
2. **Update Migration Version:** Change V999 to actual version
3. **Test Thoroughly:** Complex authorization needs careful testing
4. **Performance:** Recursive query limited to 100 levels

---

**Full Documentation:** See [walkthrough.md](./walkthrough.md)
