package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.healthcheck.HealthCheckLeadRequest;
import com.taxin60sec.backend.dto.healthcheck.HealthCheckLeadResponse;
import com.taxin60sec.backend.dto.healthcheck.HealthCheckRequest;
import com.taxin60sec.backend.dto.healthcheck.HealthCheckResponse;
import com.taxin60sec.backend.entity.HealthCheckLead;
import com.taxin60sec.backend.repository.HealthCheckLeadRepository;
import com.taxin60sec.backend.service.NotificationService;
import com.taxin60sec.backend.service.impl.PublicHealthCheckService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/intake/health-check")
public class PublicHealthCheckController {

    private final PublicHealthCheckService healthCheckService;
    private final HealthCheckLeadRepository leadRepository;
    private final NotificationService notificationService;

    public PublicHealthCheckController(
            PublicHealthCheckService healthCheckService,
            HealthCheckLeadRepository leadRepository,
            NotificationService notificationService
    ) {
        this.healthCheckService = healthCheckService;
        this.leadRepository = leadRepository;
        this.notificationService = notificationService;
    }

    @PostMapping
    public ApiResponse<HealthCheckResponse> evaluate(@RequestBody HealthCheckRequest request) {
        HealthCheckResponse response = healthCheckService.evaluate(request);
        return ApiResponse.success("Health check result", response, null);
    }

    /**
     * Upsert: called silently right after the score is shown (no email yet, just to get a leadId that can be
     * carried into intake so a converting user's baseline obligations trace back to this quiz), and again when
     * the person types their email into "Email my results" (leadId provided, so we update rather than duplicate).
     */
    @PostMapping("/capture")
    public ApiResponse<HealthCheckLeadResponse> captureLead(@RequestBody HealthCheckLeadRequest request) {

        HealthCheckLead lead = request.leadId() != null
                ? leadRepository.findById(request.leadId()).orElseGet(HealthCheckLead::new)
                : new HealthCheckLead();

        if (request.email() != null && !request.email().isBlank()) {
            lead.setEmail(request.email());
        }
        lead.setPhoneNumber(request.phoneNumber());
        lead.setUserType(request.userType());
        lead.setScore(request.score());
        lead.setStatusLabel(request.statusLabel());
        if (request.triggeredCodes() != null && !request.triggeredCodes().isEmpty()) {
            lead.setTriggeredServiceCodes(String.join(",", request.triggeredCodes()));
        }

        HealthCheckLead saved = leadRepository.save(lead);

        if (request.email() != null && !request.email().isBlank()) {
            try {
                notificationService.sendHealthCheckResultsEmail(
                        request.email(),
                        request.score(),
                        request.statusLabel(),
                        request.issuesSummary()
                );
            } catch (Exception ignored) {
                // Lead is already saved - a failed follow-up email shouldn't fail the capture.
            }
        }

        return ApiResponse.success("Results saved", toResponse(saved), null);
    }

    private HealthCheckLeadResponse toResponse(HealthCheckLead lead) {
        return new HealthCheckLeadResponse(
                lead.getId(),
                lead.getEmail(),
                lead.getPhoneNumber(),
                lead.getUserType(),
                lead.getScore(),
                lead.getStatusLabel(),
                lead.isConverted(),
                lead.getCreatedAt()
        );
    }
}