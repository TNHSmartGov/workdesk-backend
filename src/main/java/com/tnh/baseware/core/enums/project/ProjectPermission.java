package com.tnh.baseware.core.enums.project;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum ProjectPermission {
    PROJECT_VIEW("PROJECT_VIEW", "project_view", "Xem dự án"),
    PROJECT_CREATE("PROJECT_CREATE", "project_create", "Tạo dự án"),
    PROJECT_UPDATE("PROJECT_UPDATE", "project_update", "Cập nhật dự án"),
    PROJECT_DELETE("PROJECT_DELETE", "project_delete", "Xóa dự án"),
    PROJECT_ARCHIVE("PROJECT_ARCHIVE", "project_archive", "Lưu trữ dự án"),

    TASK_CREATE("TASK_CREATE", "task_create", "Tạo công việc"),
    TASK_UPDATE_ANY("TASK_UPDATE", "task_update", "Cập nhật công việc"),
    TASK_DELETE_ANY("TASK_DELETE", "task_delete", "Xóa công việc"),
    TASK_VIEW("TASK_VIEW", "task_view", "Xem công việc"),

    MEMBER_ADD("MEMBER_ADD", "member_add", "Thêm thành viên"),
    MEMBER_REMOVE("MEMBER_REMOVE", "member_remove", "Xóa thành viên"),
    MEMBER_UPDATE("MEMBER_UPDATE", "member_update", "Cập nhật thành viên"),
    MEMBER_VIEW("MEMBER_VIEW", "member_view", "Xem ds thành viên"),

    FILE_UPLOAD("FILE_UPLOAD", "file_upload", "Tải lên tệp"),
    FILE_DOWNLOAD("FILE_DOWNLOAD", "file_download", "Tải xuống tệp"),
    FILE_DELETE("FILE_DELETE", "file_delete", "Xóa tệp"),
    FILE_UPDATE("FILE_UPDATE", "file_update", "Cập nhật tệp");

    String value;
    String name;
    String displayName;

    public static ProjectPermission fromValue(String value) {
        for (ProjectPermission permission : values()) {
            if (permission.value.equalsIgnoreCase(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown ProjectPermission value: " + value);
    }
}
