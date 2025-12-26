package com.tnh.baseware.core.enums;

import com.tnh.baseware.core.enums.base.BaseEnum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum TitleDefault implements BaseEnum<String> {

    UNIT_LEADER("UNIT_LEADER", "Trưởng đơn vị"),
    DEPUTY("DEPUTY", "Phó đơn vị"),
    STAFF("STAFF", "Nhân viên");

    String value;
    String displayName;

    @Override
    public String getValue() {
        return value;
    }

}
