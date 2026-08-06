package com.taxin60sec.backend.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over the underlying object storage provider.
 * Storage, notifications, payments, AI, and workflow orchestration are all
 * expected to change over time, so callers depend on this interface rather
 * than a concrete implementation (see docs/ARCHITECTURE.md).
 */
public interface StorageService {

    /**
     * Uploads a file and returns the metadata needed to reference it later.
     *
     * @param file     the multipart file to persist
     * @param isPublic whether the stored object should be publicly readable
     * @return metadata describing where the file was stored
     */
    StorageMetadata upload(MultipartFile file, boolean isPublic);

    /**
     * Removes a previously stored object.
     *
     * @param key the storage key returned by {@link #upload}
     */
    void delete(String key);

    /**
     * Metadata describing a stored object.
     *
     * @param key     the storage key/path the object was written to
     * @param version an opaque version identifier for the stored object
     */
    record StorageMetadata(String key, String version) {
    }
}