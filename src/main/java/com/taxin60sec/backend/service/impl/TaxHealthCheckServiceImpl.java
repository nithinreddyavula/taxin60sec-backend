package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.healthscore.TaxHealthCheckRequest;
import com.taxin60sec.backend.dto.healthscore.TaxHealthCheckResponse;
import com.taxin60sec.backend.entity.TaxHealthCheck;
import com.taxin60sec.backend.entity.enums.RevenueBand;
import com.taxin60sec.backend.entity.enums.ScoreBand;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.TaxHealthCheckRepository;
import com.taxin60sec.backend.service.TaxHealthCheckService;
import com.taxin60sec.backend.whatsapp.WhatsappService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Scoring engine for the free Tax Health Score tool.
 *
 * Design intent: this is the top-of-funnel growth mechanic (think CIBIL-score-check
 * style virality) that feeds the two real revenue verticals — GST/ITR volume and
 * high-ticket NRI/Virtual CFO. The score itself must be instant, deterministic,
 * and require zero login. WhatsApp delivery and the referral code are what turn a
 * one-off page visit into a share.
 */
@Service
@Transactional
public class TaxHealthCheckServiceImpl implements TaxHealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(TaxHealthCheckServiceImpl.class);
    private static final String REFERRAL_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I to avoid confusion when read aloud/typed
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TaxHealthCheckRepository repository;
    private final WhatsappService whatsappService;
    private final String publicUrl;

    public TaxHealthCheckServiceImpl(
            TaxHealthCheckRepository repository,
            WhatsappService whatsappService,
            @Value("${app.public-url}") String publicUrl
    ) {
        this.repository = repository;
        this.whatsappService = whatsappService;
        this.publicUrl = publicUrl;
    }

    @Override
    public TaxHealthCheckResponse submit(TaxHealthCheckRequest request) {

        int score = 100;
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // --- GST registration risk ---
        boolean shouldBeGstRegistered = request.revenueBand() != RevenueBand.UNDER_20L;
        if (shouldBeGstRegistered && !Boolean.TRUE.equals(request.gstRegistered())) {
            score -= 25;
            issues.add("You may be liable for GST registration at your revenue level but are not registered.");
            recommendations.add("Get GST registered before penalties or interest accumulate.");
        }

        // --- GST filing recency ---
        if (Boolean.TRUE.equals(request.gstRegistered())) {
            long daysSinceGst = daysSince(request.lastGstFilingDate());
            if (daysSinceGst < 0) {
                score -= 15;
                issues.add("No GST filing date on record.");
                recommendations.add("File your pending GST returns to avoid late fees.");
            } else if (daysSinceGst > 120) {
                score -= 30;
                issues.add("Your last GST filing was over 4 months ago.");
                recommendations.add("You likely have multiple pending GST returns — this compounds penalties monthly.");
            } else if (daysSinceGst > 60) {
                score -= 15;
                issues.add("Your last GST filing was over 2 months ago.");
                recommendations.add("File this month's GST return before the next due date.");
            }
        }

        // --- ITR filing recency ---
        long daysSinceItr = daysSince(request.lastItrFilingDate());
        if (daysSinceItr < 0) {
            score -= 20;
            issues.add("No income tax return filing date on record.");
            recommendations.add("File your ITR — even a NIL or belated return protects you from notices.");
        } else if (daysSinceItr > 545) { // ~18 months
            score -= 20;
            issues.add("Your last ITR filing was over 18 months ago.");
            recommendations.add("You may have missed a filing year — check for pending notices under Section 139.");
        }

        // --- Missing documents ---
        int missingDocsPenalty = Math.min(request.missingDocumentsCount() * 5, 20);
        if (missingDocsPenalty > 0) {
            score -= missingDocsPenalty;
            issues.add("You're missing " + request.missingDocumentsCount() + " document(s) needed for clean filing.");
            recommendations.add("Upload the missing documents so your CA can validate everything in one pass.");
        }

        // --- NRI / foreign income specific risk ---
        boolean nriRisk = request.isNri() || request.hasForeignIncome();
        if (nriRisk) {
            score -= 10;
            issues.add("Foreign income or NRI status detected — DTAA and repatriation rules apply.");
            recommendations.add("Check your DTAA benefit eligibility and Form 15CA/15CB status before your next remittance.");
        }

        score = Math.max(0, Math.min(100, score));
        ScoreBand band = bandFor(score);

        TaxHealthCheck check = new TaxHealthCheck();
        check.setFullName(request.fullName());
        check.setPhoneNumber(request.phoneNumber());
        check.setEmail(request.email());
        check.setGstRegistered(Boolean.TRUE.equals(request.gstRegistered()));
        check.setLastGstFilingDate(request.lastGstFilingDate());
        check.setLastItrFilingDate(request.lastItrFilingDate());
        check.setMissingDocumentsCount(request.missingDocumentsCount());
        check.setHasForeignIncome(request.hasForeignIncome());
        check.setNri(request.isNri());
        check.setRevenueBand(request.revenueBand());
        check.setScore(score);
        check.setScoreBand(band);
        check.setShareToken(generateToken());
        check.setReferralCode(generateUniqueReferralCode());
        check.setReferredByCode(blankToNull(request.referredByCode()));

        check = repository.save(check);

        boolean delivered = tryDeliverOnWhatsapp(check, issues, band);
        check.setWhatsappDelivered(delivered);

        // NRI_TAXATION is the only service code confirmed to exist (seeded by ServiceCatalogSeeder).
        // Deliberately not guessing codes for the domestic GST/ITR services here — if a guessed
        // code doesn't match what's actually in the DB, the CTA button silently breaks. Send those
        // users to a plain /intake instead, where they pick from the live service dropdown.
        String ctaServiceCode = nriRisk ? "NRI_TAXATION" : null;

        return toResponse(check, issues, recommendations, ctaServiceCode);
    }

    @Override
    public TaxHealthCheckResponse getByShareToken(String shareToken) {
        TaxHealthCheck check = repository.findByShareToken(shareToken)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Result not found or expired"));

        // Recompute the display lists so a shared/reloaded link shows the same content
        // without re-running penalties twice into the stored score.
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        boolean nriRisk = check.isNri() || check.isHasForeignIncome();

        if (nriRisk) {
            issues.add("Foreign income or NRI status detected — DTAA and repatriation rules apply.");
            recommendations.add("Check your DTAA benefit eligibility and Form 15CA/15CB status before your next remittance.");
        }
        if (check.getScore() < 100) {
            recommendations.add("Talk to a Tax60 CA to fix the items flagged above.");
        }

        String ctaServiceCode = nriRisk ? "NRI_TAXATION" : null;

        return toResponse(check, issues, recommendations, ctaServiceCode);
    }

    private boolean tryDeliverOnWhatsapp(TaxHealthCheck check, List<String> issues, ScoreBand band) {
        try {
            String resultUrl = publicUrl + "/health-score/result/" + check.getShareToken();
            StringBuilder message = new StringBuilder();
            message.append("Your Tax60 Health Score: *").append(check.getScore()).append("/100*")
                    .append(" (").append(band.name().replace('_', ' ')).append(")\n\n");
            if (!issues.isEmpty()) {
                message.append("Top issue: ").append(issues.get(0)).append("\n\n");
            }
            message.append("Full report: ").append(resultUrl);
            whatsappService.sendTextMessage(check.getPhoneNumber(), message.toString());
            return true;
        } catch (Exception ex) {
            // WhatsApp credentials may not be live yet (or the number may not be on WhatsApp) —
            // this must never block the user from seeing their score.
            log.warn("Could not deliver Tax Health Score over WhatsApp for id={}: {}", check.getId(), ex.getMessage());
            return false;
        }
    }

    private TaxHealthCheckResponse toResponse(
            TaxHealthCheck check,
            List<String> issues,
            List<String> recommendations,
            String ctaServiceCode
    ) {
        String headline = switch (check.getScoreBand()) {
            case EXCELLENT -> "Your taxes are in great shape.";
            case GOOD -> "You're mostly compliant, a couple of things need attention.";
            case NEEDS_ATTENTION -> "A few gaps could cost you in penalties.";
            case AT_RISK -> "Your filings need attention soon.";
            case CRITICAL -> "Urgent: you have significant compliance gaps.";
        };

        String ctaLabel = check.getScore() >= 85
                ? "Keep it that way — talk to a CA"
                : "Fix this now with Tax60";

        return new TaxHealthCheckResponse(
                check.getShareToken(),
                check.getScore(),
                check.getScoreBand(),
                headline,
                issues,
                recommendations,
                ctaServiceCode,
                ctaLabel,
                check.getReferralCode(),
                publicUrl + "/health-score?ref=" + check.getReferralCode(),
                check.isWhatsappDelivered()
        );
    }

    private ScoreBand bandFor(int score) {
        if (score >= 85) return ScoreBand.EXCELLENT;
        if (score >= 70) return ScoreBand.GOOD;
        if (score >= 50) return ScoreBand.NEEDS_ATTENTION;
        if (score >= 30) return ScoreBand.AT_RISK;
        return ScoreBand.CRITICAL;
    }

    private long daysSince(LocalDate date) {
        if (date == null) return -1;
        return ChronoUnit.DAYS.between(date, LocalDate.now());
    }

    private String generateToken() {
        StringBuilder sb = new StringBuilder(24);
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 24; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private String generateUniqueReferralCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(REFERRAL_ALPHABET.charAt(RANDOM.nextInt(REFERRAL_ALPHABET.length())));
            }
            code = sb.toString();
        } while (repository.existsByReferralCode(code));
        return code;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim().toUpperCase();
    }
}
