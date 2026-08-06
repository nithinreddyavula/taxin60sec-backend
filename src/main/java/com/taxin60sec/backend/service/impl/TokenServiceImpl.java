package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.security.JwtService;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.TokenService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenServiceImpl implements TokenService {
    private final JwtService jwtService;

    public TokenServiceImpl(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String createAccessToken(UserPrincipal principal) {
        return jwtService.generateAccessToken(principal);
    }

    @Override
    public Instant accessTokenExpiresAt() {
        return jwtService.getAccessTokenExpiresAt();
    }
}
