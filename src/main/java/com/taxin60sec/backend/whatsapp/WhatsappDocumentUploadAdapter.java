package com.taxin60sec.backend.whatsapp;

import com.taxin60sec.backend.document.DocumentUploadRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class WhatsappDocumentUploadAdapter {

    public DocumentUploadRequest toUploadRequest(
        Long caseId,
        Long requiredDocumentId,
        byte[] fileBytes,
        String fileName,
        String mimeType
) {

        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                fileName,
                mimeType,
                fileBytes
        );

        DocumentUploadRequest request = new DocumentUploadRequest();

        request.setCaseId(caseId);
request.setRequiredDocumentId(requiredDocumentId);
request.setFile(multipartFile);

        return request;
    }
}