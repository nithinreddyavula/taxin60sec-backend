package com.taxin60sec.backend.dto.healthscore;

import com.taxin60sec.backend.entity.enums.ScoreBand;

import java.util.List;

public record TaxHealthCheckResponse(

        String shareToken,

        int score,

        ScoreBand scoreBand,

        String headline,

        List<String> issues,

        List<String> recommendations,

        // where the "Fix this now" button should send them
        String ctaServiceCode,

        String ctaLabel,

        String referralCode,

        String referralShareUrl,

        boolean whatsappDelivered

) {}
