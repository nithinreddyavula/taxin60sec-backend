package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import com.taxin60sec.backend.entity.enums.NoticeSeverity;
import com.taxin60sec.backend.entity.enums.NoticeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(
        name = "notices",
        indexes = {
                @Index(name = "idx_notices_user_read", columnList = "user_id,read")
        }
)
public class Notice extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NoticeType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeSeverity severity = NoticeSeverity.INFO;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String title;

    @Size(max = 600)
    @Column(length = 600)
    private String message;

    /** Optional link back to the case this notice is about, if any. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case relatedCase;

    @Column(name = "read", nullable = false)
    private boolean read = false;

    private Instant readAt;
}