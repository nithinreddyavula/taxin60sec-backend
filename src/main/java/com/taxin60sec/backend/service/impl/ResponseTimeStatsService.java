package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.publicintake.ResponseTimeStatsResponse;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.repository.CaseRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class ResponseTimeStatsService {

    private static final long SLA_SECONDS = 60;

    private final CaseRepository cases;

    public ResponseTimeStatsService(CaseRepository cases) {
        this.cases = cases;
    }

    public ResponseTimeStatsResponse currentStats() {
        List<Case> responded = cases.findTop200ByFirstResponseAtIsNotNullAndDeletedFalseOrderByFirstResponseAtDesc();

        if (responded.isEmpty()) {
            return new ResponseTimeStatsResponse(0, null, null, (int) SLA_SECONDS);
        }

        long total = 0;
        long withinSla = 0;

        for (Case c : responded) {
            long seconds = Duration.between(c.getCreatedAt(), c.getFirstResponseAt()).getSeconds();
            total += seconds;
            if (seconds <= SLA_SECONDS) {
                withinSla++;
            }
        }

        long avg = total / responded.size();
        double pct = (withinSla * 100.0) / responded.size();

        return new ResponseTimeStatsResponse(responded.size(), avg, Math.round(pct * 10.0) / 10.0, (int) SLA_SECONDS);
    }
}