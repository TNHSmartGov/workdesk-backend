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
public enum DocumentType implements BaseEnum<String> {

    DECISION("DECISION", "decision", "Quyết định"),
    OFFICIAL_LETTER("OFFICIAL_LETTER", "official_letter", "Công văn"),
    ANNOUNCEMENT("ANNOUNCEMENT", "announcement", "Thông báo"),
    PLAN("PLAN", "plan", "Kế hoạch"),
    REPORT("REPORT", "report", "Báo cáo"),
    GUIDELINE("GUIDELINE", "guideline", "Hướng dẫn"),
    OTHER("OTHER", "other", "Khác");

    String value;
    String name;
    String displayName;

    public static DocumentType fromValue(String value) {
        for (DocumentType type : DocumentType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown DocumentType value: " + value);
    }
}
