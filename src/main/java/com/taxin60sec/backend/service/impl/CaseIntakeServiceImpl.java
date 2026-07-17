package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.business.CaseIntakeResponse;
import com.taxin60sec.backend.dto.business.CaseRequests;
import com.taxin60sec.backend.dto.business.IntakeRequests;
import com.taxin60sec.backend.dto.domain.RequiredDocumentDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.TimelineEvent;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.CasePriority;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.mapper.CaseMapper;
import com.taxin60sec.backend.mapper.DocumentMapper;
import com.taxin60sec.backend.mapper.TimelineEventMapper;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.ServiceOfferingRepository;
import com.taxin60sec.backend.repository.TimelineEventRepository;
import com.taxin60sec.backend.service.BusinessService;
import com.taxin60sec.backend.service.CaseIntakeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CaseIntakeServiceImpl implements CaseIntakeService {
    private final CaseRepository cases;
    private final ServiceOfferingRepository services;
    private final TimelineEventRepository timeline;
    private final BusinessService business;
    private final CaseMapper caseMapper;
    private final DocumentMapper documentMapper;
    private final TimelineEventMapper timelineMapper;

    public CaseIntakeServiceImpl(CaseRepository cases, ServiceOfferingRepository services, TimelineEventRepository timeline,
                                 BusinessService business, CaseMapper caseMapper, DocumentMapper documentMapper,
                                 TimelineEventMapper timelineMapper) {
        this.cases = cases; this.services = services; this.timeline = timeline; this.business = business;
        this.caseMapper = caseMapper; this.documentMapper = documentMapper; this.timelineMapper = timelineMapper;
    }

    @Override
    public CaseIntakeResponse startOrResume(IntakeRequests.Start request, User client) {
        Case taxCase = cases.findFirstByClientIdAndServiceOfferingIdAndArchivedFalseAndDeletedFalseOrderByUpdatedAtDesc(client.getId(), request.serviceOfferingId())
                .filter(this::canResume)
                .orElseGet(() -> {
                    ServiceOffering service = service(request.serviceOfferingId());
                    String title = request.title() == null || request.title().isBlank()
                            ? service.getDisplayName() + " intake" : request.title().trim();
                    Long caseId = business.createCase(new CaseRequests.Create(title, null, service.getId(), CasePriority.NORMAL, null, null), client).id();
                    return cases.getReferenceById(caseId);
                });
        event(taxCase, client, "INTAKE_RESUMED", "WhatsApp intake started", null);
        return response(taxCase);
    }

    @Override
    public CaseIntakeResponse recordAnswers(Long caseId, IntakeRequests.Answers request, User actor) {
        Case taxCase = taxCase(caseId);
        Map<String, String> answers = decode(taxCase.getIntakeAnswers());
        request.answers().forEach(answer -> answers.put(answer.question().trim(), answer.answer().trim()));
        taxCase.setIntakeAnswers(encode(answers));
        if (request.complete()) {
            taxCase.setIntakeCompleted(true);
            taxCase.setIntakeSummary(summary(taxCase, answers));
            if (taxCase.getWorkflowStage() == WorkflowStage.CREATED) {
                business.stage(caseId, taxCase.isPaymentRequired() ? WorkflowStage.PAYMENT_PENDING : WorkflowStage.DOCUMENTS_PENDING, actor);
            }
            event(taxCase, actor, "INTAKE_COMPLETED", "AI intake summary prepared for CA review", null);
        } else {
            event(taxCase, actor, "INTAKE_ANSWER_RECORDED", "Customer provided intake information", null);
        }
        return response(taxCase);
    }

    @Override
    @Transactional(readOnly = true)
    public CaseIntakeResponse review(Long caseId) { return response(taxCase(caseId)); }

    private CaseIntakeResponse response(Case taxCase) {
        User client = taxCase.getClient();
        return new CaseIntakeResponse(caseMapper.toDto(taxCase), client.getFullName(), client.getEmail(), client.getPhoneNumber(),
                taxCase.getServiceOffering().getDisplayName(), questions(taxCase.getServiceOffering()), decode(taxCase.getIntakeAnswers()),
                business.documents(taxCase.getId()), business.missing(taxCase.getId()), business.timeline(taxCase.getId()));
    }

    private boolean canResume(Case taxCase) { return !taxCase.isIntakeCompleted() || taxCase.getWorkflowStage() == WorkflowStage.CLIENT_ACTION_REQUIRED; }
    private Case taxCase(Long id) { return cases.findById(id).filter(c -> !c.isDeleted()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Case not found")); }
    private ServiceOffering service(Long id) { return services.findById(id).filter(s -> !s.isDeleted() && s.isActive()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Active service not found")); }
    private List<String> questions(ServiceOffering service) { return service.getIntakeQuestions() == null || service.getIntakeQuestions().isBlank() ? List.of("Please describe what you need help with.") : service.getIntakeQuestions().lines().filter(q -> !q.isBlank()).toList(); }
    private String summary(Case taxCase, Map<String, String> answers) { String facts = answers.isEmpty() ? "No answers recorded." : answers.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).reduce((a, b) -> a + "; " + b).orElse(""); return "AI-assisted intake draft for CA review. Service: " + taxCase.getServiceOffering().getDisplayName() + ". Customer: " + taxCase.getClient().getFullName() + ". Details: " + facts; }
    private void event(Case taxCase, User actor, String type, String title, String description) { TimelineEvent event = new TimelineEvent(); event.setTaxCase(taxCase); event.setActor(actor); event.setEventType(type); event.setTitle(title); event.setDescription(description); timeline.save(event); }
    private String encode(Map<String, String> values) { return values.entrySet().stream().map(e -> token(e.getKey()) + ":" + token(e.getValue())).reduce((a, b) -> a + "\n" + b).orElse(""); }
    private Map<String, String> decode(String raw) { Map<String, String> values = new LinkedHashMap<>(); if (raw == null || raw.isBlank()) return values; for (String line : raw.split("\\r?\\n")) { String[] pair = line.split(":", 2); if (pair.length == 2) values.put(untoken(pair[0]), untoken(pair[1])); } return values; }
    private String token(String value) { return Base64.getUrlEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
    private String untoken(String value) { return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8); }
}
