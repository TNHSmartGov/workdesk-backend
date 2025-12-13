package com.tnh.baseware.core.enums.task;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum LogActionType implements BaseEnum<String> {
    CREATE_TASK("CREATE_TASK", "create_task", "Tạo công việc"),
    UPDATE_INFO("UPDATE_INFO", "update_info", "Cập nhật thông tin"),
    UPDATE_STATUS("UPDATE_STATUS", "update_status", "Cập nhật trạng thái"),
    ASSIGN_MEMBER("ASSIGN_MEMBER", "assign_member", "Phân công thành viên"),
    REMOVE_MEMBER("REMOVE_MEMBER", "remove_member", "Xóa thành viên"),
    MEMBER_SUBMIT("MEMBER_SUBMIT", "member_submit", "Thành viên báo cáo xong"),
    UPLOAD_FILE("UPLOAD_FILE", "upload_file", "Tải lên tài liệu"),
    ADD_COMMENT("ADD_COMMENT", "add_comment", "Thêm bình luận"),
    CLOSE_TASK("CLOSE_TASK", "close_task", "Đóng công việc");

    String value;
    String name;
    String displayName;

    public static LogActionType fromValue(String value) {
        for (LogActionType type : LogActionType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown value: " + value);
    }
}
