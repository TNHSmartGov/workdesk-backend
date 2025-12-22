package com.tnh.baseware.core.components;

import com.tnh.baseware.core.enums.base.BaseEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class EnumRegistry {
    private static final String BASE_PACKAGE = "com.tnh.baseware.core.enums";
    private final Map<String, Class<?>> enumCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeEnumRegistry() {
        log.info("Initializing Enum Registry...");
        scanAndCacheEnums();
        log.info("Enum Registry initialized with {} enums: {}", enumCache.size(), enumCache.keySet());
    }

    public Class<?> findEnumClass(String enumName) throws ClassNotFoundException {
        Class<?> enumClass = enumCache.get(enumName);
        if (enumClass == null) {
            throw new ClassNotFoundException("Enum " + enumName + " not found in registry");
        }
        return enumClass;
    }

    public static void main(String[] args) {
        EnumRegistry registry = new EnumRegistry();
        registry.scanAndCacheEnums();
        registry.getAllEnumNames().forEach(System.out::println);
    }

    public Set<String> getAllEnumNames() {
        return enumCache.keySet();
    }

    private void scanAndCacheEnums() {
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                    false);

            // Filter cho enum classes implement BaseEnum
            scanner.addIncludeFilter(new TypeFilter() {
                @Override
                public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
                    String className = null;
                    try {
                        className = metadataReader.getClassMetadata().getClassName();
                        Class<?> clazz = Class.forName(className);
                        return clazz.isEnum() && BaseEnum.class.isAssignableFrom(clazz);
                    } catch (Exception e) {
                        log.debug("Failed to check class: {}", className, e);
                        return false;
                    }
                }
            });

            Set<BeanDefinition> candidates = scanner.findCandidateComponents(BASE_PACKAGE);
            log.debug("Found {} enum candidates", candidates.size());

            for (BeanDefinition candidate : candidates) {
                try {
                    String className = candidate.getBeanClassName();
                    Class<?> clazz = Class.forName(className);
                    String enumName = clazz.getSimpleName();
                    enumCache.put(enumName, clazz);
                    log.debug("Registered enum: {} -> {}", enumName, className);
                } catch (Exception e) {
                    log.warn("Failed to register enum from class: {}", candidate.getBeanClassName(), e);
                }
            }

            log.info("Successfully cached {} enums", enumCache.size());

        } catch (Exception e) {
            log.error("Failed to scan and cache enums", e);
        }
    }
}
