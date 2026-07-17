package com.taxin60sec.backend.dto.business;

import jakarta.validation.constraints.*;
import java.time.Instant;

public final class DocumentRequests {
    private DocumentRequests() { }
    public record Create(@NotBlank @Size(max=180) String originalFilename, @NotBlank @Size(max=120) String documentType,
                         @Size(max=600) String storageKey, @Size(max=160) String mimeType, @PositiveOrZero Long fileSize,
                         Long requiredDocumentId, @Future Instant expiresAt) { }
    public record Update(@NotBlank @Size(max=180) String originalFilename, @NotBlank @Size(max=120) String documentType,
                         @Size(max=600) String storageKey, @Size(max=160) String mimeType, @PositiveOrZero Long fileSize) { }
    public record Reject(@NotBlank @Size(max=1000) String reason) { }
}
