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
public enum DocumentStatus implements BaseEnum<String> {

    DRAFT("DRAFT", "draft", "Bản nháp"),
    RECEIVED("RECEIVED", "received", "Đã tiếp nhận"),
    VALIDATED("VALIDATED", "validated", "Đã kiểm tra"),
    ARCHIVED("ARCHIVED", "archived", "Lưu trữ");

    String value;
    String name;
    String displayName;

    public static DocumentStatus fromValue(String value) {
        for (DocumentStatus status : DocumentStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new BWCGenericRuntimeException("Unknown DocumentStatus value: " + value);
    }
}

