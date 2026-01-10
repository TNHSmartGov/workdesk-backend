package com.tnh.baseware.core.enums.project;

import java.util.Set;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum ProjectMemberRole implements BaseEnum<String> {
    OWNER("OWNER", "owner", "Chủ trì", Set.of(ProjectPermission.values())),
    MANAGER("MANAGER", "manager", "Quản lý dự án", Set.of(
            ProjectPermission.PROJECT_VIEW,
            ProjectPermission.PROJECT_UPDATE,
            ProjectPermission.MEMBER_VIEW,
            ProjectPermission.MEMBER_ADD,
            ProjectPermission.MEMBER_REMOVE,
            ProjectPermission.TASK_UPDATE_ANY,
            ProjectPermission.TASK_CREATE,
            ProjectPermission.TASK_DELETE_ANY,
            ProjectPermission.FILE_DELETE,
            ProjectPermission.FILE_DOWNLOAD,
            ProjectPermission.FILE_UPLOAD,
            ProjectPermission.FILE_UPDATE)),
    MEMBER("MEMBER", "member", "Tham gia", Set.of(
            ProjectPermission.PROJECT_VIEW,
            ProjectPermission.MEMBER_VIEW,
            ProjectPermission.TASK_VIEW,
            ProjectPermission.TASK_CREATE,
            ProjectPermission.FILE_UPLOAD)),
    VIEWER("VIEWER", "viewer", "Theo dõi", Set.of(
            ProjectPermission.PROJECT_VIEW,
            ProjectPermission.MEMBER_VIEW,
            ProjectPermission.TASK_VIEW));

    String value;
    String name;
    String displayName;
    Set<ProjectPermission> permissions;

    public static ProjectMemberRole fromValue(String value) {
        for (ProjectMemberRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new BWCGenericRuntimeException("Unknown ProjectMemberRole value: " + value);
    }

    public boolean hasPermission(ProjectPermission permission) {
        return permissions.contains(permission);
    }

}
