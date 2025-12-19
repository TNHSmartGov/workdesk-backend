package com.tnh.baseware.core.enums;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum OrganizationLevel implements BaseEnum<Integer> {
    PROVINCE(1, "province", "Tỉnh/Thành phố"),
    COMMUNE(2, "commune", "Xã/Phường"),
    DEPARTMENT(3, "department", "Sở/Ban/Ngành"),
    COMPANY(4, "company", "Doanh nghiệp");

    Integer value;
    String name;
    String displayName;

    public static OrganizationLevel fromValue(Integer value) {
        for (OrganizationLevel level : OrganizationLevel.values()) {
            if (level.getValue().equals(value)) {
                return level;
            }
        }
        throw new BWCGenericRuntimeException("Unknown OrganizationLevel value: " + value);
    }
}
