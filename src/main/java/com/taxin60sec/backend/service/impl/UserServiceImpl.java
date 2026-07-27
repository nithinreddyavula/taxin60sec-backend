package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.auth.ChangePasswordRequest;
import com.taxin60sec.backend.dto.domain.UserDto;
import com.taxin60sec.backend.dto.user.UpdateCurrentUserRequest;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.mapper.UserMapper;
import com.taxin60sec.backend.repository.UserRepository;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.RefreshTokenService;
import com.taxin60sec.backend.service.UserService;
import com.taxin60sec.backend.utils.TextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentProfile() {
        return userMapper.toDto(currentUser());
    }

    @Override
    @Transactional
    public UserDto updateCurrentProfile(UpdateCurrentUserRequest request) {
        User user = currentUser();
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(TextUtils.normalize(request.fullName()));
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(TextUtils.normalize(request.phoneNumber()));
        }
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAll(user);
    }

    @Override
    @Transactional
    public void deactivateCurrentAccount() {
        User user = currentUser();
        user.setActive(false);
        userRepository.save(user);
        refreshTokenService.revokeAll(user);
    }

    @Override
    @Transactional
    public UserDto reactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "User not found"));
        user.setActive(true);
        if (user.isDeleted()) {
            user.restore();
        }
        return userMapper.toDto(userRepository.save(user));
    }

    private User currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUser();
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, "Authentication is required");
    }
}
