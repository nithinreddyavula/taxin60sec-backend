package com.taxin60sec.backend.service;

import com.taxin60sec.backend.dto.admin.AdminCaseDetailResponse;
import com.taxin60sec.backend.dto.admin.AdminCaseSummaryResponse;

import java.util.List;

public interface AdminCaseService {

    List<AdminCaseSummaryResponse> getAllCases();

    AdminCaseDetailResponse getCase(Long caseId);

    void updateStatus(
            Long caseId,
            String status
    );

}