package com.taxin60sec.backend.document;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void upload(DocumentUploadRequest request) {

        documentService.upload(request);

    }

    @GetMapping("/validate/{caseId}")
    public DocumentValidationResult validate(
            @PathVariable Long caseId) {

        return documentService.validate(caseId);

    }

}