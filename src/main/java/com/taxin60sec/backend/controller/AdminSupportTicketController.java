package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.dto.domain.SupportTicketDto;
import com.taxin60sec.backend.entity.enums.SupportTicketStatus;
import com.taxin60sec.backend.service.impl.SupportTicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/support/tickets")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSupportTicketController {

    private final SupportTicketService ticketService;

    public AdminSupportTicketController(SupportTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ApiResponse<PageResponse<SupportTicketDto>> list(
            @RequestParam(required = false) SupportTicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("Support tickets", ticketService.allTickets(status, page, size), null);
    }

    @PatchMapping("/{ticketId}/status")
    public ApiResponse<SupportTicketDto> setStatus(@PathVariable Long ticketId, @RequestBody java.util.Map<String, String> body) {
        SupportTicketStatus status = SupportTicketStatus.valueOf(body.get("status").toUpperCase());
        return ApiResponse.success("Status updated", ticketService.setStatus(ticketId, status), null);
    }
}