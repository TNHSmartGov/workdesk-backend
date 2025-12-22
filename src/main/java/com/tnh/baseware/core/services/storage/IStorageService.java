package com.tnh.baseware.core.services.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface IStorageService<D> {
    D uploadFile(MultipartFile file);
    InputStream downloadFile(String path);
    void deleteFile(String path);
}
