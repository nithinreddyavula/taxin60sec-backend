package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.admin.AdminReportsOverviewResponse;
import com.taxin60sec.backend.service.impl.AdminReportsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportsController {

    private final AdminReportsService reportsService;

    public AdminReportsController(AdminReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/overview")
    public ApiResponse<AdminReportsOverviewResponse> overview(HttpServletRequest request) {
        return ApiResponse.success("Reports overview", reportsService.overview(), request.getRequestURI());
    }
}