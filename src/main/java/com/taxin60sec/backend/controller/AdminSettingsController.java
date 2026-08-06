package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.admin.PlatformSettingDto;
import com.taxin60sec.backend.dto.admin.UpdateSettingRequest;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.PlatformSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final PlatformSettingsService settingsService;

    public AdminSettingsController(PlatformSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ApiResponse<List<PlatformSettingDto>> list(HttpServletRequest request) {
        return ApiResponse.success("Settings loaded", settingsService.allSettings(), request.getRequestURI());
    }

    @PutMapping("/{key}")
    public ApiResponse<PlatformSettingDto> update(
            @PathVariable String key,
            @Valid @RequestBody UpdateSettingRequest body,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) {
        PlatformSettingDto updated = settingsService.update(key, body.value(), principal.getUser());
        return ApiResponse.success("Setting updated", updated, request.getRequestURI());
    }
}