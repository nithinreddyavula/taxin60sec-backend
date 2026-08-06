package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.UserDto;
import com.taxin60sec.backend.entity.Role;
import com.taxin60sec.backend.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        // Role names are stored bare ("CLIENT", "ADMIN") but the JWT and every
        // frontend role check use the Spring Security convention ("ROLE_CLIENT").
        // Keep this consistent with UserPrincipal.getAuthorities(), which does
        // the same prefixing for the token - otherwise the roles returned here
        // silently fail to match "ROLE_*" checks and users get bounced back to
        // login right after a successful auth response.
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(name -> name.startsWith("ROLE_") ? name : "ROLE_" + name)
                .collect(Collectors.toUnmodifiableSet());

        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.isActive(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}