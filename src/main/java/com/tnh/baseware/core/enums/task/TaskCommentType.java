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
public enum TaskCommentType implements BaseEnum<String> {
    NORMAL("NORMAL", "normal", "Bình luận thường"),
    REPORT("REPORT", "report", "Báo cáo tiến độ"),
    REJECT("REJECT", "reject", "Từ chối"),
    RESUBMIT("RESUBMIT", "resubmit", "Gửi lại");

    String value;
    String name;
    String displayName;

    public static TaskCommentType fromValue(String value) {
        for (TaskCommentType type : values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown value: " + value);
    }
}
