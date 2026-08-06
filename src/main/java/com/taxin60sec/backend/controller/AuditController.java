package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.entity.AuditLog;
import com.taxin60sec.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository logs;

    @GetMapping("/api/v1/audit/{entityType}/{entityId}")
    public List<AuditLog> history(@PathVariable String entityType, @PathVariable String entityId) {
        return logs.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    @GetMapping("/api/v1/admin/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<AuditLog>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLog> result = logs.search(
                (module == null || module.isBlank()) ? null : module,
                (search == null || search.isBlank()) ? null : search.trim(),
                PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size)
        );
        return ApiResponse.success(
                "Audit logs loaded",
                new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()),
                null
        );
    }

    @GetMapping("/api/v1/admin/audit-logs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AuditLog> detail(@PathVariable Long id) {
        return ApiResponse.success("Audit log", logs.findById(id).orElse(null), null);
    }
}