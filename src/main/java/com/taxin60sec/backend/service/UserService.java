package com.taxin60sec.backend.service;

import com.taxin60sec.backend.dto.auth.ChangePasswordRequest;
import com.taxin60sec.backend.dto.domain.UserDto;
import com.taxin60sec.backend.dto.user.UpdateCurrentUserRequest;

public interface UserService {
    UserDto getCurrentProfile();

    UserDto updateCurrentProfile(UpdateCurrentUserRequest request);

    void changePassword(ChangePasswordRequest request);

    void deactivateCurrentAccount();

    UserDto reactivateAccount(Long userId);
}
