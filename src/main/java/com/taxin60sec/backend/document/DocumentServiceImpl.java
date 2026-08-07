package com.taxin60sec.backend.document;

import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.RequiredDocument;
import com.taxin60sec.backend.entity.UploadedDocument;
import com.taxin60sec.backend.entity.enums.DocumentVerificationStatus;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.RequiredDocumentRepository;
import com.taxin60sec.backend.repository.UploadedDocumentRepository;
import com.taxin60sec.backend.storage.StoredFile;

import com.taxin60sec.backend.ai.AiDocumentValidator;
import com.taxin60sec.backend.audit.AuditContracts;
import com.taxin60sec.backend.storage.SecureLocalStorageService;
import com.taxin60sec.backend.document.DocumentAnalysisResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    /** Stages from which it's safe to auto-advance to DOCUMENTS_UPLOADED. A case that's
     * already moved past this point (CA reviewing, filed, etc.) should never be pulled
     * backwards just because validate() ran again on a re-upload. */
    private static final Set<WorkflowStage> ADVANCEABLE_TO_DOCUMENTS_UPLOADED =
            EnumSet.of(WorkflowStage.CREATED, WorkflowStage.CA_ASSIGNED, WorkflowStage.DOCUMENTS_PENDING);

    private final CaseRepository caseRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final RequiredDocumentRepository requiredDocumentRepository;
    private final AiDocumentValidator aiDocumentValidator;
    private final SecureLocalStorageService secureLocalStorageService;
    private final AuditContracts.AuditService audit;

    @Override
    public void upload(DocumentUploadRequest request) {

        Case taxCase = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Case not found : " + request.getCaseId()));

        RequiredDocument requiredDocument = requiredDocumentRepository.findById(request.getRequiredDocumentId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Required document not found : " + request.getRequiredDocumentId()));

        if (requiredDocument.getTaxCase() != null && !requiredDocument.getTaxCase().getId().equals(taxCase.getId())) {
            throw new IllegalArgumentException("Required document does not belong to the supplied case.");
        }

        UploadedDocument uploadedDocument = new UploadedDocument();

        uploadedDocument.setTaxCase(taxCase);
        uploadedDocument.setRequiredDocument(requiredDocument);

        StoredFile storedFile = secureLocalStorageService.store(
                request.getFile(),
                taxCase.getId(),
                requiredDocument.getDocumentType()
        );

        uploadedDocument.setSha256Hash(storedFile.getSha256());

        uploadedDocument.setStorageKey(storedFile.getPath());
        uploadedDocument.setOriginalFilename(storedFile.getOriginalName());
        uploadedDocument.setMimeType(storedFile.getContentType());
        uploadedDocument.setFileSize(storedFile.getSize());

        uploadedDocument.setDocumentType(requiredDocument.getDocumentType());

        uploadedDocument.setVerificationStatus(
                DocumentVerificationStatus.PENDING
        );

        uploadedDocumentRepository
                .findTopByTaxCaseIdAndDocumentTypeAndDeletedFalseOrderByVersionNumberDesc(
                        taxCase.getId(),
                        requiredDocument.getDocumentType())
                .ifPresent(previous ->
                        uploadedDocument.setVersionNumber(
                                previous.getVersionNumber() + 1));

        uploadedDocumentRepository.saveAndFlush(uploadedDocument);
        aiDocumentValidator.validate(uploadedDocument);
        uploadedDocumentRepository.save(uploadedDocument);
        audit.record(new AuditContracts.AuditEvent("DOCUMENT_UPLOADED", "DOCUMENT", String.valueOf(uploadedDocument.getId()), null, java.time.Instant.now(), java.util.Map.of("caseId", String.valueOf(taxCase.getId()), "type", requiredDocument.getDocumentType())));

        validate(taxCase.getId());
    }
    @Override
    @Transactional(readOnly = true)
    public DocumentValidationResult validate(Long caseId) {

        Case taxCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Case not found : " + caseId));

        List<RequiredDocument> requiredDocuments =
                requiredDocumentRepository
                        .findByTaxCaseIdAndDeletedFalseOrderByDisplayOrderAsc(caseId);

        if (requiredDocuments.isEmpty() && taxCase.getServiceOffering() != null) {

            requiredDocuments = requiredDocumentRepository
                    .findByServiceOfferingIdAndDeletedFalseOrderByDisplayOrderAsc(
                            taxCase.getServiceOffering().getId()
                    );
        }

        StringBuilder missingDocuments = new StringBuilder();

        boolean valid = true;

        // Tracks whether every document on the checklist - mandatory or not - actually
        // has a file against it. This is the same signal the client-facing "Documents
        // Summary" widget uses, and it's what the workflow stage should reflect. Using
        // only the mandatory subset here was the bug: a service whose checklist is
        // entirely optional (see PublicIntakeServiceImpl.createDefaultRequiredDocuments)
        // would satisfy "no mandatory doc missing" trivially, before a single file was
        // uploaded.
        boolean allDocumentsUploaded = !requiredDocuments.isEmpty();

        for (RequiredDocument requiredDocument : requiredDocuments) {

            boolean uploaded = uploadedDocumentRepository
                    .findTopByTaxCaseIdAndDocumentTypeAndDeletedFalseOrderByVersionNumberDesc(
                            caseId,
                            requiredDocument.getDocumentType())
                    .isPresent();

            if (!uploaded) {
                allDocumentsUploaded = false;
            }

            if (!requiredDocument.isMandatory()) {
                continue;
            }

            boolean verified = uploadedDocumentRepository
                    .findTopByTaxCaseIdAndDocumentTypeAndDeletedFalseOrderByVersionNumberDesc(
                            caseId,
                            requiredDocument.getDocumentType())
                    .filter(document -> document.getVerificationStatus() == DocumentVerificationStatus.VERIFIED)
                    .isPresent();

            if (!verified) {

                valid = false;

                if (!missingDocuments.isEmpty()) {
                    missingDocuments.append(", ");
                }

                missingDocuments.append(requiredDocument.getName());
            }
        }

        if (allDocumentsUploaded && ADVANCEABLE_TO_DOCUMENTS_UPLOADED.contains(taxCase.getWorkflowStage())) {

            taxCase.setDocumentVerificationCompleted(valid);

            taxCase.setWorkflowStage(
                    WorkflowStage.DOCUMENTS_UPLOADED
            );

            caseRepository.save(taxCase);
        }

        if (valid) {

            return new DocumentValidationResult(
                    true,
                    "All required documents uploaded successfully."
            );
        }

        return new DocumentValidationResult(
                false,
                "Missing required documents: " + missingDocuments
        );
    }
}