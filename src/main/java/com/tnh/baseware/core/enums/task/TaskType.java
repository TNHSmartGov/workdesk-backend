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
public enum TaskType implements BaseEnum<String> {
    WORK("WORK", "work", "Công việc"),
    BUG("BUG", "bug", "Lỗi/Bug"),
    MEETING("MEETING", "meeting", "Họp"),
    FEATURE("FEATURE", "feature", "Tính năng mới"),
    DOCUMENTATION("DOCUMENTATION", "documentation", "Tài liệu");

    String value;
    String name;
    String displayName;

    public static TaskType fromValue(String value) {
        for (TaskType type : TaskType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown value: " + value);
    }
}
