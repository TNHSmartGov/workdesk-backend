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
public enum ProjectType implements BaseEnum<String> {

    PERSONAL("PERSONAL", "personal", "Cá nhân"),
    NORMAL("NORMAL", "normal", "Bình thường");

    private final String value;
    private final String name;
    private final String displayName;

    public static ProjectType fromValue(String value) {
        for (ProjectType status : ProjectType.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new BWCGenericRuntimeException("Unknown ProjectType value: " + value);
    }
}
