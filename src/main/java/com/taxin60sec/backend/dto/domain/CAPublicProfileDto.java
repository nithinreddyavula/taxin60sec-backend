package com.taxin60sec.backend.dto.domain;

/**
 * Safe-to-show-to-a-client view of a CA's credentials. Deliberately excludes
 * everything in CAProfileDto that a client should never see: email, PAN
 * number, background-check status, payout details.
 */
public record CAPublicProfileDto(
        Long userId,
        String fullName,
        String membershipNumber,
        String firmName,
        String specialization,
        boolean verified,
        String tier
) {
}