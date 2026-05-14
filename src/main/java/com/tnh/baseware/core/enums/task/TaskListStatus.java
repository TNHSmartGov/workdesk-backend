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
public enum TaskListStatus implements BaseEnum<String> {

    OPEN("OPEN", "open", "Mở"),
    IN_PROGRESS("IN_PROGRESS", "in_progress", "Đang thực hiện"),
    COMPLETED("COMPLETED", "completed", "Đã hoàn thành"),
    CLOSED("CLOSED", "closed", "Đã đóng");

    String value;
    String name;
    String displayName;

    public static TaskListStatus fromValue(String value) {
        for (TaskListStatus type : TaskListStatus.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown value: " + value);
    }
}
