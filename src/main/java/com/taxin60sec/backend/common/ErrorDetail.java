package com.taxin60sec.backend.common;

public record ErrorDetail(
        String code,
        String field,
        String message
) {
}
