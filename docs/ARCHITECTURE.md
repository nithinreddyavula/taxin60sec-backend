# Tax60 Backend Architecture

## Architecture Style

Tax60 uses a modular monolith. This keeps deployment simple for an early-stage SaaS product while allowing clear module boundaries for future extraction if scale demands it.

## Request Flow

Controller -> Service Interface -> Service Implementation -> Repository -> Entity

DTOs are used at API boundaries. Entities are not exposed by new versioned APIs.

## Package Responsibilities

- `config`: framework and application configuration.
- `controller`: REST API entry points.
- `service`: business-facing service contracts.
- `service.impl`: service implementations.
- `repository`: persistence access through Spring Data JPA.
- `entity`: JPA entities and enums.
- `entity.base`: reusable persistence base classes.
- `dto`: request and response payloads.
- `mapper`: entity/DTO conversion.
- `security`: Spring Security configuration and future auth components.
- `exception`: global exception handling and typed application exceptions.
- `common`: reusable API response and error contracts.
- `validation`: validation groups and shared validation rules.
- `utils`: stateless utility classes.
- `workflow`: future case lifecycle orchestration.
- `storage`: future object storage abstraction.
- `notification`: future email/WhatsApp/SMS abstraction.
- `payment`: future payment provider abstraction.
- `ai`: future AI service abstraction.

## Design Decisions

1. Keep a modular monolith.
   This avoids premature microservices while preserving clean internal boundaries.

2. Preserve legacy `/api/contact`.
   The existing frontend/admin integration keeps working. New clients should use `/api/v1/contacts` with standard response envelopes.

3. Use DTOs and mappers for APIs.
   This prevents persistence details from leaking into public contracts.

4. Use interfaces for volatile integrations.
   Storage, notifications, payments, AI, and workflow orchestration will change over time, so the application depends on interfaces.

5. Keep security configured but permissive for now.
   Authentication and authorization are not implemented yet. The app is stateless, CORS-aware, and ready for JWT/session integration without blocking the current product.

6. Use thin base entities.
   `BaseEntity` provides id, audit timestamps, and optimistic locking. Domain logic will be added later in services and aggregates as requirements mature.

## Foundational Entities

- `User`: future client, CA, admin, and operations user.
- `Role`: role metadata for authorization.
- `Case`: future client service case.
- `ServiceOffering`: catalog entry for services such as GST, ITR, accounting, and advisory.
- `Document`: document metadata and storage key.
- `Timeline`: case activity history.
- `Payment`: payment state and provider reference.
- `Notification`: outbound communication record.

These entities intentionally contain no business workflow logic yet.
