package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.RevenueBand;
import com.taxin60sec.backend.entity.enums.ScoreBand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * A single free "Tax Health Score" submission. No login required — this is the
 * top-of-funnel growth hook: anyone can check their score in under a minute,
 * get it delivered on WhatsApp, and share their result / referral link.
 *
 * Deliberately NOT linked to a User account, because most people filling this
 * out are not yet clients — that's the point.
 */
@Entity
@Getter
@Setter
@Table(name = "tax_health_checks", indexes = {
        @Index(name = "idx_health_check_share_token", columnList = "shareToken", unique = true),
        @Index(name = "idx_health_check_referral_code", columnList = "referralCode", unique = true),
        @Index(name = "idx_health_check_referred_by", columnList = "referredByCode"),
        @Index(name = "idx_health_check_phone", columnList = "phoneNumber")
})
public class TaxHealthCheck extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 160)
    private String email;

    @Column(nullable = false)
    private boolean gstRegistered;

    private LocalDate lastGstFilingDate;

    private LocalDate lastItrFilingDate;

    @Column(nullable = false)
    private int missingDocumentsCount;

    @Column(nullable = false)
    private boolean hasForeignIncome;

    @Column(nullable = false)
    private boolean isNri;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RevenueBand revenueBand;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScoreBand scoreBand;

    @Column(nullable = false, unique = true, length = 20)
    private String shareToken;

    @Column(nullable = false, unique = true, length = 12)
    private String referralCode;

    @Column(length = 12)
    private String referredByCode;

    @Column(nullable = false)
    private boolean whatsappDelivered = false;

    @Column(nullable = false)
    private boolean convertedToCase = false;
}
