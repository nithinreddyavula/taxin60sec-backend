package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.DocumentVerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "uploaded_documents")
public class UploadedDocument extends BaseEntity {
    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String originalFilename;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String documentType;

    @Size(max = 600)
    @Column(length = 600)
    private String storageKey;

    @Size(max = 160)
    @Column(length = 160)
    private String mimeType;

    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DocumentVerificationStatus verificationStatus = DocumentVerificationStatus.PENDING;

    private Instant verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedBy;

    @Size(max = 1000)
    @Column(length = 1000)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case taxCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_document_id")
    private RequiredDocument requiredDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;
}
