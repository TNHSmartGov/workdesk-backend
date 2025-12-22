package com.tnh.baseware.core.enums.project;

import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum ProjectMemberRole {
    OWNER("OWNER", "owner", "Chủ trì"),
    MANAGER("MANAGER", "manager", "Quản lý dự án"),
    MEMBER("MEMBER", "member", "Tham gia"),
    VIEWER("VIEWER", "viewer", "Theo dõi");

    String value;
    String name;
    String displayName;

    public static ProjectMemberRole fromValue(String value) {
        for (ProjectMemberRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new BWCGenericRuntimeException("Unknown ProjectMemberRole value: " + value);
    }
}
