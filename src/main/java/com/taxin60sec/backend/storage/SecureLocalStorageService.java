package com.taxin60sec.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Local-disk implementation of {@link StorageService}. Files are written under
 * a configurable base directory, keyed by a random UUID so stored filenames never
 * collide and never leak the original filename on disk.
 * <p>
 * This is the current production storage backend; it is intentionally hidden
 * behind {@link StorageService} so it can be swapped for an S3-backed
 * implementation later without touching callers.
 */
@Service
public class SecureLocalStorageService implements StorageService {

    private final Path basePath;

    public SecureLocalStorageService(
            @Value("${storage.local.base-path:${STORAGE_LOCAL_PATH:./storage-data}}") String basePath) {
        this.basePath = Path.of(basePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            throw new StorageException("Unable to initialize local storage directory: " + this.basePath, e);
        }
    }

    /**
     * Stores an uploaded document for a case, namespaced by case id and document type.
     */
    public StoredFile store(MultipartFile file, Long caseId, String documentType) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Cannot store an empty file.");
        }

        String safeDocumentType = documentType == null ? "GENERAL" : documentType.replaceAll("[^A-Za-z0-9_-]", "_");
        Path directory = basePath.resolve("cases").resolve(String.valueOf(caseId)).resolve(safeDocumentType);

        try {
            Files.createDirectories(directory);

            String extension = extensionOf(file.getOriginalFilename());
            String storedFilename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
            Path destination = directory.resolve(storedFilename);

            String sha256;
            try (InputStream inputStream = file.getInputStream()) {
                sha256 = sha256Hex(inputStream, destination);
            }

            return new StoredFile(
                    basePath.relativize(destination).toString(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    Files.size(destination),
                    sha256
            );
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public StorageMetadata upload(MultipartFile file, boolean isPublic) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Cannot store an empty file.");
        }

        String scope = isPublic ? "public" : "private";
        Path directory = basePath.resolve(scope);

        try {
            Files.createDirectories(directory);

            String extension = extensionOf(file.getOriginalFilename());
            String storedFilename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
            Path destination = directory.resolve(storedFilename);

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            String key = basePath.relativize(destination).toString();
            String version = String.valueOf(Instant.now().toEpochMilli());

            return new StorageMetadata(key, version);
        } catch (IOException e) {
            throw new StorageException("Failed to upload file.", e);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(basePath.resolve(key));
        } catch (IOException e) {
            throw new StorageException("Failed to delete stored file: " + key, e);
        }
    }

    private static String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex + 1).replaceAll("[^A-Za-z0-9]", "");
    }

    private static String sha256Hex(InputStream inputStream, Path destination) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var digestStream = new java.security.DigestInputStream(inputStream, digest)) {
                Files.copy(digestStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new StorageException("SHA-256 algorithm not available.", e);
        }
    }
}