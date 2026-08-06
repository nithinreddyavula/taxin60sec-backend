package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.domain.PaymentBreakdownDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.UserRepository;
import com.taxin60sec.backend.service.PlatformSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final PlatformSettingsService settingsService;

    public PricingService(CaseRepository caseRepository, UserRepository userRepository, PlatformSettingsService settingsService) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.settingsService = settingsService;
    }

    /**
     * Resolves the amount to actually charge for a case, applying (and consuming) at most
     * one referral discount:
     *  1. If this client was referred and hasn't used their welcome discount yet - apply it.
     *  2. Otherwise, if the client has earned referral credits from referring others - use one.
     * Each discount is one-time: the flag/credit is consumed the moment it's used here.
     */
    @Transactional
    public BigDecimal resolveAmount(Long caseId) {

        Case taxCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));

        BigDecimal base = taxCase.getServiceOffering().getBasePrice();
        User client = taxCase.getClient();

        BigDecimal discountedAmount = base;

        if (client.getReferredByCode() != null
                && !client.getReferredByCode().isBlank()
                && !taxCase.isReferralDiscountApplied()) {

            discountedAmount = applyDiscount(base);
            taxCase.setReferralDiscountApplied(true);
            caseRepository.save(taxCase);

        } else if (client.getReferralCredits() > 0) {

            discountedAmount = applyDiscount(base);
            client.setReferralCredits(client.getReferralCredits() - 1);
            userRepository.save(client);
        }

        return discountedAmount;
    }

    /**
     * Same discount logic as resolveAmount(), but read-only - does NOT consume the referral
     * flag/credit. Use this for display purposes (e.g. showing the checkout breakdown before
     * the client actually pays); resolveAmount() stays the one source of truth for what
     * actually gets charged and consumed at payment time.
     */
    public BigDecimal previewAmount(Long caseId) {
        Case taxCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));

        BigDecimal base = taxCase.getServiceOffering().getBasePrice();
        User client = taxCase.getClient();

        boolean eligibleForDiscount =
                (client.getReferredByCode() != null && !client.getReferredByCode().isBlank() && !taxCase.isReferralDiscountApplied())
                        || client.getReferralCredits() > 0;

        return eligibleForDiscount ? applyDiscount(base) : base;
    }

    /**
     * Itemized breakdown for the checkout screen: Platform Fee / Expert Fee / GST / Total.
     * The displayed total is treated as GST-inclusive (standard for India), so GST is
     * backed out of it rather than added on top; the remaining pre-tax amount is then split
     * into platform fee vs expert fee using the same commission rate EscrowService applies
     * on release, so the numbers a client sees at checkout match what the CA actually earns.
     */
    public PaymentBreakdownDto breakdownFor(Long caseId) {
        BigDecimal total = previewAmount(caseId);

        BigDecimal gstRate = settingsService.getFraction(PlatformSettingsService.GST_RATE_PERCENTAGE);
        BigDecimal commissionRate = settingsService.getFraction(PlatformSettingsService.COMMISSION_PERCENTAGE);

        BigDecimal preTax = total.divide(BigDecimal.ONE.add(gstRate), 2, RoundingMode.HALF_UP);
        BigDecimal gst = total.subtract(preTax);

        BigDecimal platformFee = preTax.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expertFee = preTax.subtract(platformFee);

        return new PaymentBreakdownDto(total, expertFee, platformFee, gst, "INR");
    }

    private BigDecimal applyDiscount(BigDecimal base) {
        BigDecimal referralDiscountPercent = settingsService.getDecimal(PlatformSettingsService.REFERRAL_DISCOUNT_PERCENTAGE);
        BigDecimal multiplier = BigDecimal.valueOf(100).subtract(referralDiscountPercent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return base.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}