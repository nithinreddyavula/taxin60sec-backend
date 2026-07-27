package com.taxin60sec.backend.entity.enums;

/**
 * Self-declared revenue band, collected in the free Tax Health Score check.
 * Used to flag GST-registration risk (India's GST threshold is roughly ₹20L
 * for services / ₹40L for goods, so anyone above UNDER_20L who is not GST
 * registered gets flagged) and to route higher-band leads to Virtual CFO / NRI.
 */
public enum RevenueBand {
    UNDER_20L,
    BETWEEN_20L_AND_1CR,
    BETWEEN_1CR_AND_5CR,
    ABOVE_5CR
}
