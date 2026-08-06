package com.taxin60sec.backend.exception;

import com.taxin60sec.backend.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final ApiErrorCode errorCode;

    public ApiException(HttpStatus status, ApiErrorCode errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }
}
