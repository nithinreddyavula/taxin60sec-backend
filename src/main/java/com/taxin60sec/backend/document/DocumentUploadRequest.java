package com.taxin60sec.backend.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class DocumentUploadRequest {

    private Long caseId;

    private Long requiredDocumentId;

    private MultipartFile file;

}