package com.taxin60sec.backend.dto.admin;

/**
 * Feeds the "assign to CA" dropdown on the admin case-assignment screen.
 * Only ever built from verified CAProfile rows (see AdminCaListController) -
 * never from the raw user list - so admin can never accidentally pick a CA
 * who hasn't completed KYC, and can see tier at a glance to route simple vs
 * complex work to the right CA.
 *
 * `id` is the CA's User ID - the same value the existing "assign" call
 * expects as caUserId, kept under this name for compatibility with the
 * current admin dashboard's CA dropdown.
 */
public record CaSummaryResponse(
        Long id,
        String fullName,
        String email,
        String tier,
        String firmName,
        String specialization,
        String availability,
        long activeCaseload,
        Double averageRating
) {}