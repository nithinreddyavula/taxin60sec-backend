# Contribution Guide

## Before You Code

1. Confirm the feature belongs in the backend.
2. Identify the target package and API version.
3. Check whether an extension interface already exists.
4. Keep the change small and focused.

## Pull Request Checklist

- Code compiles with `./mvnw test`.
- New APIs use request/response DTOs.
- New APIs return `ApiResponse<T>`.
- Validation errors are handled by `GlobalExceptionHandler`.
- Controllers do not call repositories directly.
- No secrets are committed.
- Documentation is updated when architecture or setup changes.

## Review Priorities

1. Security and data exposure.
2. API compatibility.
3. Transaction boundaries.
4. Validation coverage.
5. Simplicity and maintainability.
