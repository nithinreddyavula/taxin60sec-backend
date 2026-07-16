package com.taxin60sec.backend.service;

import com.taxin60sec.backend.entity.RefreshToken;
import com.taxin60sec.backend.entity.User;

public interface RefreshTokenService {
    String create(User user);

    RefreshToken verify(String rawToken);

    void revoke(String rawToken);

    void revokeAll(User user);
}
