package com.taxin60sec.backend.document;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.taxin60sec.backend.repository.UploadedDocumentRepository;
import com.taxin60sec.backend.entity.UploadedDocument;
import org.springframework.web.bind.annotation.*;
import com.taxin60sec.backend.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentDownloadController {

    private final DocumentDownloadService downloadService;
    private final UploadedDocumentRepository uploadedDocumentRepository;

   @GetMapping("/{documentId}/download")
public ResponseEntity<Resource> download(
        @PathVariable Long documentId,
        @AuthenticationPrincipal UserPrincipal principal) {

    UploadedDocument document =
            uploadedDocumentRepository.findById(documentId)
                    .orElseThrow();

    Resource resource =
            downloadService.download(documentId, principal);

    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(document.getMimeType()))
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" +
                            document.getOriginalFilename() + "\""
            )
            .body(resource);
}

}