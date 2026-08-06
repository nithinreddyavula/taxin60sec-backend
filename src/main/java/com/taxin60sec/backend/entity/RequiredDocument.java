package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "required_documents")
public class RequiredDocument extends BaseEntity {
    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String name;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String documentType;

    @Size(max = 600)
    @Column(length = 600)
    private String description;

    @Column(nullable = false)
    private boolean mandatory = true;

    @Size(max = 500)
    @Column(length = 500)
    private String acceptedFileTypes;

    @Min(1)
    private Long maximumFileSize;

    @Size(max = 600)
    @Column(length = 600)
    private String sampleDocumentUrl;

    @Min(0)
    @Column(nullable = false)
    private int displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_offering_id")
    private ServiceOffering serviceOffering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case taxCase;
}
