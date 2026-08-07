package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.admin.AdminCaseDetailResponse;
import com.taxin60sec.backend.dto.admin.AdminCaseSummaryResponse;
import com.taxin60sec.backend.dto.admin.AssignCaseRequest;
import com.taxin60sec.backend.dto.admin.AssignableCaResponse;
import com.taxin60sec.backend.service.AdminCaseService;
import com.taxin60sec.backend.service.impl.CaseExcelExportService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCaseController {

    private final AdminCaseService service;
    private final CaseExcelExportService excelExportService;

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

    /** Streams every case as a downloadable .xlsx workbook. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        byte[] workbook = excelExportService.exportCases();

        String filename = "tax60-cases-" +
                DateTimeFormatter.ofPattern("yyyy-MM-dd").format(java.time.LocalDate.now(ZoneId.systemDefault())) +
                ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return new ResponseEntity<>(workbook, headers, HttpStatus.OK);
    }
}