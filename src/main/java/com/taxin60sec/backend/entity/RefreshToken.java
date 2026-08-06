package com.taxin60sec.backend.entity;

import com.taxin60sec.backend.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String tokenHash;

    @NotNull
    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
