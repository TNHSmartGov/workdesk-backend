package com.tnh.baseware.core.services.storage.impl;

import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.storage.IStorageService;
import io.minio.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service("s3StorageService")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class S3StorageService implements IStorageService<String> {

    String bucketPrefix;

    MinioClient minioClient;
    MessageService messageService;

    public S3StorageService(@Value("${minio.bucket-prefix:workdesk-}") String bucketPrefix, MinioClient minioClient,
            MessageService messageService) {
        this.bucketPrefix = bucketPrefix;
        this.minioClient = minioClient;
        this.messageService = messageService;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String bucketName = initBucket();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String path = LocalDate.now().format(formatter) + "/" + UUID.randomUUID() + "_"
                    + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return path;
        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new BWCGenericRuntimeException(messageService.getMessage("file.upload.error"));
        }
    }

    @Override
    public InputStream downloadFile(String path) {
        try {
            String bucketName = initBucket();
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
        } catch (Exception e) {
            throw new BWCGenericRuntimeException(messageService.getMessage("file.download.error"));
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            String bucketName = initBucket();
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
        } catch (Exception e) {
            throw new BWCGenericRuntimeException(messageService.getMessage("file.delete.error"));
        }
    }

    private String initBucket() {
        String bucketName = bucketPrefix + "default"; // Or any static name
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new BWCGenericRuntimeException("Init bucket failed: " + bucketName);
        }
        return bucketName;
    }
}
