package com.tnh.baseware.core.configs;

import com.tnh.baseware.core.enums.SpringProfile;
import com.tnh.baseware.core.utils.BasewareUtils;
import com.tnh.baseware.core.utils.LogStyleHelper;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String TABLE_PREFIX_PROD = "";
    private static final String TABLE_PREFIX_DEV = "";
    private static final String EMPTY_IDENTIFIER = "";
    private static final Pattern MANY_TO_MANY_PATTERN = Pattern.compile("^[a-z_]+_[a-z_]+$", Pattern.CASE_INSENSITIVE);
    final Environment environment;
    private String tablePrefix = TABLE_PREFIX_DEV;

    @PostConstruct
    public void init() {
        boolean prodProfileActive = isProdProfileActive();
        tablePrefix = prodProfileActive ? TABLE_PREFIX_PROD : TABLE_PREFIX_DEV;

        log.info(LogStyleHelper.info("Custom naming strategy initialized with {} profile (prefix: '{}')"),
                prodProfileActive ? "production" : "development",
                tablePrefix);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        if (name == null || name.getText() == null) {
            log.warn(LogStyleHelper.warn("Received null table name identifier!"));
            return Identifier.toIdentifier(EMPTY_IDENTIFIER);
        }

        String originalName = name.getText().trim();
        String tableName;

        if (isManyToManyTable(originalName)) {
            log.debug(LogStyleHelper.debug("Detected many-to-many table: {}"), originalName);
            tableName = originalName;
        } else {
            tableName = BasewareUtils.pluralize(originalName);
            if (!tableName.equals(originalName)) {
                log.debug(LogStyleHelper.debug("Pluralized table name: {} -> {}"), originalName, tableName);
            }
        }

        String prefixedName = tablePrefix + tableName;

        return Identifier.toIdentifier(prefixedName, name.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        if (name == null || name.getText() == null) {
            log.warn(LogStyleHelper.warn("Received null column name identifier!"));
            return Identifier.toIdentifier(EMPTY_IDENTIFIER);
        }

        String originalName = name.getText().trim();
        String snakeCaseName = BasewareUtils.toSnakeCase(originalName);

        if (!snakeCaseName.equals(originalName)) {
            log.debug(LogStyleHelper.debug("Converted column name: {} -> {}"), originalName, snakeCaseName);
        }

        return Identifier.toIdentifier(snakeCaseName, name.isQuoted());
    }

    private boolean isProdProfileActive() {
        String[] activeProfiles = environment.getActiveProfiles();
        Set<String> profileSet = new HashSet<>(Arrays.asList(activeProfiles));
        return profileSet.contains(SpringProfile.PRODUCTION.getValue());
    }

    private boolean isManyToManyTable(String tableName) {
        if (tableName == null || tableName.length() < 3) {
            return false;
        }

        return MANY_TO_MANY_PATTERN.matcher(tableName).matches();
    }
}
