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
public enum TaskMemberRole implements BaseEnum<String> {
    LEAD("LEAD", "lead", "Chủ trì", 2),
    ASSIGNEE("ASSIGNEE", "assignee", "Người thực hiện", 3),
    REVIEWER("REVIEWER", "reviewer", "Người kiểm duyệt", 4),
    WATCHER("WATCHER", "watcher", "Người theo dõi", 5),
    OWNER("OWNER", "owner", "Người sở hữu", 1);

    String value;
    String name;
    String displayName;
    int order;

    public static TaskMemberRole fromValue(String value) {
        for (TaskMemberRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new BWCGenericRuntimeException("Unknown TaskMemberRole value: " + value);
    }
}
