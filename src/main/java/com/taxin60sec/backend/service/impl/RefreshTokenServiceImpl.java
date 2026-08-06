package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.entity.RefreshToken;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.RefreshTokenRepository;
import com.taxin60sec.backend.service.RefreshTokenService;
import com.taxin60sec.backend.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final long expirationSeconds;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${tax60.security.jwt.refresh-token-expiration-seconds:2592000}") long expirationSeconds
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    @Transactional
    public String create(User user) {
        String rawToken = newToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(DateTimeUtils.nowUtc().plusSeconds(expirationSeconds));
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verify(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_TOKEN, "Invalid refresh token"));
        if (refreshToken.isRevoked()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_TOKEN, "Refresh token has been revoked");
        }
        if (Instant.now().isAfter(refreshToken.getExpiresAt())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.TOKEN_EXPIRED, "Refresh token has expired");
        }
        return refreshToken;
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(refreshToken -> {
            refreshToken.setRevokedAt(DateTimeUtils.nowUtc());
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Override
    @Transactional
    public void revokeAll(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private String newToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_SERVER_ERROR, "Unable to process refresh token");
        }
    }
}
