package com.taxin60sec.backend.document;

import com.taxin60sec.backend.storage.StorageException;
import com.taxin60sec.backend.document.DocumentAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import com.taxin60sec.backend.repository.UploadedDocumentRepository;
import com.taxin60sec.backend.entity.UploadedDocument;
import com.taxin60sec.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DocumentDownloadServiceImpl implements DocumentDownloadService {

    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final DocumentAccessService accessService;

    @Override
public Resource download(Long uploadedDocumentId,
                         UserPrincipal principal) {

    UploadedDocument document =
            uploadedDocumentRepository.findById(uploadedDocumentId)
                    .orElseThrow(() ->
                            new StorageException("Document not found."));

    accessService.verifyAccess(principal, document);

    Path path = Path.of(document.getStorageKey());

    if (!Files.exists(path)) {
        throw new StorageException("Stored file not found.");
    }

    return new PathResource(path);
}

}