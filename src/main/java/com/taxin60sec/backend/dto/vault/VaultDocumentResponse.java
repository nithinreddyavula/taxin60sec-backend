package com.taxin60sec.backend.dto.vault;

import java.time.Instant;

public record VaultDocumentResponse(
        Long id,
        String originalFilename,
        String documentType,
        String caseNumber,
        String serviceName,
        String verificationStatus,
        Long fileSize,
        Instant uploadedAt
) {}