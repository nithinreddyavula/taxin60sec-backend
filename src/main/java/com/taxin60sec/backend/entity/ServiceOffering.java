package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.ServiceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "service_offerings")
public class ServiceOffering extends BaseEntity {
    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String displayName;

    @Size(max = 1200)
    @Column(length = 1200)
    private String description;

    @NotNull
    @Column(nullable = false, length = 80)
    @Enumerated(EnumType.STRING)
    private ServiceCategory category = ServiceCategory.OTHER;

    @Min(0)
    private Integer estimatedCompletionDays;

    @DecimalMin("0.00")
    @Column(precision = 12, scale = 2)
    private BigDecimal basePrice;

    @DecimalMin("0.00")
    @Column(precision = 12, scale = 2)
    private BigDecimal minimumPrice;

    @DecimalMin("0.00")
    @Column(precision = 12, scale = 2)
    private BigDecimal maximumPrice;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean featured = false;

    @Min(0)
    @Column(nullable = false)
    private int displayOrder = 0;

    @Size(max = 120)
    @Column(length = 120)
    private String icon;

    @Size(max = 40)
    @Column(length = 40)
    private String color;

    @Column(nullable = false)
    private boolean requiresPaymentFirst = false;

    @Column(nullable = false)
    private boolean requiresDocumentVerification = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    private User updatedBy;

    @OneToMany(mappedBy = "serviceOffering")
    private Set<Case> cases = new HashSet<>();

    @OneToMany(mappedBy = "serviceOffering")
    private Set<RequiredDocument> requiredDocuments = new HashSet<>();
}
