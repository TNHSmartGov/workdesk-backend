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
public enum OrganizationRole implements BaseEnum<String> {
    UNIT_LEADER("UNIT_LEADER", "Trưởng đơn vị", 1),
    DEPUTY("DEPUTY", "Phó đơn vị", 2),
    STAFF("STAFF", "Nhân viên", 3);

    String value;
    String displayName;
    int orderIndex;

    @Override
    public String getValue() {
        return value;
    }

    public static OrganizationRole fromValue(String value) {
        for (OrganizationRole role : OrganizationRole.values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        throw new BWCGenericRuntimeException("Unknown OrganizationRole value: " + value);
    }
}
