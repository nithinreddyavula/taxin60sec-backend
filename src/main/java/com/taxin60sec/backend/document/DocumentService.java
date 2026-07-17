package com.taxin60sec.backend.document;

public interface DocumentService {

    DocumentValidationResult validate(Long caseId);

    void upload(DocumentUploadRequest request);

}