# Changelog Directory

Thư mục này chứa changelog cho từng feature/plan được implement.

## Quy tắc đặt tên

- **Format:** `YYYY-MM-DD_feature-name.md`
- **Example:** `2026-02-02_organization-daily-stats.md`

## Template

Mỗi changelog entry sử dụng template sau:

```markdown
# [Feature Name]

**Date:** YYYY-MM-DD  
**Status:** ✅ Complete / 🚧 In Progress / ❌ Cancelled  
**Developer:** [Name]

## Overview
Brief description của feature

## Changes Made

### Files Created
- List of new files with paths

### Files Modified
- List of modified files with brief description

### Database Changes
- Migrations, schema changes

## API Changes

### New Endpoints
- List of new API endpoints

### Modified Endpoints
- List of changed endpoints

## Configuration Changes
- application.yml
- Environment variables
- etc.

## Testing
- [ ] Unit tests
- [ ] Integration tests
- [ ] Manual testing completed

## Deployment Notes
Special instructions for deployment

## Related Issues/Tickets
- Link to Jira, GitHub issues, etc.
```

## Index

Xem [CHANGELOG_INDEX.md](./CHANGELOG_INDEX.md) để có danh sách đầy đủ tất cả changes.
