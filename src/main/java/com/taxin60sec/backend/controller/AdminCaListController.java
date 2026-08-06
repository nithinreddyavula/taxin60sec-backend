package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.admin.CaSummaryResponse;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.repository.CAProfileRepository;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.service.impl.CaPerformanceRatingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Feeds the "assign to CA" dropdown in the admin dashboard - kept as its own small
 * controller rather than folded into BusinessControllers.java since it isn't a
 * case-scoped resource.
 *
 * Sources from verified CAProfile rows only (never the raw ROLE_CA user list) so
 * this can never surface a CA who hasn't finished KYC - matching the marketplace
 * model's "only verified partners are assignable" requirement. BusinessService.assign()
 * already re-checks verification server-side regardless, but the dropdown should
 * never offer a choice that assign() would then reject.
 *
 * Also surfaces each CA's self-reported availability, current active caseload
 * (item #9, CA capacity/availability), and average internal performance rating
 * (item #8) so admin can route around both capacity AND quality during seasonal
 * spikes instead of overloading or over-trusting whoever happens to be next in
 * the list. The rating stays admin-only here, same as everywhere else it's used -
 * this endpoint is already @PreAuthorize("hasRole('ADMIN')").
 */
@RestController
@RequestMapping("/api/v1/admin/cas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCaListController {

    private static final Set<CaseStatus> TERMINAL_STATUSES = EnumSet.of(CaseStatus.COMPLETED, CaseStatus.CANCELLED);

    private final CAProfileRepository caProfileRepository;
    private final CaseRepository caseRepository;
    private final CaPerformanceRatingService ratingService;

    public AdminCaListController(
            CAProfileRepository caProfileRepository,
            CaseRepository caseRepository,
            CaPerformanceRatingService ratingService
    ) {
        this.caProfileRepository = caProfileRepository;
        this.caseRepository = caseRepository;
        this.ratingService = ratingService;
    }

    @GetMapping
    public ApiResponse<List<CaSummaryResponse>> list() {
        List<CaSummaryResponse> cas = caProfileRepository.findByVerifiedTrueOrderByCreatedAtAsc()
                .stream()
                .map(profile -> new CaSummaryResponse(
                        profile.getUser().getId(),
                        profile.getUser().getFullName(),
                        profile.getUser().getEmail(),
                        profile.getTier() != null ? profile.getTier().name() : null,
                        profile.getFirmName(),
                        profile.getSpecialization(),
                        profile.getAvailability().name(),
                        caseRepository.countByAssignedCa_IdAndDeletedFalseAndStatusNotIn(profile.getUser().getId(), TERMINAL_STATUSES),
                        ratingService.averageScoreForCa(profile.getUser().getId())
                ))
                .toList();

        return ApiResponse.success("CA list", cas, null);
    }
}