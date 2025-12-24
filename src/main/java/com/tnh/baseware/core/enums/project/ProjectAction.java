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
public enum ProjectAction implements BaseEnum<String> {
    PUBLISH("PUBLISH", "publish", "Đưa vào hoạt động"),
    ARCHIVE("ARCHIVE", "archive", "Lưu trữ");

    private final String value;
    private final String name;
    private final String displayName;

    public static ProjectAction fromValue(String value) {
        for (ProjectAction status : ProjectAction.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new BWCGenericRuntimeException("Unknown ProjectAction value: " + value);
    }
}
