# 📘 Hướng Dẫn Tích Hợp Agile (Scrum/Kanban)

Tài liệu này mô tả chi tiết các API và quy trình tích hợp tính năng quản lý dự án theo mô hình Agile cho Frontend.

## 🎯 1. Mục Đích (Purpose)
Module Agile được xây dựng nhằm giải quyết các bài toán sau:
- **Chu kỳ triển khai (Sprints)**: Cho phép chia nhỏ dự án thành các giai đoạn ngắn hạn (Sprint 1 tuần, 2 tuần...).
- **Đánh giá & Ước lượng (Estimation)**: Cung cấp khả năng đánh giá độ phức tạp (Story Points) và thời gian (Hours) cho từng công việc.
- **Không ảnh hưởng Core System**: Các dữ liệu này được lưu trữ tách biệt, không làm thay đổi luồng xử lý `Task` hiện tại nếu dự án không dùng Agile.

---

## 🚀 2. Hướng Dẫn Sử Dụng (Integration Guide)

### 2.1. Quản lý Sprint (Sprint Management)
Dành cho (Product Owner / Scrum Master / Leader).

**API Base**: `/api/v1/sprints`

| Chức năng | Method | Endpoint | Payload | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| **Tạo Sprint** | `POST` | `/` | `SprintEditorForm` | Tạo một sprint mới (Status mặc định: PENDING) |
| **Lấy DS Sprint** | `GET` | `/by-project/{projectId}` | - | Lấy danh sách sprint để hiển thị trên Board Header hoặc Filter |
| **Bắt đầu Sprint** | `POST` | `/{id}/start` | - | Chuyển trạng thái sang ACTIVE. Hệ thống sẽ ghi nhận `start_date` thực tế. |
| **Kết thúc Sprint** | `POST` | `/{id}/complete` | - | Đóng sprint (COMPLETED). Các task chưa xong nên được đẩy sang sprint sau hoặc backlog. |

**Ví dụ Payload tạo Sprint:**
```json
{
  "name": "Sprint 1: Authentication",
  "goal": "Hoàn thiện đăng nhập/đăng ký",
  "start_date": "2024-02-01T00:00:00Z",
  "end_date": "2024-02-14T00:00:00Z",
  "project_id": "uuid-cua-du-an"
}
```

### 2.2. Quản lý Thông tin Agile của Task (Task Agile Info)
Dành cho thành viên dự án khi lập kế hoạch (Planning) hoặc cập nhật tiến độ.

**API Base**: `/api/v1/task-agile-info`

| Chức năng | Method | Endpoint | Mô tả |
| :--- | :--- | :--- | :--- |
| **Xem chi tiết** | `GET` | `/by-task/{taskId}` | Lấy thông tin Story Point, Estimate của 1 task |
| **Cập nhật** | `PUT` | `/by-task/{taskId}` | Gán task vào sprint, cập nhật điểm, giờ làm |

**Kịch bản 1: Kéo thả Task vào Sprint (Scrum Board Planning)**
Khi người dùng kéo một Task từ "Backlog" vào "Sprint 1":
1. Gọi API `PUT /api/v1/task-agile-info/by-task/{taskId}`
2. Payload:
   ```json
   {
     "sprint_id": "uuid-cua-sprint-1"
   }
   ```

**Kịch bản 2: Ước lượng công việc (Poker Planning)**
Khi cập nhật Story Point cho task:
```json
{
  "story_points": 5,
  "original_estimate": 8.0, // Đơn vị: giờ
  "remaining_estimate": 8.0
}
```

**Kịch bản 3: Log work (Cập nhật tiến độ)**
Khi daily update, developer cập nhật thời gian còn lại:
```json
{
  "remaining_estimate": 4.0, // Còn 4h nữa là xong
  "completed_hours": 4.0     // Đã làm được 4h
}
```

---

## 3. Cấu trúc Dữ liệu Phản hồi (Response Data)

Khi gọi API lấy danh sách Task (`GET /api/v1/tasks`), field `data` trả về (TaskDTO) sẽ có thêm trường `agile_info` (nếu task đó đã có thông tin Agile).

**Ví dụ response TaskDTO:**
```json
{
  "id": "task-uuid",
  "title": "Thiết kế Database",
  "status": "IN_PROGRESS",
  "progress": 50,
  "agile_info": {
    "sprint": {
      "id": "sprint-uuid",
      "name": "Sprint 1",
      "status": "ACTIVE"
    },
    "story_points": 5,
    "original_estimate": 8.0,
    "remaining_estimate": 4.0,
    "completed_hours": 4.0
  }
}
```
> **Lưu ý**: Nếu `agile_info` là `null`, Frontend nên hiển thị mặc định (0 story points, chưa assign sprint).

---

## 4. Quyền hạn (Permissions)
- **Xem dữ liệu**: Tất cả thành viên trong Project.
- **Tạo/Sửa Sprint**: Chỉ User có role `MANAGER` hoặc `UNIT_LEADER` trong Organization/Project (Tùy cấu hình RBAC hiện tại).
- **Cập nhật Task Agile**: Người được assign task hoặc quản lý.

---

## 5. Lọc & Tìm kiếm nâng cao (Advanced Search)
Frontend sử dụng API `/api/v1/tasks/search` (POST) để lấy danh sách task theo điều kiện.

**Lấy Task thuộc Sprint X:**
```json
{
  "filters": [
    {
      "key": "project.id",
      "operator": "EQUAL",
      "fieldType": "UUID",
      "value": "{projectId}"
    },
    {
      "key": "agileInfo.sprint.id",
      "operator": "EQUAL",
      "fieldType": "UUID",
      "value": "{sprintId}"
    }
  ]
}
```

**Lấy Task trong Backlog (Chưa có Sprint):**
```json
{
  "filters": [
    {
      "key": "project.id",
      "operator": "EQUAL",
      "fieldType": "UUID",
      "value": "{projectId}"
    },
    {
      "key": "agileInfo.sprint.id",
      "operator": "IS_NULL"
    }
  ]
}
```
*Ghi chú: Toán tử `IS_NULL` giúp tìm cả những task chưa bao giờ khởi tạo thông tin Agile hoặc đã khởi tạo nhưng chưa gán Sprint.*

## 6. Sắp xếp thứ tự thẻ (Kanban Sorting)
Khi người dùng thay đổi thứ tự thẻ trên Board, Frontend gửi field `board_position` (Double) để lưu thứ tự.

**Logic sắp xếp:**
- Khi lấy danh sách, Sort theo `board_position` ASC.
- Giá trị này là kiểu số thực (`Double`).
- Khi thêm mới vào cuối danh sách: `board_position = max_current_position + 60000` (hoặc bước nhảy an toàn khác như 10000). Giá trị khởi điểm nên bắt đầu từ mốc lớn (ví dụ `60000`).
- Khi chèn giữa thẻ A và thẻ B: `board_position = (posA + posB) / 2`.
- Payload cập nhật:
  ```json
  PUT /api/v1/task-agile-info/by-task/{taskId}
  {
    "board_position": 150000.0
  }
  ```



