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
public enum ProjectStatus implements BaseEnum<String> {

    TODO("TODO", "todo", "Mới tạo"),
    IN_PROGRESS("IN_PROGRESS", "in_progress", "Đang thực hiện"),
    REVIEW("REVIEW", "review", "Chờ duyệt"),
    DONE("DONE", "done", "Hoàn thành"),
    CANCELLED("CANCELLED", "cancelled", "Đã hủy");

    String value;
    String name;
    String displayName;

    public static ProjectStatus fromValue(String value) {
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new BWCGenericRuntimeException("Unknown value: " + value);
    }
}
