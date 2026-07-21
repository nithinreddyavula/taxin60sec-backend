package com.taxin60sec.backend.dto.admin;

import java.util.Map;

public record AdminCaseDetailResponse(

        Long caseId,

        String clientName,

        String email,

        String phone,

        String serviceName,

        String status,

        boolean intakeCompleted,

        Map<String,String> answers,

        String intakeSummary

) {}