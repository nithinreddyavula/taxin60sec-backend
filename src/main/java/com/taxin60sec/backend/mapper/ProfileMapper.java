package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.AdminProfileDto;
import com.taxin60sec.backend.dto.domain.ClientProfileDto;
import com.taxin60sec.backend.entity.AdminProfile;
import com.taxin60sec.backend.entity.ClientProfile;
import com.taxin60sec.backend.entity.User;
import org.springframework.stereotype.Component;

/**
 * NOTE: this mapper does NOT handle CAProfile - CAProfileServiceImpl.toDto() is the single
 * source of truth for CAProfileDto (it needs a CaseRepository lookup for activeCaseload that
 * this class doesn't have access to). A toDto(CAProfile) method used to live here but had
 * drifted out of sync with CAProfileDto's real shape and was dead code (nothing called it) -
 * removed rather than fixed in two places, to avoid the same drift happening again.
 */
@Component
public class ProfileMapper {
    public ClientProfileDto toDto(ClientProfile profile) {
        return new ClientProfileDto(
                profile.getId(),
                idOf(profile.getUser()),
                profile.getBusinessName(),
                profile.getPanNumber(),
                profile.getGstin(),
                profile.getAddress()
        );
    }

    public AdminProfileDto toDto(AdminProfile profile) {
        return new AdminProfileDto(
                profile.getId(),
                idOf(profile.getUser()),
                profile.getDepartment(),
                profile.getDesignation()
        );
    }

    private Long idOf(User user) {
        return user == null ? null : user.getId();
    }
}