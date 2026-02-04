package com.tnh.baseware.core.enums.project;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum SprintStatus implements BaseEnum<String> {

    PENDING("PENDING", "pending", "Chưa bắt đầu"),
    ACTIVE("ACTIVE", "active", "Đang diễn ra"),
    COMPLETED("COMPLETED", "completed", "Đã hoàn thành");

    private final String value;
    private final String name;
    private final String displayName;

    public static SprintStatus fromValue(String value) {
        for (SprintStatus status : SprintStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new BWCGenericRuntimeException("Unknown SprintStatus value: " + value);
    }
}
