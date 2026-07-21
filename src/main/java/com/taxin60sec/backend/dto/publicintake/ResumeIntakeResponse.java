package com.taxin60sec.backend.dto.publicintake;

import java.util.Map;

public record ResumeIntakeResponse(

        Long caseId,

        String clientName,

        String serviceName,

        boolean completed,

        int answeredCount,

        int totalQuestions,

        String currentQuestion,

        Map<String,String> answers

) {
}