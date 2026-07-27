package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.auth.ChangePasswordRequest;
import com.taxin60sec.backend.dto.domain.UserDto;
import com.taxin60sec.backend.dto.user.UpdateCurrentUserRequest;
import com.taxin60sec.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> currentProfile(HttpServletRequest request) {
        return ApiResponse.success("Current profile retrieved", userService.getCurrentProfile(), request.getRequestURI());
    }

    @PatchMapping("/me")
    public ApiResponse<UserDto> updateCurrentProfile(@Valid @RequestBody UpdateCurrentUserRequest body, HttpServletRequest request) {
        return ApiResponse.success("Current profile updated", userService.updateCurrentProfile(body), request.getRequestURI());
    }

    @PostMapping("/me/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest body, HttpServletRequest request) {
        userService.changePassword(body);
        return ApiResponse.success("Password changed", null, request.getRequestURI());
    }

    @PostMapping("/me/deactivate")
    public ApiResponse<Void> deactivate(HttpServletRequest request) {
        userService.deactivateCurrentAccount();
        return ApiResponse.success("Account deactivated", null, request.getRequestURI());
    }

    @PostMapping("/{userId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> reactivate(@PathVariable Long userId, HttpServletRequest request) {
        return ApiResponse.success("Account reactivated", userService.reactivateAccount(userId), request.getRequestURI());
    }
}
