package com.taxin60sec.backend.payment;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.Payment;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.EscrowStatus;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The escrow-style payout release for the marketplace model (item #5), now milestone-based
 * rather than binary: a payment's escrowStatus moves HELD -> PARTIALLY_RELEASED -> RELEASED
 * as the case progresses through documents-verified -> in-progress -> filed -> client-confirmed,
 * releasing an incremental slice of the payment (and commission) at each step rather than the
 * whole amount at once.
 *
 * releaseForMilestone() is called from BusinessService.transition() whenever the case moves
 * into one of MILESTONE_PERCENTAGES' stages. releaseForClientConfirmation() and releaseByAdmin()
 * both release straight to 100%, since "client confirms" and "admin override" are both final
 * releases rather than partial ones.
 *
 * releasePercentage tracks the CUMULATIVE percentage released so far, so re-entering a stage
 * (or a workflow that skips a stage) never double-releases - only the incremental delta above
 * whatever was already released gets paid out.
 */
@Service
@Transactional
public class EscrowService {

    private static final Map<WorkflowStage, Integer> MILESTONE_PERCENTAGES = Map.of(
            WorkflowStage.DOCUMENTS_VERIFIED, 25,
            WorkflowStage.PROCESSING, 50,
            WorkflowStage.FILED, 75
    );

    private final CaseRepository cases;
    private final PaymentRepository payments;
    private final com.taxin60sec.backend.repository.CAProfileRepository caProfiles;
    private final PayoutProvider payoutProvider;
    private final com.taxin60sec.backend.service.PlatformSettingsService settingsService;

    public EscrowService(CaseRepository cases, PaymentRepository payments,
                          com.taxin60sec.backend.repository.CAProfileRepository caProfiles,
                          PayoutProvider payoutProvider,
                          com.taxin60sec.backend.service.PlatformSettingsService settingsService) {
        this.cases = cases;
        this.payments = payments;
        this.caProfiles = caProfiles;
        this.payoutProvider = payoutProvider;
        this.settingsService = settingsService;
    }

    /** Called by BusinessService.transition() on every workflow stage change - a no-op for stages that aren't a payment milestone. */
    public List<Payment> releaseForMilestone(Case c, WorkflowStage stage) {
        Integer targetPercent = MILESTONE_PERCENTAGES.get(stage);
        if (targetPercent == null) return List.of();
        return releasePartial(c, targetPercent, "MILESTONE:" + stage.name());
    }

    /**
     * Client-initiated final release: the client confirms the case's work is delivered to their
     * satisfaction. Records the confirmation milestone on the case, then releases the remaining
     * balance (up to 100%) of every payment still HELD or PARTIALLY_RELEASED for it.
     */
    public List<Payment> releaseForClientConfirmation(Long caseId, User client) {
        Case c = caseById(caseId);

        if (c.getClient() == null || !Objects.equals(c.getClient().getId(), client.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "Case does not belong to current client");
        }

        if (c.getConfirmedByClientAt() == null) {
            c.setConfirmedByClientAt(Instant.now());
        }

        return releasePartial(c, 100, "CLIENT");
    }

    /**
     * Admin override release to 100% - does NOT require confirmedByClientAt to be set, since an
     * admin may need to unblock a payout the client has gone unresponsive on.
     */
    public List<Payment> releaseByAdmin(Long caseId, User admin) {
        Case c = caseById(caseId);
        return releasePartial(c, 100, "ADMIN");
    }

    private List<Payment> releasePartial(Case c, int targetPercent, String releasedBy) {
        List<Payment> eligible = payments.findByRelatedCase_IdAndEscrowStatusIn(
                c.getId(), List.of(EscrowStatus.HELD, EscrowStatus.PARTIALLY_RELEASED)
        );
        if (eligible.isEmpty()) return List.of();

        Instant now = Instant.now();
        List<Payment> touched = new ArrayList<>();

        for (Payment payment : eligible) {
            int currentPercent = payment.getReleasedPercentage() == null ? 0 : payment.getReleasedPercentage();
            if (targetPercent <= currentPercent) continue; // already released at least this much - never double-release

            BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
            BigDecimal incrementalGross = amount
                    .multiply(BigDecimal.valueOf(targetPercent - currentPercent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal incrementalCommission = incrementalGross
                    .multiply(settingsService.getFraction(com.taxin60sec.backend.service.PlatformSettingsService.COMMISSION_PERCENTAGE))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal incrementalPayout = incrementalGross.subtract(incrementalCommission);

            payment.setPlatformCommissionAmount(nullToZero(payment.getPlatformCommissionAmount()).add(incrementalCommission));
            payment.setCaPayoutAmount(nullToZero(payment.getCaPayoutAmount()).add(incrementalPayout));
            payment.setReleasedPercentage(targetPercent);
            payment.setEscrowStatus(targetPercent >= 100 ? EscrowStatus.RELEASED : EscrowStatus.PARTIALLY_RELEASED);
            payment.setEscrowReleasedAt(now);
            payment.setEscrowReleasedBy(releasedBy);

            attemptPayout(c, payment, incrementalPayout);

            touched.add(payment);
        }

        return touched;
    }

    /**
     * Attempts to actually move the incremental payout amount to the assigned CA. This is
     * deliberately separate from - and never allowed to undo - the release bookkeeping above:
     * the escrow release decision (how much of the client's money is earned and owed to the CA)
     * is a business fact that's true regardless of whether a payout provider is configured or
     * the transfer succeeds right now. A failed or unconfigured payout just means the money is
     * tracked as owed but not yet actually sent - visible on the payment record for retry or
     * manual follow-up, never silently lost and never blocking the case workflow.
     */
    private void attemptPayout(Case c, Payment payment, java.math.BigDecimal incrementalPayout) {
        if (incrementalPayout == null || incrementalPayout.signum() <= 0) return;
        if (c.getAssignedCa() == null) return;

        try {
            var profile = caProfiles.findByUserId(c.getAssignedCa().getId()).orElse(null);
            if (profile == null || !profile.payoutDestinationConfigured()) {
                payment.setPayoutProviderStatus("NO_DESTINATION");
                payment.setPayoutFailureReason("CA has not set up a payout destination yet");
                payment.setPayoutAttemptedAt(Instant.now());
                return;
            }

            User ca = c.getAssignedCa();
            PayoutProvider.PayoutResult result = payoutProvider.payout(new PayoutProvider.PayoutRequest(
                    payment.getId() + ":" + payment.getReleasedPercentage(),
                    incrementalPayout,
                    "INR",
                    profile.getPayoutAccountHolderName() != null ? profile.getPayoutAccountHolderName() : ca.getFullName(),
                    ca.getEmail(),
                    ca.getPhoneNumber(),
                    profile.getPayoutMethod(),
                    profile.getPayoutBankAccountNumber(),
                    profile.getPayoutBankIfsc(),
                    profile.getPayoutUpiId(),
                    profile.getPayoutProviderContactId(),
                    profile.getPayoutProviderFundAccountId()
            ));

            payment.setPayoutProviderStatus(result.providerStatus());
            payment.setPayoutProviderReference(result.providerReference());
            payment.setPayoutFailureReason(result.failureReason());
            payment.setPayoutAttemptedAt(Instant.now());

            if (result.contactId() != null && profile.getPayoutProviderContactId() == null) {
                profile.setPayoutProviderContactId(result.contactId());
            }
            if (result.fundAccountId() != null && profile.getPayoutProviderFundAccountId() == null) {
                profile.setPayoutProviderFundAccountId(result.fundAccountId());
            }
        } catch (Exception e) {
            // A payout provider failure must never block or reverse the escrow release itself -
            // same "record and move on" pattern as notification and other best-effort side effects
            // elsewhere in this codebase.
            payment.setPayoutProviderStatus("ERROR");
            payment.setPayoutFailureReason("Payout attempt failed: " + e.getMessage());
            payment.setPayoutAttemptedAt(Instant.now());
        }
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Case caseById(Long id) {
        return cases.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Case not found"));
    }
}