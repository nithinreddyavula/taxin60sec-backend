package com.taxin60sec.backend.dto.domain;

public record CAProfileDto(
        Long id,
        Long userId,
        String fullName,
        String email,
        String membershipNumber,
        String panNumber,
        String firmName,
        String specialization,
        boolean verified,
        String tier,
        String backgroundCheckStatus,
        boolean practiceCertificateUploaded,
        boolean panDocumentUploaded,
        boolean agreementAccepted,
        String availability,
        long activeCaseload,
        boolean payoutDestinationConfigured,
        String payoutMethod,
        String payoutUpiId,
        String payoutBankAccountNumberMasked
) {
}