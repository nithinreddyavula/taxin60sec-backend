package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.entity.Role;
import com.taxin60sec.backend.repository.RoleRepository;
import com.taxin60sec.backend.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleServiceImpl implements RoleService {
    private static final String CLIENT_ROLE = "CLIENT";

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public Role getClientRole() {
        return roleRepository.findByName(CLIENT_ROLE)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(CLIENT_ROLE);
                    role.setDescription("Client user");
                    return roleRepository.save(role);
                });
    }
}
