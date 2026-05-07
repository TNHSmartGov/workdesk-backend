package com.tnh.baseware.core.configs;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.end-point:http://127.0.0.1:9000}")
    private String minioEndpoint;

    @Value("${minio.security.access-key:}")
    private String minioAccessKey;

    @Value("${minio.security.secret-key:}")
    private String minioSecretKey;

    @Bean
    public MinioClient minioClient() {
        System.out.println("Connecting to MinIO at: " + minioEndpoint);
        return MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
    }
}
