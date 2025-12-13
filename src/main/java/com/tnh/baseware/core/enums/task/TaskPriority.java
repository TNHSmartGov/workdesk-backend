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
public enum TaskPriority implements BaseEnum<String> {
    LOW("LOW", "low", "Thấp"),
    MEDIUM("MEDIUM", "medium", "Trung bình"),
    HIGH("HIGH", "high", "Cao"),
    URGENT("URGENT", "urgent", "Khẩn cấp");

    String value;
    String name;
    String displayName;

    public static TaskPriority fromValue(String value) {
        for (TaskPriority type : TaskPriority.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown value: " + value);
    }
}
