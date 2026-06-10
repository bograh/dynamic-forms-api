# DynamicForms

A REST API for building and submitting dynamic forms. Admins define form schemas with typed fields and validation rules; authenticated users submit responses that are validated server-side against the live schema.

## Tech Stack

- **Java 21** / **Spring Boot 4**
- **Spring Security** — stateless JWT auth, Argon2 password hashing
- **PostgreSQL** — JSONB columns for field options, validation rules, and submission data
- **Flyway** — versioned schema migrations
- **Hibernate 7** — JPA with schema validation on startup

## Features

- Register / login / refresh token (HttpOnly cookie, rotated on every refresh)
- Admin: create forms, define typed fields with per-field validation rules, publish forms
- Public: fetch published form schema by slug
- Authenticated: submit responses with server-side validation against the live schema
- Paginated submission history per form

## Prerequisites

- Java 21+
- PostgreSQL running on `localhost:5432`

## Setup

1. Create the database and user:

```sql
CREATE USER app WITH PASSWORD 'apppassword';
CREATE DATABASE dynamicforms OWNER app;
```

2. Run the application — Flyway applies migrations automatically on startup:

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

## API Overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/register` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Login, returns access token |
| `POST` | `/api/auth/refresh-token` | Cookie | Rotate refresh token |
| `GET` | `/api/forms/{slug}` | Bearer | Fetch published form schema |
| `POST` | `/api/forms/{id}/submit` | Bearer | Submit a form response |
| `POST` | `/api/admin/forms` | Admin | Create a form |
| `PUT` | `/api/admin/forms/{id}` | Admin | Update form metadata |
| `PUT` | `/api/admin/forms/{id}/fields` | Admin | Replace form fields |
| `POST` | `/api/admin/forms/{id}/publish` | Admin | Publish a form |
| `GET` | `/api/admin/forms/{id}/submissions` | Admin | List submissions (paginated) |

Access tokens go in the `Authorization: Bearer <token>` header. The refresh token is set and read as an HttpOnly cookie scoped to `/api/auth/refresh-token`.

## Configuration

Key properties in `application.yaml`:

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/dynamicforms` | Database URL |
| `security-jwt.access-token-expiration-ms` | `900000` | Access token TTL (15 min) |
| `security-jwt.refresh-token-expiration-ms` | `604800000` | Refresh token TTL (7 days) |
| `allowed.cors.url` | `*` | Allowed CORS origin |

Secrets (`access-token-secret`, `refresh-token-secret`) should be replaced with strong random values before deploying.
