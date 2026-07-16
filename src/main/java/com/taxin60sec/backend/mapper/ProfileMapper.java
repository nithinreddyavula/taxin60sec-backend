package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.AdminProfileDto;
import com.taxin60sec.backend.dto.domain.CAProfileDto;
import com.taxin60sec.backend.dto.domain.ClientProfileDto;
import com.taxin60sec.backend.entity.AdminProfile;
import com.taxin60sec.backend.entity.CAProfile;
import com.taxin60sec.backend.entity.ClientProfile;
import com.taxin60sec.backend.entity.User;
import org.springframework.stereotype.Component;

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

    public CAProfileDto toDto(CAProfile profile) {
        return new CAProfileDto(
                profile.getId(),
                idOf(profile.getUser()),
                profile.getMembershipNumber(),
                profile.getFirmName(),
                profile.getSpecialization(),
                profile.isVerified()
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
