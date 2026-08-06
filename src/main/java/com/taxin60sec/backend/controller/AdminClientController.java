package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.dto.admin.AdminClientSummaryResponse;
import com.taxin60sec.backend.entity.ClientProfile;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.ClientProfileRepository;
import com.taxin60sec.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/clients")
@PreAuthorize("hasRole('ADMIN')")
public class AdminClientController {

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final CaseRepository caseRepository;

    public AdminClientController(UserRepository userRepository, ClientProfileRepository clientProfileRepository, CaseRepository caseRepository) {
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.caseRepository = caseRepository;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminClientSummaryResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<User> users = userRepository.searchByRole(
                "ROLE_CLIENT",
                (search == null || search.isBlank()) ? null : search.trim(),
                PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size)
        );

        Page<AdminClientSummaryResponse> mapped = users.map(user -> {
            ClientProfile profile = clientProfileRepository.findByUserId(user.getId()).orElse(null);
            return new AdminClientSummaryResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    profile != null ? profile.getPanNumber() : null,
                    user.isActive() ? "Active" : "Inactive",
                    user.getCreatedAt(),
                    caseRepository.countByClientIdAndDeletedFalse(user.getId())
            );
        });

        return ApiResponse.success(
                "Clients loaded",
                new PageResponse<>(mapped.getContent(), mapped.getNumber(), mapped.getSize(), mapped.getTotalElements(), mapped.getTotalPages()),
                null
        );
    }
}