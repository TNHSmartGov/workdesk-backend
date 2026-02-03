package com.tnh.baseware.core.enums.stats;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Loại snapshot thống kê hằng ngày
 * 
 * MIDDAY: Snapshot giữa ngày (12:00 PM) - Capture hoạt động buổi sáng
 * END_OF_DAY: Snapshot cuối ngày (17:30 PM) - Capture toàn bộ ngày làm việc
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum SnapshotType implements BaseEnum<String> {
    MIDDAY("MIDDAY", "midday", "Giữa ngày"),
    END_OF_DAY("END_OF_DAY", "end_of_day", "Cuối ngày");

    String value;
    String name;
    String displayName;

    public static SnapshotType fromValue(String value) {
        for (SnapshotType type : SnapshotType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown SnapshotType value: " + value);
    }
}
