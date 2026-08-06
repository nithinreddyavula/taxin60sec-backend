package com.taxin60sec.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record UpdateSettingRequest(@NotBlank String value) {}