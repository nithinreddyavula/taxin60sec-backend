package com.taxin60sec.backend.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class SecureLocalStorageService {

    private final StorageProperties storageProperties;
    private final FileValidationService fileValidationService;
    private final FileNameGenerator fileNameGenerator;
    private final HashService hashService;

    public StoredFile store(MultipartFile file,
                            Long caseId,
                            String documentType) {

        fileValidationService.validate(file);

        try {

            Path root = Paths.get(storageProperties.getLocation());

            Path directory = root
                    .resolve("case-" + caseId)
                    .resolve(documentType);

            Files.createDirectories(directory);

            String storedName =
                    fileNameGenerator.generate(file.getOriginalFilename());

            Path destination = directory.resolve(storedName);

            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination);
            }

            String sha256;

            try (InputStream input = Files.newInputStream(destination)) {
                sha256 = hashService.sha256(input);
            }

            return StoredFile.builder()
                    .originalName(file.getOriginalFilename())
                    .storedName(storedName)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .sha256(sha256)
                    .path(destination.toString())
                    .build();

        } catch (IOException ex) {
            throw new StorageException("Unable to store file.", ex);
        }

    }

}
