package com.taxin60sec.backend.service;

import com.taxin60sec.backend.dto.auth.AuthResponse;
import com.taxin60sec.backend.dto.auth.LoginRequest;
import com.taxin60sec.backend.dto.auth.RefreshTokenRequest;
import com.taxin60sec.backend.dto.auth.RegisterRequest;
import com.taxin60sec.backend.dto.domain.UserDto;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    UserDto currentUser();

    void logout(RefreshTokenRequest request);
}
