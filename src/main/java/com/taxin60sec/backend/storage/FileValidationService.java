package com.taxin60sec.backend.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileValidationService {

    private final StorageProperties storageProperties;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    public void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty.");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new StorageException("Unsupported file type.");
        }

        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new StorageException("Maximum file size exceeded.");
        }
    }

}