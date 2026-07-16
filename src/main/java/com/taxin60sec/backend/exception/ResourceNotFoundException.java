package com.taxin60sec.backend.exception;

import com.taxin60sec.backend.common.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, message);
    }
}
