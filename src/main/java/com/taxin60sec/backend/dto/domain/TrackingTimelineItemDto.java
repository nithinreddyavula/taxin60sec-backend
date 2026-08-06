package com.taxin60sec.backend.dto.domain;

import java.time.Instant;

public record TrackingTimelineItemDto(String title, String description, Instant occurredAt) {}