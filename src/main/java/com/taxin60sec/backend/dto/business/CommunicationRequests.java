package com.taxin60sec.backend.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CommunicationRequests {
    private CommunicationRequests() { }
    public record SendMessage(@NotBlank @Size(max = 4000) String content) { }
}