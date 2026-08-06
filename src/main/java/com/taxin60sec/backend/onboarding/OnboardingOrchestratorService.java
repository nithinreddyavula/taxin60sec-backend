package com.taxin60sec.backend.onboarding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.document.DocumentAnalysisResult;
import com.taxin60sec.backend.document.DocumentIntelligenceService;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.UploadedDocument;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.entity.enums.DocumentVerificationStatus;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.UploadedDocumentRepository;
import com.taxin60sec.backend.workflow.CaseIntelligenceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Coordinates the existing conversation, OCR, document intelligence and case services as one onboarding flow. */
@Service
@Transactional
public class OnboardingOrchestratorService {
    private final CaseRepository cases;
    private final UploadedDocumentRepository documents;
    private final DocumentIntelligenceService documentIntelligence;
    private final CaseIntelligenceService intelligence;
    private final ObjectMapper objectMapper;

    public OnboardingOrchestratorService(CaseRepository cases, UploadedDocumentRepository documents,
                                         DocumentIntelligenceService documentIntelligence,
                                         CaseIntelligenceService intelligence, ObjectMapper objectMapper) {
        this.cases = cases;
        this.documents = documents;
        this.documentIntelligence = documentIntelligence;
        this.intelligence = intelligence;
        this.objectMapper = objectMapper;
    }

    public OnboardingResponse complete(Long caseId) {
        Case taxCase = caseOf(caseId);
        List<UploadedDocument> uploaded = documents.findByTaxCaseIdAndDeletedFalseOrderByCreatedAtDesc(caseId);
        List<String> validationIssues = executeOcrAndValidation(uploaded);
        CaseIntelligenceService.DocumentChecklist checklist = intelligence.checklist(caseId);
        CaseIntelligenceService.PriceEstimate estimate = intelligence.estimate(caseId);
        CaseIntelligenceService.CaseSummary generated = intelligence.summary(caseId);
        validationIssues.addAll(generated.risks());
        List<ExtractedField> extracted = extractFields(uploaded);
        int confidence = confidence(uploaded, checklist, validationIssues);
        String risk = confidence >= 85 && validationIssues.isEmpty() ? "LOW" : confidence >= 60 ? "MEDIUM" : "HIGH";
        transition(taxCase, checklist, uploaded);
        String action = nextAction(taxCase, checklist);
        String summary = structuredSummary(taxCase, checklist, extracted, validationIssues, confidence, risk, action);
        taxCase.setIntakeSummary(summary);
        taxCase.setAiRiskFlags(String.join(" | ", validationIssues));
        cases.save(taxCase);
        return response(taxCase, uploaded, checklist, extracted, validationIssues, confidence, risk, action,
                generated.followUpQuestions(), estimate, summary);
    }

    @Transactional(readOnly = true)
    public OnboardingResponse summary(Long caseId) {
        Case taxCase = caseOf(caseId);
        List<UploadedDocument> uploaded = documents.findByTaxCaseIdAndDeletedFalseOrderByCreatedAtDesc(caseId);
        CaseIntelligenceService.DocumentChecklist checklist = intelligence.checklist(caseId);
        CaseIntelligenceService.PriceEstimate estimate = intelligence.estimate(caseId);
        List<String> issues = taxCase.getAiRiskFlags() == null || taxCase.getAiRiskFlags().isBlank()
                ? List.of() : List.of(taxCase.getAiRiskFlags().split(" \\| "));
        int confidence = confidence(uploaded, checklist, issues);
        return response(taxCase, uploaded, checklist, extractFields(uploaded), issues, confidence,
                confidence >= 60 ? "MEDIUM" : "HIGH", nextAction(taxCase, checklist),
                intelligence.questions(caseId), estimate, taxCase.getIntakeSummary());
    }

    @Transactional(readOnly = true)
    public CaseIntelligenceService.DocumentChecklist missingDocuments(Long caseId) {
        caseOf(caseId);
        return intelligence.checklist(caseId);
    }

    @Transactional(readOnly = true)
    public CaseIntelligenceService.PriceEstimate pricing(Long caseId) {
        caseOf(caseId);
        return intelligence.estimate(caseId);
    }

    public WorkflowStatus workflow(Long caseId) {
        Case taxCase = caseOf(caseId);
        return new WorkflowStatus(taxCase.getId(), taxCase.getWorkflowStage(), taxCase.getStatus(), taxCase.isPaymentRequired(), taxCase.isDocumentVerificationCompleted());
    }

    private List<String> executeOcrAndValidation(List<UploadedDocument> uploaded) {
        List<String> issues = new ArrayList<>();
        for (UploadedDocument document : uploaded) {
            if (document.getStorageKey() == null || document.getStorageKey().isBlank() || !Files.isRegularFile(Path.of(document.getStorageKey()))) {
                issues.add(document.getOriginalFilename() + " has no readable stored file for OCR.");
                continue;
            }
            DocumentAnalysisResult result = documentIntelligence.analyze(document);
            issues.addAll(result.getIssues());
        }
        return issues;
    }

