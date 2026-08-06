package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Internal-only CA performance record (item #8) - case completion time and client
 * satisfaction, used solely by admin to inform future case assignment quality. NEVER
 * exposed to clients or to the CA themselves; every endpoint that touches this entity
 * (see CaPerformanceController) is admin-only, on purpose, matching the "internal only,
 * never shown publicly" requirement.
 *
 * One row per completed case - see CaPerformanceRatingService.rate(), which upserts
 * against caseRef rather than allowing duplicate ratings per case.
 */
@Entity
@Getter
@Setter
@Table(name = "ca_performance_ratings")
public class CaPerformanceRating extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private Case caseRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ca_id", nullable = false)
    private User ca;

    /** Calendar days between the case's assignedAt and completedAt - computed at rating time, not user-entered. */
    private Integer completionDays;

    /** 1 (poor) to 5 (excellent) - set by admin after checking in with the client; no client-facing survey is wired up yet. */
    @Min(1)
    @Max(5)
    private Integer clientSatisfactionScore;

    @Column(length = 2000)
    private String qualityNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_by_user_id")
    private User ratedBy;

    private Instant ratedAt;
}