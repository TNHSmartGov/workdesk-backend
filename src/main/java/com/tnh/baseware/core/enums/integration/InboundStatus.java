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
public enum InboundStatus implements BaseEnum<String> {

    RECEIVED("RECEIVED", "received", "Đã tiếp nhận"),
    PROCESSED("PROCESSED", "processed", "Đã xử lý"),
    FAILED("FAILED", "failed", "Xử lý lỗi");

    String value;
    String name;
    String displayName;

    public static InboundStatus fromValue(String value) {
        for (InboundStatus status : InboundStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new BWCGenericRuntimeException("Unknown InboundDocumentStatus value: " + value);
    }
}

