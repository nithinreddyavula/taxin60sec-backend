package com.taxin60sec.backend.controller;
import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.admin.AdminCaseDetailResponse;
import com.taxin60sec.backend.dto.admin.AdminCaseSummaryResponse;
import com.taxin60sec.backend.service.AdminCaseService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/admin/cases")
@RequiredArgsConstructor
public class AdminCaseController {

    private final AdminCaseService service;

    @GetMapping
    public ApiResponse<List<AdminCaseSummaryResponse>> all(){

        return ApiResponse.success(

                "Cases",

                service.getAllCases(),

                null

        );

    }

    @GetMapping("/{id}")
    public ApiResponse<AdminCaseDetailResponse> one(

            @PathVariable Long id

    ){

        return ApiResponse.success(

                "Case",

                service.getCase(id),

                null

        );

    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> update(

            @PathVariable Long id,

            @RequestParam String status

    ){

        service.updateStatus(id,status);

        return ApiResponse.success(

                "Updated",

                null,

                null

        );

    }

}