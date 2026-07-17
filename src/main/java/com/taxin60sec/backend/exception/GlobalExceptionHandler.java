package com.taxin60sec.backend.exception;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.ErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.taxin60sec.backend.storage.StorageException;
import com.taxin60sec.backend.security.ForbiddenException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception, HttpServletRequest request) {
        log.warn("Handled API exception: {}", exception.getMessage());
        ErrorDetail error = new ErrorDetail(exception.getErrorCode().name(), null, exception.getMessage());
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiResponse.error(exception.getMessage(), List.of(error), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ErrorDetail> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorDetail(ApiErrorCode.VALIDATION_ERROR.name(), error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Validation failed", errors, request.getRequestURI()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        List<ErrorDetail> errors = exception.getConstraintViolations()
                .stream()
                .map(error -> new ErrorDetail(ApiErrorCode.VALIDATION_ERROR.name(), error.getPropertyPath().toString(), error.getMessage()))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Validation failed", errors, request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(AuthenticationCredentialsNotFoundException exception, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(ApiErrorCode.UNAUTHORIZED.name(), null, "Authentication is required");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized", List.of(error), request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(AccessDeniedException exception, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(ApiErrorCode.FORBIDDEN.name(), null, "Access denied");
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Forbidden", List.of(error), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception", exception);
        ErrorDetail error = new ErrorDetail(ApiErrorCode.INTERNAL_SERVER_ERROR.name(), null, "Unexpected server error");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", List.of(error), request.getRequestURI()));
    }

    @ExceptionHandler(StorageException.class)
public ResponseEntity<ApiResponse<Void>> handleStorageException(
        StorageException exception,
        HttpServletRequest request) {

    ErrorDetail error = new ErrorDetail(
            ApiErrorCode.STORAGE_ERROR.name(),
            null,
            exception.getMessage()
    );

    return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(
                    "Storage operation failed",
                    List.of(error),
                    request.getRequestURI()
            ));
}
}