    private void transition(Case taxCase, CaseIntelligenceService.DocumentChecklist checklist, List<UploadedDocument> uploaded) {
        boolean verified = checklist.missing().isEmpty()
                && uploaded.stream().allMatch(document -> document.getVerificationStatus() == DocumentVerificationStatus.VERIFIED);
        taxCase.setDocumentVerificationCompleted(verified);
        if (!taxCase.isIntakeCompleted()) {
            taxCase.setWorkflowStage(WorkflowStage.DOCUMENTS_PENDING);
            taxCase.setStatus(CaseStatus.INTAKE);
        } else if (!verified) {
            taxCase.setWorkflowStage(uploaded.isEmpty() ? WorkflowStage.DOCUMENTS_PENDING : WorkflowStage.DOCUMENTS_UPLOADED);
            taxCase.setStatus(CaseStatus.DOCUMENT_COLLECTION);
        } else if (taxCase.isPaymentRequired()) {
            taxCase.setWorkflowStage(WorkflowStage.PAYMENT_PENDING);
        } else {
            taxCase.setWorkflowStage(WorkflowStage.UNDER_REVIEW);
            taxCase.setStatus(CaseStatus.CA_REVIEW);
        }
    }

    private List<ExtractedField> extractFields(List<UploadedDocument> uploaded) {
        List<ExtractedField> result = new ArrayList<>();
        for (UploadedDocument document : uploaded) {
            try {
                Map<String, Object> root = objectMapper.readValue(Optional.ofNullable(document.getOcrData()).orElse("{}"), new TypeReference<>() { });
                if (root.get("fields") instanceof Map<?, ?> fields) {
                    for (Map.Entry<?, ?> field : fields.entrySet()) {
                        result.add(new ExtractedField(document.getId(), document.getOriginalFilename(), String.valueOf(field.getKey()), String.valueOf(field.getValue())));
                    }
                }
            } catch (Exception ignored) {
                // A malformed legacy OCR payload must not prevent the rest of onboarding from completing.
            }
        }
        return result;
    }

    private int confidence(List<UploadedDocument> uploaded, CaseIntelligenceService.DocumentChecklist checklist, List<String> issues) {
        double ocr = uploaded.stream().map(UploadedDocument::getOcrConfidence).filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0D);
        return Math.max(0, Math.min(100, (int) Math.round(ocr * 70 + checklist.completionPercentage() * .30 - issues.size() * 5)));
    }

    private String structuredSummary(Case taxCase, CaseIntelligenceService.DocumentChecklist checklist, List<ExtractedField> fields,
                                     List<String> issues, int confidence, String risk, String action) {
        String business = taxCase.getBusinessProfile() == null ? "Not provided" : taxCase.getBusinessProfile().getBusinessName();
        return "Client=" + taxCase.getClient().getFullName() + "; business=" + business + "; service=" + taxCase.getServiceOffering().getDisplayName()
                + "; extractedFields=" + fields.size() + "; missingDocuments=" + String.join(", ", checklist.missing())
                + "; validationIssues=" + String.join(", ", issues) + "; confidence=" + confidence + "% ; risk=" + risk + "; nextAction=" + action;
    }

    private String nextAction(Case taxCase, CaseIntelligenceService.DocumentChecklist checklist) {
        if (!taxCase.isIntakeCompleted()) return "Complete the service conversation.";
        if (!checklist.missing().isEmpty()) return "Upload the remaining mandatory documents.";
        return taxCase.isPaymentRequired() ? "Complete payment to continue." : "Case is ready for CA review.";
    }

    private OnboardingResponse response(Case taxCase, List<UploadedDocument> uploaded, CaseIntelligenceService.DocumentChecklist checklist,
                                        List<ExtractedField> fields, List<String> issues, int confidence, String risk, String action,
                                        List<CaseIntelligenceService.Question> questions, CaseIntelligenceService.PriceEstimate estimate, String summary) {
        BigDecimal recommended = estimate.minimumPrice().add(estimate.maximumPrice()).divide(BigDecimal.valueOf(2));
        return new OnboardingResponse(taxCase.getId(), taxCase.getWorkflowStage(), taxCase.getStatus(), summary,
                new Client(taxCase.getClient().getId(), taxCase.getClient().getFullName(), taxCase.getClient().getEmail()),
                uploaded.stream().map(document -> new UploadedDocumentView(document.getId(), document.getOriginalFilename(), document.getDocumentType(), document.getVerificationStatus().name(), document.getOcrConfidence())).toList(),
                checklist, fields, issues, confidence, risk, action, questions,
                new Pricing(estimate.minimumPrice(), recommended, estimate.maximumPrice(), estimate.reason()));
    }

    private Case caseOf(Long id) {
        return cases.findById(id).filter(caseEntity -> !caseEntity.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Case not found"));
    }

    public record OnboardingResponse(Long caseId, WorkflowStage workflowStage, CaseStatus status, String summary, Client client,
                                     List<UploadedDocumentView> uploadedDocuments, CaseIntelligenceService.DocumentChecklist documents,
                                     List<ExtractedField> extractedFields, List<String> validationIssues, int confidenceScore,
                                     String riskLevel, String suggestedNextAction, List<CaseIntelligenceService.Question> followUpQuestions,
                                     Pricing pricing) { }
    public record Client(Long id, String name, String email) { }
    public record UploadedDocumentView(Long id, String filename, String type, String verificationStatus, Double ocrConfidence) { }
    public record ExtractedField(Long documentId, String documentName, String name, String value) { }
    public record Pricing(BigDecimal minimum, BigDecimal recommended, BigDecimal maximum, String reason) { }
    public record WorkflowStatus(Long caseId, WorkflowStage stage, CaseStatus status, boolean paymentRequired, boolean documentVerificationCompleted) { }
}
