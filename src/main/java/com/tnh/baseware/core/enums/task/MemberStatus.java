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
public enum MemberStatus implements BaseEnum<String> {
    ASSIGNED("ASSIGNED", "assigned", "Đã nhận việc"),
    PROCESSING("PROCESSING", "processing", "Đang thực hiện"),
    COMPLETED("COMPLETED", "completed", "Đã hoàn thành"),
    BLOCKED("BLOCKED", "blocked", "Đang bị chặn");

    String value;
    String name;
    String displayName;

    public static MemberStatus fromValue(String value) {
        for (MemberStatus type : MemberStatus.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown value: " + value);
    }
}
