package com.taxin60sec.backend.dto.admin;

import java.time.Instant;

public record PlatformSettingDto(
        String key,
        String value,
        String defaultValue,
        String description,
        boolean customized,
        Instant updatedAt
) {}