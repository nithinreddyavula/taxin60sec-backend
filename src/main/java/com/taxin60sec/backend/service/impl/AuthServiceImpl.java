package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.auth.AuthResponse;
import com.taxin60sec.backend.dto.auth.LoginRequest;
import com.taxin60sec.backend.dto.auth.RefreshTokenRequest;
import com.taxin60sec.backend.dto.auth.RegisterRequest;
import com.taxin60sec.backend.dto.domain.UserDto;
import com.taxin60sec.backend.entity.ClientProfile;
import com.taxin60sec.backend.entity.RefreshToken;
import com.taxin60sec.backend.entity.Role;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.mapper.UserMapper;
import com.taxin60sec.backend.repository.UserRepository;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.AuthService;
import com.taxin60sec.backend.service.RefreshTokenService;
import com.taxin60sec.backend.service.RoleService;
import com.taxin60sec.backend.service.TokenService;
import com.taxin60sec.backend.utils.TextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            RoleService roleService,
            TokenService tokenService,
            RefreshTokenService refreshTokenService,
            UserMapper userMapper
    ) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = TextUtils.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "Email is already registered");
        }

        Role clientRole = roleService.getClientRole();
        User user = new User();
        user.setFullName(TextUtils.normalize(request.fullName()));
        user.setEmail(email);
        user.setPhoneNumber(TextUtils.normalize(request.phoneNumber()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(clientRole);

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setUser(user);
        user.setClientProfile(clientProfile);

        User saved = userRepository.save(user);
        return issueTokens(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(TextUtils.normalizeEmail(request.email()), request.password())
            );
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            return issueTokens(principal.getUser());
        } catch (DisabledException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.USER_DISABLED, "User account is disabled");
        } catch (BadCredentialsException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verify(request.refreshToken());
        User user = refreshToken.getUser();
        ensureEnabled(user);
        refreshTokenService.revoke(request.refreshToken());
        return issueTokens(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto currentUser() {
        return userMapper.toDto(currentPrincipal().getUser());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private AuthResponse issueTokens(User user) {
        ensureEnabled(user);
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = tokenService.createAccessToken(principal);
        String refreshToken = refreshTokenService.create(user);
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                tokenService.accessTokenExpiresAt(),
                userMapper.toDto(user)
        );
    }

    private UserPrincipal currentPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, "Authentication is required");
    }

    private void ensureEnabled(User user) {
        if (!user.isActive() || user.isDeleted()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.USER_DISABLED, "User account is disabled");
        }
    }
}
