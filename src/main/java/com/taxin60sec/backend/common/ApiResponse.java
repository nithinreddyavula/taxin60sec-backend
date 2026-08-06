package com.taxin60sec.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Object errors,
        String path,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(String message, T data, String path) {
        return new ApiResponse<>(true, message, data, null, path, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message, Object errors, String path) {
        return new ApiResponse<>(false, message, null, errors, path, Instant.now());
    }
}
