package com.taxin60sec.backend.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.entity.UploadedDocument;
import com.taxin60sec.backend.entity.enums.DocumentVerificationStatus;
import com.taxin60sec.backend.ocr.OcrFieldExtractor;
import com.taxin60sec.backend.ocr.OcrProvider;
import com.taxin60sec.backend.ocr.OcrResult;
import com.taxin60sec.backend.repository.UploadedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentIntelligenceService {
    private final UploadedDocumentRepository documents;
    private final List<OcrProvider> providers;
    private final ObjectMapper objectMapper;

    @Transactional
    public DocumentAnalysisResult analyze(UploadedDocument document) {
        List<String> issues = new ArrayList<>();
        Path file = Path.of(document.getStorageKey());
        int pages = 1;
        boolean encrypted = false;
        boolean corrupted = false;
        String text = "";
        try {
            if ("application/pdf".equalsIgnoreCase(document.getMimeType())) {
                try (PDDocument pdf = Loader.loadPDF(file.toFile())) { pages = pdf.getNumberOfPages(); encrypted = pdf.isEncrypted(); }
                if (encrypted) issues.add("PDF is encrypted and cannot be validated automatically.");
            }
        } catch (Exception ex) { corrupted = true; issues.add("Document is corrupt or cannot be read."); }
        final int detectedPages = pages;
        OcrResult ocr = providers.stream().filter(p -> p.supports(document.getMimeType())).findFirst().map(p -> p.extract(file, document.getMimeType(), detectedPages)).orElse(OcrResult.empty("none", detectedPages));
        text = ocr.text();
        Map<String, String> fields = ocr.fields().isEmpty() ? OcrFieldExtractor.fields(text) : ocr.fields();
        DocumentType expected = DocumentType.from(document.getDocumentType());
        DocumentType classified = classify(expected, fields, text);
        if (expected != DocumentType.OTHER && classified != expected) issues.add("Content does not match expected document type " + expected + ".");
        requiredFields(expected, fields, issues);
        boolean duplicate = documents.findFirstBySha256HashAndDeletedFalse(document.getSha256Hash()).filter(existing -> !existing.getId().equals(document.getId())).isPresent();
        if (duplicate) issues.add("An identical document was uploaded previously.");
        boolean ownership = ownership(document, fields, text);
        if (!ownership) issues.add("Document ownership could not be established from the available identity data.");
        if (document.getExpiresAt() != null && document.getExpiresAt().isBefore(Instant.now())) issues.add("Document has expired.");
        int quality = quality(document, ocr);
        if (quality < 35) issues.add("Image quality is too low for reliable validation.");
        document.setPageCount(pages); document.setEncrypted(encrypted); document.setCorrupted(corrupted); document.setDuplicateFile(duplicate);
        document.setClassifiedDocumentType(classified.name()); document.setClassificationConfidence(classified == expected ? .92D : .45D);
        document.setOcrProvider(ocr.provider()); document.setOcrConfidence(ocr.confidence()); document.setImageQualityScore(quality); document.setOwnershipValidated(ownership);
        document.setOcrData(json(Map.of("text", text, "fields", fields, "tables", ocr.tables()))); document.setValidationIssues(String.join(" | ", issues));
        boolean valid = !corrupted && !encrypted && !duplicate && quality >= 35 && mandatoryValid(expected, fields) && (expected == DocumentType.OTHER || classified == expected);
        document.setVerificationStatus(valid ? DocumentVerificationStatus.VERIFIED : DocumentVerificationStatus.PENDING);
        if (valid) document.setVerifiedAt(Instant.now());
        return result(valid, document, issues, fields);
    }
    private DocumentAnalysisResult result(boolean valid, UploadedDocument d, List<String> issues, Map<String,String> fields) { DocumentAnalysisResult r = new DocumentAnalysisResult(); r.setValid(valid); r.setConfidence(Optional.ofNullable(d.getOcrConfidence()).orElse(0D)); r.setDocumentType(d.getClassifiedDocumentType()); r.setIssues(issues); r.setSummary(valid ? "Automated document validation passed." : "Automated validation requires review."); r.setPageCount(Optional.ofNullable(d.getPageCount()).orElse(0)); r.setDuplicate(d.isDuplicateFile()); r.setCorrupted(d.isCorrupted()); r.setEncrypted(d.isEncrypted()); r.setOwnershipValidated(d.isOwnershipValidated()); r.setExtractedFields(fields); return r; }
    private DocumentType classify(DocumentType expected, Map<String,String> fields, String text) { if (fields.containsKey("gstin")) return DocumentType.GST_CERTIFICATE; if (fields.containsKey("pan")) return DocumentType.PAN; if (fields.containsKey("aadhaar")) return DocumentType.AADHAAR; if (fields.containsKey("invoiceNumber")) return DocumentType.INVOICE; return expected; }
    private void requiredFields(DocumentType type, Map<String,String> f, List<String> issues) { if (!mandatoryValid(type, f)) issues.add("Mandatory identifying fields were not found."); }
    private boolean mandatoryValid(DocumentType type, Map<String,String> f) { return switch(type) { case PAN -> f.containsKey("pan"); case AADHAAR -> f.containsKey("aadhaar"); case GST_CERTIFICATE -> f.containsKey("gstin"); case CANCELLED_CHEQUE -> f.containsKey("ifsc") && f.containsKey("accountNumber"); case INVOICE -> f.containsKey("invoiceNumber") || f.containsKey("amount"); default -> true; }; }
    private boolean ownership(UploadedDocument d, Map<String,String> f, String text) { if (d.getTaxCase() == null || d.getTaxCase().getClient() == null) return false; String name = d.getTaxCase().getClient().getFullName(); return name == null || name.isBlank() || text.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT)) || f.containsKey("pan") || f.containsKey("gstin"); }
    private int quality(UploadedDocument d, OcrResult ocr) { if (!d.getMimeType().startsWith("image/")) return 100; if (d.getFileSize() < 10_000) return 20; return ocr.confidence() > 0 ? 75 : 45; }
    private String json(Object value) { try { return objectMapper.writeValueAsString(value); } catch (JsonProcessingException e) { return "{}"; } }
}
