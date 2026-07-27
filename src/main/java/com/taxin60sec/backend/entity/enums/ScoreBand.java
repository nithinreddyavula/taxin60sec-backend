package com.taxin60sec.backend.entity.enums;

/**
 * Human-readable band for a Tax Health Score result (0-100).
 * Drives the headline copy and the urgency of the CTA shown to the user.
 */
public enum ScoreBand {
    EXCELLENT,        // 85-100
    GOOD,             // 70-84
    NEEDS_ATTENTION,  // 50-69
    AT_RISK,          // 30-49
    CRITICAL          // 0-29
}
