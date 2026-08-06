package com.taxin60sec.backend.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${tax60.security.jwt.secret:${TAX60_JWT_SECRET:}}") String secret,
            @Value("${tax60.security.jwt.access-token-expiration-seconds:900}") long expirationSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", principal.getUsername());
        payload.put("uid", principal.getId());
        payload.put("roles", principal.getAuthorities().stream().map(Object::toString).toList());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        return sign(header, payload);
    }

    public String extractSubject(String token) {
        return payload(token).get("sub").toString();
    }

    public Instant getAccessTokenExpiresAt() {
        return Instant.now().plusSeconds(expirationSeconds);
    }

    public boolean isValid(String token, UserPrincipal principal) {
        return principal.getUsername().equals(extractSubject(token)) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        Number expiration = (Number) payload(token).get("exp");
        return Instant.now().getEpochSecond() >= expiration.longValue();
    }

    private String sign(Map<String, Object> header, Map<String, Object> payload) {
        requireSecret();
        try {
            String encodedHeader = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(header));
            String encodedPayload = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
            String content = encodedHeader + "." + encodedPayload;
            return content + "." + signature(content);
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_SERVER_ERROR, "Unable to generate access token");
        }
    }

    private Map<String, Object> payload(String token) {
        requireSecret();
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw invalidToken();
        }
        String content = parts[0] + "." + parts[1];
        if (!MessageDigest.isEqual(signature(content).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw invalidToken();
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {
            });
            Number expiration = (Number) payload.get("exp");
            if (expiration == null || Instant.now().getEpochSecond() >= expiration.longValue()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.TOKEN_EXPIRED, "JWT has expired");
            }
            return payload;
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalidToken();
        }
    }

    private String signature(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_SERVER_ERROR, "Unable to sign access token");
        }
    }

    private void requireSecret() {
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_SERVER_ERROR, "JWT secret is not configured securely");
        }
    }

    private ApiException invalidToken() {
        return new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_TOKEN, "Invalid JWT");
    }
}
