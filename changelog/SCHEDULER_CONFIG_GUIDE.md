# Organization Stats Scheduler - Configuration Guide

## 📋 Overview

Scheduler hiện hỗ trợ **dynamic configuration** qua `application.yml`, cho phép:
- ✅ Enable/disable toàn bộ scheduler
- ✅ Thêm/bớt schedules động
- ✅ Thay đổi thời gian chạy
- ✅ Enable/disable từng schedule riêng lẻ
- ✅ Không cần rebuild code khi thay đổi

---

## ⚙️ Configuration Format

### Basic Configuration (application-dev.yml)

```yaml
baseware:
  organization-stats:
    enabled: true  # Global enable/disable
    schedules:
      - name: "midday"
        cron: "0 0 12 * * MON-FRI"
        snapshot-type: "MIDDAY"
      - name: "end-of-day"
        cron: "0 30 17 * * MON-FRI"
        snapshot-type: "END_OF_DAY"
```

---

## 🎛️ Configuration Options

### Global Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable/disable tất cả schedules |
| `schedules` | list | `[]` | Danh sách schedules |

### Schedule Settings

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `name` | string | ✅ | Tên schedule (for logging) |
| `cron` | string | ✅ | Cron expression |
| `snapshot-type` | string | ✅ | `MIDDAY` hoặc `END_OF_DAY` |
| `enabled` | boolean | ❌ | Enable/disable schedule này (optional) |

---

## 📝 Examples

### Example 1: Default Configuration (2 snapshots/day)

```yaml
baseware:
  organization-stats:
    enabled: true
    schedules:
      - name: "midday"
        cron: "0 0 12 * * MON-FRI"
        snapshot-type: "MIDDAY"
      - name: "end-of-day"
        cron: "0 30 17 * * MON-FRI"
        snapshot-type: "END_OF_DAY"
```

**Result:** 
- 12:00 PM: MIDDAY snapshot
- 17:30 PM: END_OF_DAY snapshot
- Only weekdays (Monday-Friday)

---

### Example 2: Multiple Snapshots (3 snapshots/day)

```yaml
baseware:
  organization-stats:
    enabled: true
    schedules:
      - name: "morning"
        cron: "0 0 9 * * MON-FRI"
        snapshot-type: "MIDDAY"
      - name: "midday"
        cron: "0 0 12 * * MON-FRI"
        snapshot-type: "MIDDAY"
      - name: "end-of-day"
        cron: "0 30 17 * * MON-FRI"
        snapshot-type: "END_OF_DAY"
```

**Result:**
- 09:00 AM: Morning snapshot
- 12:00 PM: Midday snapshot
- 17:30 PM: End-of-day snapshot

---

### Example 3: Hourly Snapshots (Mon-Fri, 8AM-6PM)

```yaml
baseware:
  organization-stats:
    enabled: true
    schedules:
      - name: "hourly"
        cron: "0 0 8-18 * * MON-FRI"
        snapshot-type: "MIDDAY"
      - name: "end-of-day"
        cron: "0 30 17 * * MON-FRI"
        snapshot-type: "END_OF_DAY"
```

**Result:** Stats every hour from 8AM to 6PM

---

### Example 4: Disable Midday, Keep End-of-Day Only

```yaml
baseware:
  organization-stats:
    enabled: true
    schedules:
      - name: "midday"
        cron: "0 0 12 * * MON-FRI"
        snapshot-type: "MIDDAY"
        enabled: false  # Disabled
      - name: "end-of-day"
        cron: "0 30 17 * * MON-FRI"
        snapshot-type: "END_OF_DAY"
```

**Result:** Only END_OF_DAY snapshot at 17:30 PM

---

### Example 5: Include Weekends

```yaml
baseware:
  organization-stats:
    enabled: true
    schedules:
      - name: "daily"
        cron: "0 0 12 * * *"  # Every day including weekends
        snapshot-type: "MIDDAY"
```

**Note:** Scheduler có safety check skip weekends, nhưng có thể override bằng cron `*`

---

### Example 6: Completely Disable

```yaml
baseware:
  organization-stats:
    enabled: false  # All schedules disabled
    schedules:
      - name: "midday"
        cron: "0 0 12 * * MON-FRI"
        snapshot-type: "MIDDAY"
```

