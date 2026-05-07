# User Profile Enhancements & Activity Stats

**Date:** 2026-02-03
**Status:** ✅ Complete
**Developer:** ANTIGRAVITY
**Feature Type:** Enhancement

---

## Overview

Enhancement of the User Profile system to support rich media (Cover Image), join date tracking, and performance-optimized activity statistics. Implements a caching strategy using a dedicated `UserProfile` entity and a nightly job for statistics reconciliation to ensure high performance even with large datasets.

**Key Features:**
- **Extended Profile Info:** Cover image, Join date.
- **Cached Statistics:** Total tasks, Participated tasks, Completed tasks, Completed projects, Performance score.
- **Performance Optimization:** Stats are pre-calculated/cached, read O(1).
- **Nightly Reconciliation:** Automated job to recalculate stats every night at 1:00 AM.
- **Minio Integration:** Direct file upload for Avatar and Cover images.

---

## Changes Made

### Files Created

#### Entities
1. `src/main/java/com/tnh/baseware/core/entities/user/UserProfile.java`
   - Dedicated table for extended profile data and cached stats.
   - One-to-One relationship with `User`.

#### DTOs
2. `src/main/java/com/tnh/baseware/core/dtos/user/UserProfileDTO.java`
   - Data transfer object including stats fields.

#### Repositories
3. `src/main/java/com/tnh/baseware/core/repositories/user/IUserProfileRepository.java`
   - Data access for UserProfile.

4. `src/main/java/com/tnh/baseware/core/repositories/stats/UserStatsCalculationRepository.java`
   - Optimized JPQL queries for aggregating task and project statistics.

#### Services
5. `src/main/java/com/tnh/baseware/core/services/user/IUserProfileService.java`
   - Interface for profile management.

6. `src/main/java/com/tnh/baseware/core/services/user/imp/UserProfileService.java`
   - Implementation handling file uploads (Minio) and stats logic.
   - `refreshUserStats(userId)` method for calculation.

#### Scheduled Jobs
7. `src/main/java/com/tnh/baseware/core/jobs/UserStatsDailyJob.java`
   - Nightly job (1:00 AM) to recalculate stats for all users.

### Files Modified

#### REST API
8. `src/main/java/com/tnh/baseware/core/resources/user/UserResource.java`
   - Added `GET /{id}/profile`
   - Added `POST /{id}/avatar`
   - Added `POST /{id}/cover`

---

## API Changes

### New Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/users/{id}/profile` | Get detailed profile with stats | Authenticated |
| POST | `/users/{id}/avatar` | Upload new avatar | Authenticated |
| POST | `/users/{id}/cover` | Upload new cover image | Authenticated |

---

## Testing

- [x] Profile creation and retrieval.
- [x] Avatar and Cover image upload via Minio/S3.
- [x] Stats calculation logic (Total, Participated, Completed).
- [x] Nightly job execution (verified logic).

---

## Metrics Tracked

1. **Total Tasks:** Total tasks assigned to the user.
2. **Participated Tasks:** Total tasks the user is a member of.
3. **Completed Tasks:** Tasks with status DONE.
4. **Completed Projects:** Projects with status COMPLETED.
5. **Performance:** (Completed Tasks / Total Tasks) * 100.

---

**Total Files:** 8 Created, 1 Modified
**Complexity:** Medium
