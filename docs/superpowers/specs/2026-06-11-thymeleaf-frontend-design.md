# Thymeleaf Frontend Design

**Date:** 2026-06-11  
**Project:** DynamicForms  
**Status:** Approved

---

## Overview

Add a server-rendered Thymeleaf frontend to the existing DynamicForms Spring Boot REST API. The web layer integrates with the existing API via a session-JWT relay pattern: Thymeleaf controllers call REST endpoints through a `WebApiClient` wrapper that injects the access token from `HttpSession` as a Bearer header. The existing REST API (`/api/**`) remains unchanged.

**Tech choices:**
- Thymeleaf (server-rendered templates)
- Tailwind CSS (CDN play mode for development)
- Vanilla JS (admin field editor only)
- RestTemplate (synchronous HTTP client)
- Spring Security `HttpSession` (web layer auth)

---

## 1. Architecture & Package Structure

A new `web` package sits alongside the existing `auth`, `form`, `submission` packages.

```
src/main/java/dev/ograh/dynamicforms/
└── web/
    ├── client/
    │   ├── WebApiClient.java          # RestTemplate wrapper; reads JWT from session, adds Bearer header, handles 401 refresh
    │   └── ApiException.java          # Wraps non-2xx API responses for the web layer
    ├── controller/
    │   ├── WebAuthController.java     # GET/POST /login, /register, /logout
    │   ├── WebFormController.java     # GET /forms, GET /forms/{slug}, POST /forms/{id}/submit
    │   └── WebAdminController.java    # /admin/forms/**, /admin/forms/{id}/fields, /admin/forms/{id}/submissions
    ├── security/
    │   └── WebSecurityConfig.java     # Second SecurityFilterChain for the web layer (form login, session-based)
    └── config/
        └── WebMvcConfig.java          # RestTemplate bean

src/main/resources/
├── templates/
│   ├── layout/
│   │   └── base.html                 # Shared Thymeleaf fragment: nav, head, footer
│   ├── auth/
│   │   ├── login.html
│   │   └── register.html
│   ├── forms/
│   │   ├── list.html                 # Browse published forms (user)
│   │   └── view.html                 # Fill out and submit a form
│   └── admin/
│       ├── forms/
│       │   ├── list.html             # All forms with status badges
│       │   ├── create.html
│       │   ├── edit.html
│       │   └── fields.html           # JS-driven field editor
│       └── submissions/
│           └── list.html             # Paginated submissions table
└── static/
    ├── css/
    │   └── app.css                   # Tailwind output
    └── js/
        └── field-editor.js           # Vanilla JS field builder
```

---

## 2. Security Configuration

Two separate `SecurityFilterChain` beans coexist without conflict using `@Order`:

| Chain | Order | Matches | Auth mechanism |
|-------|-------|---------|----------------|
| `apiSecurityFilterChain` | 1 | `/api/**` | Stateless JWT (existing, unchanged) |
| `webSecurityFilterChain` | 2 | Everything else | Session-based form login |

**Web chain access rules:**
- Public: `GET /login`, `GET /register`, `POST /login`, `POST /register`, static assets
- Authenticated session required: `/forms/**`
- `ADMIN` authority required: `/admin/**`
- Default success URL after login: `/forms`
- Logout: `POST /logout` → clears session → redirects to `/login`

**Login flow:**
1. User POSTs credentials to `POST /login` (handled by `WebAuthController`)
2. Controller calls `POST /api/auth/login` via `WebApiClient`
3. API returns `AuthResponse` (access token + user details) and sets HttpOnly refresh cookie
4. Controller stores `accessToken`, `role`, `name`, `email` in `HttpSession`
5. Spring Security session marks the user as authenticated with their role
6. Redirects to `/forms`

**Registration flow:** Same as login — calls `POST /api/auth/register`, then auto-logs in.

**Token refresh (transparent):**
- `WebApiClient` detects `401` from the API
- Calls `POST /api/auth/refresh-token`, explicitly reading the `refreshToken` HttpOnly cookie from the current `HttpServletRequest` (via `WebUtils.getCookie`) and attaching it to the outgoing RestTemplate request as a `Cookie` header
- On success: updates `accessToken` in `HttpSession`, retries original call once
- On refresh failure: clears session, redirects to `/login?expired=true`

