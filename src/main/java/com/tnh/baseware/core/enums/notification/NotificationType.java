package com.tnh.baseware.core.enums.notification;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum NotificationType implements BaseEnum<String> {
    TASK_ASSIGNED("TASK_ASSIGNED", "task_assigned", "Giao việc"),
    TASK_COMPLETED("TASK_COMPLETED", "task_completed", "Hoàn thành công việc"),
    TASK_COMMENT("TASK_COMMENT", "task_comment", "Bình luận công việc"),
    TASK_STATUS_CHANGED("TASK_STATUS_CHANGED", "task_status_changed", "Cập nhật trạng thái"),
    TASK_MEMBER_REMOVED("TASK_MEMBER_REMOVED", "task_member_removed", "Xóa thành viên"),
    TASK_MEMBER_ROLE_CHANGED("TASK_MEMBER_ROLE_CHANGED", "task_member_role_changed", "Đổi vai trò thành viên"),
    TASK_MEMBER_STATUS_CHANGED("TASK_MEMBER_STATUS_CHANGED", "task_member_status_changed", "Đổi trạng thái thành viên"),
    TASK_DUE_SOON("TASK_DUE_SOON", "task_due_soon", "Sắp đến hạn"),
    TASK_OVERDUE("TASK_OVERDUE", "task_overdue", "Quá hạn"),
    MENTION("MENTION", "mention", "Được nhắc tới"),
    SYSTEM_ALERT("SYSTEM_ALERT", "system_alert", "Thông báo hệ thống");

    String value;
    String name;
    String displayName;

    public static NotificationType fromValue(String value) {
        for (NotificationType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown NotificationType value: " + value);
    }
}
