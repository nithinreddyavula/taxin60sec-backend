package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.business.CommunicationRequests;
import com.taxin60sec.backend.dto.domain.CallSessionDto;
import com.taxin60sec.backend.dto.domain.CaseMessageDto;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.BusinessService;
import com.taxin60sec.backend.service.CommunicationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The masked communication layer's API surface - in-app chat and masked-number
 * call requests, scoped to a single case. Access control reuses
 * BusinessApi.caseAccess(), the same check CaseDocumentController already uses:
 * admin always allowed, the assigned CA allowed, the owning client allowed,
 * nobody else.
 */
@RestController
@RequestMapping("/api/v1/cases/{caseId}/messages")
class CaseMessagesController {

    private final BusinessService business;
    private final CommunicationService communication;

    CaseMessagesController(BusinessService business, CommunicationService communication) {
        this.business = business;
        this.communication = communication;
    }

    @GetMapping
    public ApiResponse<List<CaseMessageDto>> list(@PathVariable Long caseId, @AuthenticationPrincipal UserPrincipal p) {
        BusinessApi.caseAccess(business, caseId, p);
        return BusinessApi.ok(communication.listMessages(caseId, p.getUser()));
    }

    @PostMapping
    public ApiResponse<CaseMessageDto> send(
            @PathVariable Long caseId,
            @Valid @RequestBody CommunicationRequests.SendMessage r,
            @AuthenticationPrincipal UserPrincipal p
    ) {
        BusinessApi.caseAccess(business, caseId, p);
        return BusinessApi.ok(communication.sendMessage(caseId, r.content(), p.getUser()));
    }

    @PostMapping("/read")
    public ApiResponse<Void> markRead(@PathVariable Long caseId, @AuthenticationPrincipal UserPrincipal p) {
        BusinessApi.caseAccess(business, caseId, p);
        communication.markRead(caseId, p.getUser());
        return BusinessApi.ok(null);
    }
}

@RestController
@RequestMapping("/api/v1/cases/{caseId}/calls")
class CaseCallsController {

    private final BusinessService business;
    private final CommunicationService communication;

    CaseCallsController(BusinessService business, CommunicationService communication) {
        this.business = business;
        this.communication = communication;
    }

    @GetMapping
    public ApiResponse<List<CallSessionDto>> history(@PathVariable Long caseId, @AuthenticationPrincipal UserPrincipal p) {
        BusinessApi.caseAccess(business, caseId, p);
        return BusinessApi.ok(communication.callHistory(caseId));
    }

    @PostMapping
    public ApiResponse<CallSessionDto> request(@PathVariable Long caseId, @AuthenticationPrincipal UserPrincipal p) {
        BusinessApi.caseAccess(business, caseId, p);
        return BusinessApi.ok(communication.requestCall(caseId, p.getUser()));
    }

    @PostMapping("/{callId}/end")
    public ApiResponse<CallSessionDto> end(@PathVariable Long caseId, @PathVariable Long callId, @AuthenticationPrincipal UserPrincipal p) {
        BusinessApi.caseAccess(business, caseId, p);
        return BusinessApi.ok(communication.endCall(callId, p.getUser()));
    }
}