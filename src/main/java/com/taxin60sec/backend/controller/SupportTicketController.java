package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.dto.business.SupportTicketRequests;
import com.taxin60sec.backend.dto.domain.SupportTicketDto;
import com.taxin60sec.backend.dto.domain.SupportTicketMessageDto;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support/tickets")
@PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')")
public class SupportTicketController {

    private final SupportTicketService ticketService;

    public SupportTicketController(SupportTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ApiResponse<SupportTicketDto> create(@Valid @RequestBody SupportTicketRequests.Create body, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Ticket created", ticketService.create(body, principal.getUser()), null);
    }

    @GetMapping
    public ApiResponse<PageResponse<SupportTicketDto>> mine(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("Your tickets", ticketService.myTickets(principal.getId(), caseId, page, size), null);
    }
    @GetMapping("/{ticketId}/messages")
    public ApiResponse<List<SupportTicketMessageDto>> messages(@PathVariable Long ticketId, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Ticket messages", ticketService.listMessages(ticketId, principal.getUser()), null);
    }

    @PostMapping("/{ticketId}/messages")
    public ApiResponse<SupportTicketMessageDto> reply(
            @PathVariable Long ticketId,
            @Valid @RequestBody SupportTicketRequests.Reply body,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("Reply sent", ticketService.reply(ticketId, body, principal.getUser()), null);
    }
}