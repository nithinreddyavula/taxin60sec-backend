package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.dto.admin.AdminCaseSummaryResponse;
import com.taxin60sec.backend.dto.admin.AdminDashboardResponse;
import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.service.AdminCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminCaseService service;

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> dashboard() {

        return ApiResponse.success(

                "Dashboard loaded successfully",

                service.dashboard(),

                null

        );

    }

}