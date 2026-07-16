package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.RoleDto;
import com.taxin60sec.backend.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {
    public RoleDto toDto(Role role) {
        return new RoleDto(role.getId(), role.getName(), role.getDescription());
    }
}
