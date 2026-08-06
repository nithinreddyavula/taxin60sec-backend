package com.taxin60sec.backend.service;

import com.taxin60sec.backend.security.UserPrincipal;

import java.time.Instant;

public interface TokenService {
    String createAccessToken(UserPrincipal principal);

    Instant accessTokenExpiresAt();
}
