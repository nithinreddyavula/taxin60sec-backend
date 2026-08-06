package com.taxin60sec.backend.controller;
import com.taxin60sec.backend.workflow.CaseIntelligenceService;
import com.taxin60sec.backend.entity.UploadedDocument;
import com.taxin60sec.backend.repository.UploadedDocumentRepository;
import com.taxin60sec.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/cases/{caseId}/intelligence") @RequiredArgsConstructor
public class CaseIntelligenceController {
 private final CaseIntelligenceService intelligence; private final UploadedDocumentRepository documents;
 @GetMapping("/documents") public CaseIntelligenceService.DocumentChecklist checklist(@PathVariable Long caseId){return intelligence.checklist(caseId);} @GetMapping("/questions") public java.util.List<CaseIntelligenceService.Question> questions(@PathVariable Long caseId){return intelligence.questions(caseId);} @PostMapping("/price") public CaseIntelligenceService.PriceEstimate price(@PathVariable Long caseId){return intelligence.estimate(caseId);} @PostMapping("/summary") public CaseIntelligenceService.CaseSummary summary(@PathVariable Long caseId){return intelligence.summary(caseId);} @GetMapping("/classification/{documentId}") public Classification classification(@PathVariable Long caseId,@PathVariable Long documentId){UploadedDocument d=documents.findById(documentId).filter(v->v.getTaxCase()!=null&&v.getTaxCase().getId().equals(caseId)).orElseThrow();return new Classification(d.getId(),d.getClassifiedDocumentType(),d.getClassificationConfidence(),d.getOcrConfidence(),d.getOcrData());} @PostMapping("/review") public CaseIntelligenceService.ReviewResult review(@PathVariable Long caseId,@RequestBody CaseIntelligenceService.ReviewCommand command,@AuthenticationPrincipal UserPrincipal actor){return intelligence.review(caseId,command,actor.getUser());} public record Classification(Long documentId,String documentType,Double confidence,Double ocrConfidence,String extractedData){}
}
