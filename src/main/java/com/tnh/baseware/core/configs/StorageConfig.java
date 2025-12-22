package com.tnh.baseware.core.configs;

import com.tnh.baseware.core.services.storage.IStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class StorageConfig {
    @Value("${storage.strategy}")
    private String strategy;

    private final Map<String, IStorageService<String>> strategies;

    public StorageConfig(Map<String, IStorageService<String>> strategies) {
        this.strategies = strategies;
    }

    @Bean
    public IStorageService<String> storageService() {
        return strategies.get(strategy);
    }
}
