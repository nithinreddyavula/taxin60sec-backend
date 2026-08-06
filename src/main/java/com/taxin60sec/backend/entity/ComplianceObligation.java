package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.ComplianceStatus;
import com.taxin60sec.backend.entity.enums.ComplianceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(
        name = "compliance_obligations",
        indexes = {
                @Index(name = "idx_compliance_client", columnList = "client_id"),
                @Index(name = "idx_compliance_due_date", columnList = "due_date")
        }
)
public class ComplianceObligation extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplianceType type;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String title;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplianceStatus status = ComplianceStatus.PENDING;

    private Instant completedAt;

    /** Optional link back to the case that generated this obligation, if any. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case relatedCase;

    /** Set when a reminder has already been sent, so the daily job doesn't re-notify every run. */
    @Column(nullable = false)
    private boolean reminderSent = false;
}