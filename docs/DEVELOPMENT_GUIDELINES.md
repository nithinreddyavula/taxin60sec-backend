# Development Guidelines

## Coding Standards

- Use package names under `com.taxin60sec.backend`.
- Controllers end with `Controller`.
- Service interfaces end with `Service`.
- Service implementations end with `ServiceImpl`.
- Repositories end with `Repository`.
- Request DTOs end with `Request`.
- Response DTOs end with `Response`.
- Exceptions end with `Exception`.

## API Rules

- New public APIs must be versioned under `/api/v1`.
- New APIs must return `ApiResponse<T>`.
- Never expose JPA entities directly from versioned APIs.
- Validate request DTOs with Jakarta Validation.
- Use `GlobalExceptionHandler` for error responses.

## Service Rules

- Controllers should not call repositories directly.
- Put transactional boundaries on service methods.
- Keep services focused on orchestration and rules.
- Keep mappers free of business decisions.

## Persistence Rules

- Extend `BaseEntity` for persisted domain objects.
- Prefer enums for stable status fields.
- Avoid business-specific table changes until feature requirements are clear.
- Introduce migrations before production schema changes become complex.

## Future Integrations

Do not couple controllers or services directly to vendors.

- Storage goes through `StorageService`.
- Email, WhatsApp, and SMS go through `NotificationService`.
- Razorpay or any payment provider goes through `PaymentService`.
- FastAPI or model vendors go through `AIService`.
- Case orchestration goes through `WorkflowService`.

## What Not To Add Yet

- ITR logic
- GST logic
- Pricing calculation
- WhatsApp Business API implementation
- Razorpay implementation
- OCR
- AI workflow logic
