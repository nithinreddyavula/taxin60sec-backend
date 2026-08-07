package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.admin.AdminCaseDetailResponse;
import com.taxin60sec.backend.dto.admin.AdminCaseSummaryResponse;
import com.taxin60sec.backend.dto.admin.AssignCaseRequest;
import com.taxin60sec.backend.dto.admin.AssignableCaResponse;
import com.taxin60sec.backend.service.AdminCaseService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCaseController {

    private final AdminCaseService service;

    @GetMapping
    public ApiResponse<List<AdminCaseSummaryResponse>> all() {
        return ApiResponse.success("Cases", service.getAllCases(), null);
    }

    @GetMapping("/dashboard")
    public ApiResponse<com.taxin60sec.backend.dto.admin.AdminDashboardResponse> dashboard() {
        return ApiResponse.success("Dashboard", service.dashboard(), null);
    }

    @GetMapping("/assignable-cas")
    public ApiResponse<List<AssignableCaResponse>> assignableCas() {
        return ApiResponse.success("Assignable CAs", service.assignableCas(), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminCaseDetailResponse> one(@PathVariable Long id) {
        return ApiResponse.success("Case", service.getCase(id), null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestParam String status) {
        service.updateStatus(id, status);
        return ApiResponse.success("Updated", null, null);
    }

    @PatchMapping("/{id}/assign")
    public ApiResponse<Void> assign(@PathVariable Long id, @RequestBody AssignCaseRequest request) {
        service.assignCa(id, request.caId());
        return ApiResponse.success("CA assigned", null, null);
    }
}