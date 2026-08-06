package com.taxin60sec.backend.storage;

/**
 * Result of persisting an uploaded file to storage.
 *
 * @param path          the storage key / on-disk path the file was written to
 * @param originalName  the original filename supplied by the client
 * @param contentType   the MIME type of the uploaded file
 * @param size          size of the stored file in bytes
 * @param sha256        SHA-256 hex digest of the file contents, used for de-duplication/integrity checks
 */
public record StoredFile(
        String path,
        String originalName,
        String contentType,
        long size,
        String sha256
) {

    public String getPath() {
        return path;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public String getSha256() {
        return sha256;
    }
}