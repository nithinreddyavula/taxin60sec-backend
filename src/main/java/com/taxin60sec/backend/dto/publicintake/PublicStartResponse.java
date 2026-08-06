package com.taxin60sec.backend.dto.publicintake;

import java.util.List;
import java.util.Map;

public record PublicStartResponse(

        Long caseId,

        String intakeToken,

        String customerName,

        String serviceName,

        List<String> questions,

        Map<String,String> answers

) {
}