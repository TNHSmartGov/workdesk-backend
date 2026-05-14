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
    MENU_TYPE("MENU_TYPE", "Menu Type", "Kiểu menu "),
    PROJECT_TYPE("PROJECT_TYPE", "Project Type", "Kiểu dự án"),
    ORGANIZATION_TITLE("ORGANIZATION_TITLE", "Organization Title", "Chức danh tổ chức"),
    TASK_CATEGORY("TASK_CATEGORY", "Task Category", "Phân Nhóm Công Việc"),
    DOCUMENT_TYPE("DOCUMENT_TYPE", "Document Type", "Loại văn bản"),
    DOCUMENT_FORM("DOCUMENT_FORM", "Document Form", "Hình thức văn bản");

    String value;
    String name;
    String displayName;

    public static CategoryCode fromValue(String value) {
        for (var category : CategoryCode.values()) {
            if (category.getValue().equals(value)) {
                return category;
            }
        }
        throw new BWCGenericRuntimeException("Unknown category code: " + value);
    }
}
