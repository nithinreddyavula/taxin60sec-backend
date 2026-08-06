package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_client", columnList = "client_id"),
                @Index(name = "idx_payments_provider_payment_id", columnList = "provider_payment_id")
        }
)
public class Payment extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    /** Optional - only set when the payment was made for a specific case. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case relatedCase;

    @Column(name = "provider_payment_id", length = 120)
    private String providerPaymentId;

    @Column(length = 40)
    private String provider;

    @Column(name = "reference_id", length = 120)
    private String referenceId;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.CREATED;

    // ---------- escrow-style release (marketplace model) ----------
    // Set to HELD the moment a webhook marks this payment SUCCESS (see PaymentHistoryService).
    // Null for payments that never reached SUCCESS - there's nothing to hold/release yet.
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private com.taxin60sec.backend.entity.enums.EscrowStatus escrowStatus;

    /** Cumulative percentage of this payment released so far (0-100). Drives milestone-based partial release. */
    private Integer releasedPercentage;

    private java.time.Instant escrowReleasedAt;

    /** Who most recently released a portion: "MILESTONE:<stage>", the client confirming, or an admin override. */
    @Column(length = 40)
    private String escrowReleasedBy;

    /** Cumulative platform commission taken across all partial releases so far. */
    @Column(precision = 12, scale = 2)
    private BigDecimal platformCommissionAmount;

    /** Cumulative amount released to the CA across all partial releases so far. */
    @Column(precision = 12, scale = 2)
    private BigDecimal caPayoutAmount;

    // ---------- real payout execution (distinct from escrow release above) ----------
    // "Released" (above) only means the platform has decided the CA is OWED this money -
    // it is a bookkeeping decision. These fields track whether that money has actually been
    // sent anywhere, via PayoutProvider (RazorpayXPayoutProvider when configured, otherwise
    // StubPayoutProvider - see EscrowService for exactly where this gets called).
    @Column(length = 20)
    private String payoutProviderStatus;

    /** The payout provider's own reference/transaction ID for this transfer attempt, if any. */
    @Column(length = 80)
    private String payoutProviderReference;

    @Column(length = 500)
    private String payoutFailureReason;

    private java.time.Instant payoutAttemptedAt;
}