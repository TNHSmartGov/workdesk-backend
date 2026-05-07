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
public enum MemberAction implements BaseEnum<String> {
    IN_PROGRESS("IN_PROGRESS", "in_progress", "Đang thực hiện"),
    DONE("DONE", "done", "Hoàn thành");

    public static MemberAction fromValue(String value) {
        for (MemberAction type : MemberAction.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown value: " + value);
    }

    public static MemberStatus mappingActionToStatus(MemberAction action) {
        switch (action) {
            case IN_PROGRESS:
                return MemberStatus.IN_PROGRESS;
            case DONE:
                return MemberStatus.COMPLETED;
            default:
                throw new BWCGenericRuntimeException("Unknown action: " + action);
        }
    }

    String value;
    String name;
    String displayName;
}