**Result:** No stats generated

---

## 🕐 Cron Expression Guide

### Basic Format
```
┌───────────── second (0-59)
│ ┌───────────── minute (0-59)
│ │ ┌───────────── hour (0-23)
│ │ │ ┌───────────── day of month (1-31)
│ │ │ │ ┌───────────── month (1-12 or JAN-DEC)
│ │ │ │ │ ┌───────────── day of week (0-7 or MON-SUN)
│ │ │ │ │ │
* * * * * *
```

### Common Examples

| Cron Expression | Description |
|-----------------|-------------|
| `0 0 12 * * MON-FRI` | 12:00 PM, Monday-Friday |
| `0 30 17 * * MON-FRI` | 5:30 PM, Monday-Friday |
| `0 0 9-17 * * MON-FRI` | Every hour 9AM-5PM, Monday-Friday |
| `0 0 */2 * * MON-FRI` | Every 2 hours, Monday-Friday |
| `0 0 12 * * *` | 12:00 PM every day |
| `0 0 12 1 * *` | 12:00 PM on 1st day of month |

---

## 🚀 How to Apply Changes

### Method 1: Restart Application
```bash
# Edit application-dev.yml
# Then restart
mvn spring-boot:run
```

### Method 2: Spring Boot DevTools (Hot Reload)
If DevTools enabled:
1. Edit `application-dev.yml`
2. Save file
3. Wait for auto-reload

### Method 3: Actuator Refresh (if configured)
```bash
curl -X POST http://localhost:8080/actuator/refresh
```

---

## 📊 Monitoring

### Startup Logs

Khi application start, scheduler sẽ log configuration:

```
[INFO] Organization stats scheduler ENABLED with 2 schedules:
[INFO]   - midday: 0 0 12 * * MON-FRI (MIDDAY)
[INFO]   - end-of-day: 0 30 17 * * MON-FRI (END_OF_DAY)
```

### Runtime Logs

Khi schedule chạy:

```
[INFO] === Starting midday (MIDDAY) stats generation for 2026-02-02 ===
[INFO] Found 15 organizations to process
[DEBUG] Successfully calculated MIDDAY stats for org: Sở Giáo dục
[INFO] === midday (MIDDAY) stats generation completed: 15 success, 0 errors ===
```

---

## ⚠️ Important Notes

### 1. Snapshot Type Values
Only `MIDDAY` and `END_OF_DAY` are valid:
```yaml
snapshot-type: "MIDDAY"      # ✅ Valid
snapshot-type: "END_OF_DAY"  # ✅ Valid
snapshot-type: "CUSTOM"      # ❌ Invalid - will cause error
```

### 2. Cron Validation
Invalid cron expressions will be caught at startup:
```
[ERROR] Invalid configuration for schedule 'bad-schedule': Cron expression must consist of 6 fields
```

### 3. Duplicate Names
If multiple schedules have same name, all will run (names are for logging only)

### 4. Weekend Safety
Scheduler có built-in weekend check, skip execution nếu weekend (even if cron matches)

### 5. Performance
Avoid too frequent schedules (< 5 minutes) to prevent DB overload

---

## 🔧 Troubleshooting

### Problem: Schedules not running

**Check 1:** Is globally enabled?
```yaml
enabled: true  # Must be true
```

**Check 2:** Are schedules configured?
```yaml
schedules:  # Must not be empty
  - name: "..."
```

**Check 3:** Check startup logs
```
[WARN] Organization stats scheduler is DISABLED
[WARN] No organization stats schedules configured
```

### Problem: Invalid cron expression

**Error:**
```
[ERROR] Invalid configuration for schedule 'midday': ...
```

**Solution:** Validate cron at https://crontab.guru/

### Problem: Stats not saving

Check application logs for errors:
```
[ERROR] Failed to generate MIDDAY stats for organization Sở A: ...
```

---

## 📚 Related Files

- **Config Class:** `OrganizationStatsSchedulerConfig.java`
- **Scheduler:** `OrganizationStatsScheduler.java`
- **Service:** `OrganizationStatsService.java`
- **Config File:** `application-dev.yml`

---

**Last Updated:** 2026-02-02  
**Version:** 1.0
