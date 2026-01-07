package com.tnh.baseware.core.utils;

import com.tnh.baseware.core.constants.DiffSnapshot;
import com.tnh.baseware.core.constants.FieldChange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DiffUtil {

    private DiffUtil() {}

    public static List<FieldChange> diff(DiffSnapshot before, DiffSnapshot after) {
        Map<String, Object> oldFields = before.fields();
        Map<String, Object> newFields = after.fields();

        List<FieldChange> changes = new ArrayList<>();

        for (String field : oldFields.keySet()) {
            Object oldVal = oldFields.get(field);
            Object newVal = newFields.get(field);

            if (!Objects.equals(oldVal, newVal)) {
                changes.add(new FieldChange(
                        field,
                        stringify(oldVal),
                        stringify(newVal)
                ));
            }
        }
        return changes;
    }

    private static String stringify(Object val) {
        return val == null ? null : val.toString();
    }
}
