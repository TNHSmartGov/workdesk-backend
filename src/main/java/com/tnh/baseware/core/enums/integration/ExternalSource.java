package com.tnh.baseware.core.enums.integration;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum ExternalSource implements BaseEnum<String> {

    EOFFICE("EOFFICE", "eoffice", "Hệ thống eOffice"),
    IGATE("IGATE", "igate", "Hệ thống iGate"),
    INTERNAL("INTERNAL", "internal", "Nội bộ hệ thống");

    String value;
    String name;
    String displayName;

    public static ExternalSource fromValue(String value) {
        for (ExternalSource source : ExternalSource.values()) {
            if (source.getValue().equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new BWCGenericRuntimeException("Unknown ExternalSource value: " + value);
    }
}

