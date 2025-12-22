package com.tnh.baseware.core.dtos.doc;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;

@Getter
@Setter
public class FileResource implements AutoCloseable {
    private final InputStream inputStream;
    private final String fileName;
    private final String contentType;

    public FileResource(InputStream inputStream, String fileName, String contentType) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
