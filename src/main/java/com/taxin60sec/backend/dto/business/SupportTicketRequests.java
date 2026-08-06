package com.taxin60sec.backend.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupportTicketRequests {

    public record Create(
            @NotBlank @Size(max = 160) String subject,
            @NotBlank @Size(max = 2000) String message,
            Long caseId
    ) {}

    public record Reply(
            @NotBlank @Size(max = 2000) String body
    ) {}
}