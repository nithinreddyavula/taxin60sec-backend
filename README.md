# Tax60 Backend

Production-ready backend foundation for the Tax60 AI-assisted Chartered Accountant service platform.

Tax60 does not replace Chartered Accountants. It automates intake, document collection, communication, and operational workflows before a CA begins final review and decision-making.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven
- OpenAPI / Swagger

## Local Development

```bash
./mvnw spring-boot:run
```

Default local database:

```properties
DATABASE_URL=jdbc:postgresql://localhost:5432/taxin60sec
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres123
```

Health check:

```text
GET /actuator/health
```

Swagger UI:

```text
GET /swagger-ui.html
```

## API Contracts

New APIs should live under `/api/v1`.

Standard response shape:

```json
{
  "success": true,
  "message": "Operation completed",
  "data": {},
  "path": "/api/v1/example",
  "timestamp": "2026-07-16T10:00:00Z"
}
```

Validation and server errors use the same envelope with `success=false` and an `errors` array.

## Compatibility

The existing `/api/contact` endpoints are preserved as legacy compatibility endpoints for the current frontend/admin UI. New development should use `/api/v1/contacts`.

## Package Structure

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the modular monolith structure and extension points.
