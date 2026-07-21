package com.taxin60sec.backend.dto.admin;

public record AdminDashboardResponse(

        long totalCases,

        long draft,

        long intake,

        long documentCollection,

        long caReview,

        long inProgress,

        long completed,

        long cancelled

) {}