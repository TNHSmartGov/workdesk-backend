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
public enum TaskAction implements BaseEnum<String> {
    START("START", "start", "Bắt đầu"),
    COMPLETE("COMPLETE", "complete", "Hoàn thành"),
    APPROVE("APPROVE", "approve", "Duyệt"),
    CANCEL("CANCEL", "cancel", "Hủy");

    private final String value;
    private final String name;
    private final String displayName;

    public static TaskAction fromValue(String value) {
        for (TaskAction status : TaskAction.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new BWCGenericRuntimeException("Unknown TaskAction value: " + value);
    }
}
