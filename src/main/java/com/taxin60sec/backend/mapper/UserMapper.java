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
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
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
