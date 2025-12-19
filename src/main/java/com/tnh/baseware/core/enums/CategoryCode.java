package com.tnh.baseware.core.enums;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public enum CategoryCode implements BaseEnum<String> {
    MENU_TYPE("MENU_TYPE"),
    SERVICE_PACKAGE_FEATURE_TYPE("SERVICE_PACKAGE_FEATURE_TYPE"),
    ORGANIZATION_TITLE("ORGANIZATION_TITLE");

    String value;

    public static CategoryCode fromValue(String value) {
        for (var category : CategoryCode.values()) {
            if (category.getValue().equals(value)) {
                return category;
            }
        }
        throw new BWCGenericRuntimeException("Unknown category code: " + value);
    }
}
