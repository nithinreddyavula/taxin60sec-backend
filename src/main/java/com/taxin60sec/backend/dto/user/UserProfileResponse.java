package com.taxin60sec.backend.dto.user;

import com.taxin60sec.backend.dto.domain.UserDto;

public record UserProfileResponse(
        UserDto user
) {
}
