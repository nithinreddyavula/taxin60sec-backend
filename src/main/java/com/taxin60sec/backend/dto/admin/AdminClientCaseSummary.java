package com.taxin60sec.backend.dto.admin;

import java.time.Instant;

public record AdminClientCaseSummary(
        Long caseId,
        String caseNumber,
        String serviceName,
        String status,
        Instant createdAt
) {}