package com.taxin60sec.backend.dto.domain;

import com.taxin60sec.backend.entity.enums.DocumentVerificationStatus;

import java.time.Instant;

public record UploadedDocumentDto(
        Long id,
        String originalFilename,
        String documentType,
        String storageKey,
        String mimeType,
        Long fileSize,
        DocumentVerificationStatus verificationStatus,
        Instant verifiedAt,
        Long verifiedByUserId,
        String rejectionReason,
        Long caseId,
        Long requiredDocumentId,
        Long uploadedByUserId
) {
}
