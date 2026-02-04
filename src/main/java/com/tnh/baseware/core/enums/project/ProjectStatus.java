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

    DRAFT("DRAFT", "draft", "Mới tạo"),
    ACTIVE("ACTIVE", "active", "Đang hoạt động"),
    ON_HOLD("ON_HOLD", "on_hold", "Tạm dừng"),
    COMPLETED("COMPLETED", "completed", "Hoàn thành"),
    ARCHIVED("ARCHIVED", "archived", "Đã lưu trữ");

    private final String value;
    private final String name;
    private final String displayName;

    public static ProjectStatus fromValue(String value) {
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new BWCGenericRuntimeException("Unknown ProjectStatus value: " + value);
    }
}
