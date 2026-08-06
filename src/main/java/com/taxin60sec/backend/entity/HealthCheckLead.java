package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "health_check_leads")
public class HealthCheckLead extends BaseEntity {

    @Column(length = 160)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 20)
    private String userType;

    @Column(nullable = false)
    private int score;

    @Column(length = 40)
    private String statusLabel;

    @Column(nullable = false)
    private boolean converted = false;

    /** Comma-separated ServiceOffering codes flagged as issues, e.g. "GST_FILING,VIRTUAL_CFO" — used to seed
     *  baseline ComplianceObligations the moment this lead converts into a real client. */
    @Column(length = 400)
    private String triggeredServiceCodes;

    /** Set once this lead signs up and creates a case — links the anonymous quiz result to the real account. */
    private Long clientId;

    private Long caseId;
}