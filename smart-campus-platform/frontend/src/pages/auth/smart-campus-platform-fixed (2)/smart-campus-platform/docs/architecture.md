# Architecture

## Overview

The Smart Campus Platform is a **layered monolith**: one Spring Boot application, one database, one React
client. Everything below is a consequence of that choice.

```
┌──────────────────────────────────────────────────────────┐
│                    Browser (React SPA)                   │
│  Pages → Components → Services (axios) → AuthContext     │
└───────────────────────────┬──────────────────────────────┘
                            │ HTTPS · JSON · Bearer JWT
┌───────────────────────────▼──────────────────────────────┐
│                  Spring Boot application                 │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │ Security filter chain                              │  │
│  │   JwtAuthenticationFilter → SecurityContext        │  │
│  └────────────────────────┬───────────────────────────┘  │
│  ┌────────────────────────▼───────────────────────────┐  │
│  │ Controller layer       @RestController             │  │
│  │   HTTP mapping, @Valid, @PreAuthorize              │  │
│  └────────────────────────┬───────────────────────────┘  │
│  ┌────────────────────────▼───────────────────────────┐  │
│  │ Service layer          @Service @Transactional     │  │
│  │   business rules, ownership checks, orchestration  │  │
│  └────────────────────────┬───────────────────────────┘  │
│  ┌────────────────────────▼───────────────────────────┐  │
│  │ Repository layer       Spring Data JPA             │  │
│  └────────────────────────┬───────────────────────────┘  │
│  ┌────────────────────────▼───────────────────────────┐  │
│  │ Entity layer           JPA @Entity                 │  │
│  └────────────────────────┬───────────────────────────┘  │
└───────────────────────────┼──────────────────────────────┘
                ┌───────────┴───────────┐
        ┌───────▼────────┐     ┌────────▼────────┐
        │  PostgreSQL    │     │   Groq API      │
        └────────────────┘     └─────────────────┘
```

## Layer responsibilities

| Layer | Does | Never does |
|---|---|---|
| Controller | Maps HTTP, validates the body, checks the role, returns a DTO | Business logic, database access |
| Service | Business rules, transactions, ownership checks, mapping to DTOs | HTTP concerns, SQL strings |
| Repository | Queries, derived and JPQL | Business decisions |
| Entity | The persistent model | Leaving the service layer |

A DTO is what crosses the controller boundary in both directions. This is the mechanism that keeps the
BCrypt password hash out of every API response: `User` has a `password` field, `StudentResponse` does not.

## Request flow

```
POST /api/attendance/sessions/42/detect
  │
  ├─ CorsFilter                     origin allowed?
  ├─ JwtAuthenticationFilter        token valid? → load user → SecurityContext
  ├─ AttendanceController           @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
  ├─ AttendanceService (Demo|Ble)   resolve student, check enrolment, prevent duplicate
  ├─ AttendanceRecordRepository     insert
  └─ AttendanceMapper               → AttendanceSessionResponse (JSON)

any exception → GlobalExceptionHandler → ApiError JSON
```

## Key patterns

**Strategy for attendance.** `AttendanceService` is an interface with two implementations selected at
start-up by `@ConditionalOnProperty`. Shared lifecycle logic lives in `AbstractAttendanceService`; only
`resolveStudent` differs. Adding QR-code or face-recognition attendance later means one new class, no
changes to controllers, schema or UI.

**Dependency injection through constructors.** Every collaborator arrives via the constructor
(`@RequiredArgsConstructor`), which makes dependencies explicit and lets the unit tests pass mocks in.

**Centralised error handling.** `@RestControllerAdvice` turns every exception into one `ApiError` shape,
so no controller contains a try/catch.

**Aggregation in the database.** Subject-wise attendance is one grouped JPQL query returning a projection,
not a loop issuing a query per subject.

## Frontend structure

```
main.jsx → BrowserRouter → ToastProvider → AuthProvider → App → AppRoutes
                                                                   │
                        ┌──────────────────────────────────────────┤
                        │                                          │
                 public routes                          ProtectedRoute(role)
                 /login /register                                  │
                                                          DashboardLayout
                                                        (Sidebar + Navbar)
                                                                   │
                                                             role pages
```

`AuthContext` holds the signed-in user. Only the JWT is stored in `localStorage`; the role is always read
back from `GET /api/auth/me`, so editing local storage changes nothing the server will honour.

`useApiData` gives every screen the same loading / error / data states, and a single axios instance attaches
the token and redirects to `/login` on a 401.

## Deployment

```
docker compose up --build

  smartcampus-db        postgres:16      :5432
  smartcampus-backend   JDK 17 + jar     :8080
  smartcampus-frontend  nginx + build    :3000
```

The backend waits for the database health check before starting. The frontend image is a static nginx build
with a SPA fallback to `index.html`.
