package com.taxin60sec.backend.storage;

public interface StorageService {
    String createUploadUrl(String objectKey, String contentType);

    String createDownloadUrl(String objectKey);

    void delete(String objectKey);
}
