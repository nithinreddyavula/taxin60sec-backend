package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.auth.AuthResponse;
import com.taxin60sec.backend.dto.auth.LoginRequest;
import com.taxin60sec.backend.dto.auth.RefreshTokenRequest;
import com.taxin60sec.backend.dto.auth.RegisterRequest;
import com.taxin60sec.backend.dto.domain.UserDto;
import com.taxin60sec.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", authService.register(request), httpRequest.getRequestURI()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success("Login successful", authService.login(request), httpRequest.getRequestURI());
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success("Token refreshed", authService.refresh(request), httpRequest.getRequestURI());
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me(HttpServletRequest request) {
        return ApiResponse.success("Current user retrieved", authService.currentUser(), request.getRequestURI());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        authService.logout(request);
        return ApiResponse.success("Logout successful", null, httpRequest.getRequestURI());
    }
}
