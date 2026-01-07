package com.tnh.baseware.core.constants;

public record FieldChange(
        String field,
        String oldValue,
        String newValue
) {}
