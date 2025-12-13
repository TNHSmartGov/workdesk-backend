package com.tnh.baseware.core.specs;

import com.tnh.baseware.core.utils.LogStyleHelper;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
public enum FieldType {

    BOOLEAN {
        public Object parse(String value) {
            return Boolean.valueOf(value);
        }
    },

    CHAR {
        public Object parse(String value) {
            return value.charAt(0);
        }
    },

    DATE {
        public Object parse(String value) {
            Object date = null;
            try {
                // Assuming the input string is in ISO-8601 format or a specific pattern.
                // If it's a specific pattern like "dd/MM/yyyy HH:mm:ss", we need to know the timezone to convert to Instant.
                // For now, let's assume standard ISO-8601 for Instant parsing, or use system default zone if using custom pattern.
                // If the old format "dd/MM/yyyy HH:mm:ss" is still passed, we need to handle it.

                if (value.contains("T")) {
                     date = Instant.parse(value);
                } else {
                     var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
                     date = Instant.from(formatter.parse(value));
                }
            } catch (Exception e) {
                log.error(LogStyleHelper.error("Failed parse field type DATE {}"), e.getMessage());
            }

            return date;
        }
    },

    DOUBLE {
        public Object parse(String value) {
            return Double.valueOf(value);
        }
    },

    INTEGER {
        public Object parse(String value) {
            return Integer.valueOf(value);
        }
    },

    LONG {
        public Object parse(String value) {
            return Long.valueOf(value);
        }
    },

    UUID {
        public Object parse(String value) {
            try {
                return java.util.UUID.fromString(value);
            } catch (IllegalArgumentException e) {
                log.error(LogStyleHelper.error("Failed parse field type UUID {}"), e.getMessage());
                return null;
            }
        }
    },

    STRING {
        public Object parse(String value) {
            return value;
        }
    };

    public abstract Object parse(String value);
}
