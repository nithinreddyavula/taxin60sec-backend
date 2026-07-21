package com.taxin60sec.backend.whatsapp;

import com.taxin60sec.backend.workflow.CaseIntelligenceService;
import lombok.RequiredArgsConstructor;
import com.taxin60sec.backend.whatsapp.WhatsappDocumentUploadAdapter;
import com.taxin60sec.backend.document.DocumentMatcherService;
import com.taxin60sec.backend.document.DocumentService;
import com.taxin60sec.backend.repository.RequiredDocumentRepository;
import org.springframework.stereotype.Service;
import com.taxin60sec.backend.document.DocumentUploadRequest;
import com.taxin60sec.backend.entity.RequiredDocument;

@Service
@RequiredArgsConstructor
public class WhatsappDocumentServiceImpl implements WhatsappDocumentService {

    private final WhatsappMediaService whatsappMediaService;
    private final DocumentService documentService;
    private final CaseIntelligenceService caseIntelligenceService;
    private final DocumentMatcherService documentMatcherService;
private final RequiredDocumentRepository requiredDocumentRepository;
private final WhatsappDocumentUploadAdapter uploadAdapter;


    @Override
    public String processDocument(Long caseId,
                                  WhatsappMessage message) {

        byte[] bytes = whatsappMediaService.downloadMedia(
        message.getMediaId()
);

DocumentUploadRequest request =
        uploadAdapter.toUploadRequest(
                caseId,
                null,
                bytes,
                message.getFileName(),
                message.getMimeType()
        );

String documentType =
        documentMatcherService.detectDocumentType(
                request.getFile()
        );

RequiredDocument requiredDocument =
        requiredDocumentRepository
                .findByTaxCaseIdAndDocumentTypeIgnoreCase(
                        caseId,
                        documentType
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unexpected document."
                        ));

request.setRequiredDocumentId(
        requiredDocument.getId()
);

documentService.upload(request);

        // Step 3
        var checklist =
                caseIntelligenceService.checklist(caseId);

        if (checklist.missing().isEmpty()) {

            return """
                    ✅ All required documents received.

                    Your case has been assigned to one of our CA experts.
                    """;
        }

        StringBuilder builder = new StringBuilder();

        builder.append("✅ Document received.\n\n");
        builder.append("Remaining documents:\n\n");

        checklist.missing()
        .forEach(doc ->
                builder.append("• ")
                       .append(doc)
                       .append("\n"));

        return builder.toString();
    }
}