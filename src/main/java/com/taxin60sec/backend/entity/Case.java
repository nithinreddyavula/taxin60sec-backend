package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.CasePriority;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import com.taxin60sec.backend.entity.enums.ConversationState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "cases", indexes = {
        @Index(name = "idx_cases_client_created", columnList = "client_id,created_at"),
        @Index(name = "idx_cases_ca_stage", columnList = "assigned_ca_id,workflow_stage"),
        @Index(name = "idx_cases_status_priority_due", columnList = "status,priority,expected_completion_date"),
        @Index(name = "idx_cases_business", columnList = "business_profile_id")
})
public class Case extends BaseEntity {
    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String caseNumber;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String title;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CasePriority priority = CasePriority.NORMAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private WorkflowStage workflowStage = WorkflowStage.CREATED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CaseStatus status = CaseStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationState conversationState = ConversationState.GREETING;

    @Size(max = 1500)
    @Column(length = 1500)
    private String remarks;

    @Size(max = 4000)
    @Column(length = 4000)
    private String internalNotes;

    private LocalDate expectedCompletionDate;

    private Instant completedAt;

    private Instant assignedAt;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private boolean paymentRequired = false;

    @Column(nullable = false)
    private boolean documentVerificationCompleted = false;

    /** Case remains the source of truth for the WhatsApp-led intake. */
    @Column(length = 12000)
    private String intakeAnswers;

    @Column(length = 4000)
    private String intakeSummary;

    @Column(nullable = false)
    private boolean intakeCompleted = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal estimatedMinimumPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal estimatedMaximumPrice;

    private Integer estimatedDurationDays;

    @Column(length = 1200)
    private String aiRiskFlags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;

    /** Optional during migration; existing cases continue to use client until associated with a business. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_profile_id")
    private BusinessProfile businessProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_ca_id")
    private User assignedCa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_offering_id")
    private ServiceOffering serviceOffering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by_user_id")
    private User lastUpdatedBy;

    @OneToMany(mappedBy = "taxCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<RequiredDocument> requiredDocuments = new HashSet<>();

    @OneToMany(mappedBy = "taxCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<UploadedDocument> uploadedDocuments = new HashSet<>();

    @OneToMany(mappedBy = "taxCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TimelineEvent> timelineEvents = new HashSet<>();
}
