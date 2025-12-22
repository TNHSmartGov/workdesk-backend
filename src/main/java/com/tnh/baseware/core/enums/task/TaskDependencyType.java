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
public enum TaskDependencyType implements BaseEnum<String> {

    FS("FS", "finish_start", "Hoàn thành → Bắt đầu"),
    SS("SS", "start_start", "Bắt đầu → Bắt đầu"),
    FF("FF", "finish_finish", "Hoàn thành → Hoàn thành"),
    SF("SF", "start_finish", "Bắt đầu → Hoàn thành"),
    PARENT("PARENT", "parent", "Công việc cha – con");

    String value;
    String name;
    String displayName;

    public static TaskDependencyType fromValue(String value) {
        for (TaskDependencyType type : TaskDependencyType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown value: " + value);
    }
}
