package com.taxin60sec.backend.dto.domain;

import java.time.LocalDateTime;

public record CaseTrackingLinkDto(String caseNumber, String token, LocalDateTime expiresAt) {}