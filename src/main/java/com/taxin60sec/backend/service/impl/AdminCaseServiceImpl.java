package com.taxin60sec.backend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.dto.admin.AdminCaseSummaryResponse;
import com.taxin60sec.backend.dto.admin.AdminDashboardResponse;
import com.taxin60sec.backend.dto.admin.AssignableCaResponse;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.entity.enums.NoticeSeverity;
import com.taxin60sec.backend.entity.enums.NoticeType;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.UserRepository;
import com.taxin60sec.backend.service.AdminCaseService;
import com.taxin60sec.backend.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminCaseServiceImpl implements AdminCaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final NoticeService noticeService;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public AdminCaseServiceImpl(
            CaseRepository caseRepository,
            UserRepository userRepository,
            NoticeService noticeService,
            ObjectMapper objectMapper,
            NotificationService notificationService
    ) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.noticeService = noticeService;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @Override
    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(
                caseRepository.count(),
                caseRepository.countByStatus(CaseStatus.DRAFT),
                caseRepository.countByStatus(CaseStatus.INTAKE),
                caseRepository.countByStatus(CaseStatus.DOCUMENT_COLLECTION),
                caseRepository.countByStatus(CaseStatus.CA_REVIEW),
                caseRepository.countByStatus(CaseStatus.IN_PROGRESS),
                caseRepository.countByStatus(CaseStatus.COMPLETED),
                caseRepository.countByStatus(CaseStatus.CANCELLED)
        );
    }

    @Override
    public List<AdminCaseSummaryResponse> getAllCases() {
        return caseRepository.findAllByOrderByCreatedAtDesc().stream().map(this::map).toList();
    }

    private AdminCaseSummaryResponse map(Case taxCase) {
        List<String> questions = List.of();

        if (taxCase.getServiceOffering() != null &&
                taxCase.getServiceOffering().getIntakeQuestions() != null &&
                !taxCase.getServiceOffering().getIntakeQuestions().isBlank()) {
            questions = Arrays.stream(taxCase.getServiceOffering().getIntakeQuestions().split("\\n"))
                    .map(String::trim)
                    .filter(q -> !q.isBlank())
                    .toList();
        }

        Map<String, String> answers = readAnswers(taxCase);

        return new AdminCaseSummaryResponse(
                taxCase.getId(),
                taxCase.getClient() != null ? taxCase.getClient().getFullName() : "N/A",
                taxCase.getServiceOffering() != null ? taxCase.getServiceOffering().getDisplayName() : "N/A",
                taxCase.getStatus().name(),
                taxCase.isIntakeCompleted(),
                answers.size(),
                questions.size(),
                taxCase.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                taxCase.getAssignedCa() != null ? taxCase.getAssignedCa().getId() : null,
                taxCase.getAssignedCa() != null ? taxCase.getAssignedCa().getFullName() : null
        );
    }

    private Map<String, String> readAnswers(Case taxCase) {
        try {
            if (taxCase.getIntakeAnswers() == null || taxCase.getIntakeAnswers().isBlank()) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(taxCase.getIntakeAnswers(), new TypeReference<LinkedHashMap<String, String>>() {});
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public com.taxin60sec.backend.dto.admin.AdminCaseDetailResponse getCase(Long caseId) {
        Case taxCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new com.taxin60sec.backend.exception.ApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        com.taxin60sec.backend.common.ApiErrorCode.NOT_FOUND,
                        "Case not found"
                ));

        Map<String, String> answers = readAnswers(taxCase);

        return new com.taxin60sec.backend.dto.admin.AdminCaseDetailResponse(
                taxCase.getId(),
                taxCase.getClient() != null ? taxCase.getClient().getFullName() : "N/A",
                taxCase.getClient() != null ? taxCase.getClient().getEmail() : null,
                taxCase.getClient() != null ? taxCase.getClient().getPhoneNumber() : null,
                taxCase.getServiceOffering() != null ? taxCase.getServiceOffering().getDisplayName() : "N/A",
                taxCase.getStatus().name(),
                taxCase.isIntakeCompleted(),
                answers,
                taxCase.getIntakeSummary(),
                taxCase.getAssignedCa() != null ? taxCase.getAssignedCa().getId() : null,
                taxCase.getAssignedCa() != null ? taxCase.getAssignedCa().getFullName() : null
        );
    }

    @Override
    public void updateStatus(Long caseId, String status) {
        Case taxCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new com.taxin60sec.backend.exception.ApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        com.taxin60sec.backend.common.ApiErrorCode.NOT_FOUND,
                        "Case not found"
                ));

        CaseStatus target;
        try {
            target = CaseStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new com.taxin60sec.backend.exception.ApiException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    com.taxin60sec.backend.common.ApiErrorCode.BAD_REQUEST,
                    "Unknown status: " + status
            );
        }

        CaseStatus previous = taxCase.getStatus();
        taxCase.setStatus(target);

        if (target != previous) {
            notifyClientOfStatusChange(taxCase, target);
        }
    }

    private void notifyClientOfStatusChange(Case taxCase, CaseStatus status) {
        if (taxCase.getClient() == null) return;

        String serviceName = taxCase.getServiceOffering() != null ? taxCase.getServiceOffering().getDisplayName() : "Your case";

        String title;
        String message;
        NoticeSeverity severity = NoticeSeverity.INFO;

        switch (status) {
            case CA_REVIEW -> {
                title = "Your case moved to CA review";
                message = serviceName + " is now with your assigned CA for review.";
            }
            case IN_PROGRESS -> {
                title = "Your case is now in progress";
                message = serviceName + " is actively being worked on by your CA.";
            }
            case DOCUMENT_COLLECTION -> {
                title = "Documents needed";
                message = "Please upload the remaining documents for " + serviceName + ".";
                severity = NoticeSeverity.ACTION_REQUIRED;
            }
            case COMPLETED -> {
                title = "Your case is complete";
                message = serviceName + " has been completed. Check your case for details.";
            }
            case CANCELLED -> {
                title = "Your case was cancelled";
                message = serviceName + " has been cancelled. Contact support if this is unexpected.";
                severity = NoticeSeverity.WARNING;
            }
            default -> {
                title = "Case status updated";
                message = serviceName + " status changed to " + status.name().replace("_", " ") + ".";
            }
        }

        noticeService.create(taxCase.getClient(), NoticeType.CASE_UPDATE, severity, title, message, taxCase);
    }

    @Override
    public void assignCa(Long caseId, Long caId) {
        Case taxCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new com.taxin60sec.backend.exception.ApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        com.taxin60sec.backend.common.ApiErrorCode.NOT_FOUND,
                        "Case not found"
                ));

        if (caId == null) {
            taxCase.setAssignedCa(null);
            return;
        }

        User ca = userRepository.findById(caId)
                .orElseThrow(() -> new com.taxin60sec.backend.exception.ApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        com.taxin60sec.backend.common.ApiErrorCode.NOT_FOUND,
                        "CA not found"
                ));

        boolean isCa = ca.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_CA"));
        if (!isCa) {
            throw new com.taxin60sec.backend.exception.ApiException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    com.taxin60sec.backend.common.ApiErrorCode.BAD_REQUEST,
                    "Selected user is not a CA"
            );
        }

        taxCase.setAssignedCa(ca);

        CaseStatus previousStatus = taxCase.getStatus();
        if (taxCase.getStatus() == CaseStatus.INTAKE || taxCase.getStatus() == CaseStatus.DOCUMENT_COLLECTION) {
            taxCase.setStatus(CaseStatus.CA_REVIEW);
        }

        if (taxCase.getClient() != null) {
            String serviceName = taxCase.getServiceOffering() != null ? taxCase.getServiceOffering().getDisplayName() : "your case";
            noticeService.create(
                    taxCase.getClient(),
                    NoticeType.CASE_UPDATE,
                    NoticeSeverity.INFO,
                    "A CA has been assigned to your case",
                    ca.getFullName() + " is now handling " + serviceName + ". You'll be updated as your case moves forward.",
                    taxCase
            );
        }

        if (taxCase.getStatus() != previousStatus) {
            notifyClientOfStatusChange(taxCase, taxCase.getStatus());
        }

        notifyCaAssignment(taxCase, ca);
    }

    /** Notification failure must never block the assignment itself - same pattern used in BusinessService. */
    private void notifyCaAssignment(Case taxCase, User ca) {
        String serviceName = taxCase.getServiceOffering() != null ? taxCase.getServiceOffering().getDisplayName() : "Service";
        String clientName = taxCase.getClient() != null ? taxCase.getClient().getFullName() : "Client";

        if (ca.getEmail() != null && !ca.getEmail().isBlank()) {
            try {
                notificationService.sendCaAssignmentEmail(ca.getEmail(), ca.getFullName(), taxCase.getCaseNumber(), serviceName, clientName);
            } catch (Exception ex) {
                // best-effort, do not block assignment
            }
        }
        if (ca.getPhoneNumber() != null && !ca.getPhoneNumber().isBlank()) {
            try {
                notificationService.sendCaAssignmentWhatsApp(ca.getPhoneNumber(), ca.getFullName(), taxCase.getCaseNumber());
            } catch (Exception ex) {
                // best-effort, do not block assignment
            }
        }

        User client = taxCase.getClient();
        if (client == null) return;

        if (client.getEmail() != null && !client.getEmail().isBlank()) {
            try {
                notificationService.sendClientCaAssignedEmail(client.getEmail(), client.getFullName(), taxCase.getCaseNumber(), serviceName, ca.getFullName());
            } catch (Exception ex) {
                // best-effort, do not block assignment
            }
        }
        if (client.getPhoneNumber() != null && !client.getPhoneNumber().isBlank()) {
            try {
                notificationService.sendClientCaAssignedWhatsApp(client.getPhoneNumber(), client.getFullName(), taxCase.getCaseNumber());
            } catch (Exception ex) {
                // best-effort, do not block assignment
            }
        }
    }

    @Override
    public List<AssignableCaResponse> assignableCas() {
        List<CaseStatus> excluded = List.copyOf(EnumSet.of(CaseStatus.COMPLETED, CaseStatus.CANCELLED));
        return userRepository.findByRoles_NameAndActiveTrue("ROLE_CA").stream()
                .map(ca -> new AssignableCaResponse(
                        ca.getId(),
                        ca.getFullName(),
                        ca.getEmail(),
                        caseRepository.countByAssignedCa_IdAndDeletedFalseAndStatusNotIn(ca.getId(), excluded)
                ))
                .toList();
    }
}