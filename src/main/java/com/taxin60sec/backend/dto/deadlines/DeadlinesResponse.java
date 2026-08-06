package com.taxin60sec.backend.dto.deadlines;

import java.time.LocalDate;
import java.util.List;

public record DeadlinesResponse(
        List<DeadlineItem> deadlines
) {
    public record DeadlineItem(
            String type,
            String title,
            LocalDate dueDate,
            int daysRemaining
    ) {}
}