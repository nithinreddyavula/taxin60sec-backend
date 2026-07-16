package com.taxin60sec.backend.dto.domain;

public record RequiredDocumentDto(
        Long id,
        String name,
        String documentType,
        String description,
        boolean mandatory,
        String acceptedFileTypes,
        Long maximumFileSize,
        String sampleDocumentUrl,
        int displayOrder,
        Long serviceOfferingId,
        Long caseId
) {
}
