package com.taxin60sec.backend.dto.admin;

import java.time.LocalDateTime;

public record AdminCaseSummaryResponse(

        Long caseId,

        String clientName,

        String serviceName,

        String status,

        boolean intakeCompleted,

        int answeredQuestions,

        int totalQuestions,

        LocalDateTime createdAt

) {}