---

## 3. Pages & Data Flow

### Auth Pages

| Page | Route | API call |
|------|-------|----------|
| Login | `GET/POST /login` | `POST /api/auth/login` |
| Register | `GET/POST /register` | `POST /api/auth/register` |

Errors (wrong password, duplicate email) rendered as inline messages from the API `ErrorResponse`.

### User-Facing Pages

| Page | Route | API call | Notes |
|------|-------|----------|-------|
| Form list | `GET /forms` | `GET /api/forms` | Lists all PUBLISHED forms as cards |
| Form view | `GET /forms/{slug}` | `GET /api/forms/{slug}` | Fields rendered via `th:switch` on `fieldType` |
| Submit | `POST /forms/{id}/submit` | `POST /api/forms/{id}/submit` | Shows success or field-level errors inline |

**Dynamic form rendering:** Thymeleaf iterates `fields` and uses `th:switch` on `fieldType` to render the correct HTML element (`input`, `textarea`, `select`, `radio`, `checkbox`). HTML5 validation attributes (`min`, `max`, `pattern`, `minlength`, `maxlength`) are rendered from `FieldValidation`. Server-side validation is handled by the existing `FormSubmissionValidator`.

### Admin Pages

| Page | Route | API call |
|------|-------|----------|
| Form list | `GET /admin/forms` | `GET /api/admin/forms` |
| Create form | `GET/POST /admin/forms/new` | `POST /api/admin/forms` |
| Edit form | `GET/POST /admin/forms/{id}/edit` | `PUT /api/admin/forms/{id}` |
| Field editor | `GET /admin/forms/{id}/fields` | `GET /api/admin/forms/{id}` (load) |
| Save fields | JS `fetch` → `PUT /api/admin/forms/{id}/fields` | No page reload |
| Publish | `POST /admin/forms/{id}/publish` | `POST /api/admin/forms/{id}/publish` |
| Submissions | `GET /admin/forms/{id}/submissions` | `GET /api/admin/forms/{id}/submissions` (paginated) |

**Admin field editor:** Thymeleaf renders the initial field list on page load. `field-editor.js` handles add/remove/reorder field rows and options for SELECT/RADIO/CHECKBOX types. On save, it serialises the field array and calls the API via `fetch()`, showing an inline toast on success or error.

---

## 4. New REST Endpoints Required

Two endpoints must be added to the existing REST layer to support the web layer:

| Method | Path | Controller | Auth | Description |
|--------|------|-----------|------|-------------|
| `GET` | `/api/forms` | `FormController` | Any authenticated user | Returns all `PUBLISHED` forms |
| `GET` | `/api/admin/forms` | `FormAdminController` | ADMIN | Returns all forms (any status), paginated |

---

## 5. Error Handling

- `WebApiClient` throws `ApiException` on non-2xx responses, carrying status code and the API's `ErrorResponse` body.
- Web controllers catch `ApiException` and add errors to the Spring `Model` for Thymeleaf inline rendering.
- Field-level validation errors (from `errors` map in `ErrorResponse`) are rendered under each input on the form submit page.
- `401` with failed refresh → clears session, redirects to `/login?expired=true`.
- `403` → renders `error/403.html`.
- Unexpected errors → renders `error/500.html`.
- JS fetch errors in `field-editor.js` → inline toast notification (no page reload).

---

## 6. Testing

| Scope | Approach |
|-------|----------|
| `WebApiClient` | Unit tests with `MockRestServiceServer`; verify Bearer header injection, 401 refresh retry, session update |
| Web controllers | `@WebMvcTest` with mocked `WebApiClient`; verify redirects, model attributes, template selection |
| REST layer | Existing `@SpringBootTest` integration tests remain untouched |

---

## 7. Dependencies to Add (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

Tailwind CSS via CDN play script in `base.html` (no build step required for development).
