package com.tnh.baseware.core.enums.task;

import com.tnh.baseware.core.enums.base.BaseEnum;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum TaskDocumentRelationType implements BaseEnum<String> {

    DIRECTIVE("DIRECTIVE", "directive", "Chỉ đạo"),
    LEGAL_BASIS("LEGAL_BASIS", "legal_basis", "Căn cứ pháp lý"),
    REFERENCE("REFERENCE", "reference", "Liên quan");

    String value;
    String name;
    String displayName;

    public static TaskDocumentRelationType fromValue(String value) {
        for (TaskDocumentRelationType type : TaskDocumentRelationType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new BWCGenericRuntimeException("Unknown TaskDocumentRelationType value: " + value);
    }
}

