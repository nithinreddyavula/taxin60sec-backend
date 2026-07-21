package com.taxin60sec.backend.dto.publicintake;

import java.util.Map;

public record PublicAnswerResponse(

        Long caseId,

        boolean completed,

        int answeredCount,

        int totalQuestions,

        String nextQuestion,

        Map<String, String> answers

) {
}