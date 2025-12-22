package com.tnh.baseware.core.enums.doc;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum DocumentFileType implements BaseEnum<String> {

    ORIGINAL("ORIGINAL", "original", "Bản gốc"),
    APPENDIX("APPENDIX", "appendix", "Phụ lục");

    String value;
    String name;
    String displayName;

    public static DocumentFileType fromValue(String value) {
        for (DocumentFileType type : DocumentFileType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown DocumentFileType value: " + value);
    }
}

