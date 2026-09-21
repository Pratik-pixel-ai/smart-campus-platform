# Smart Campus Platform

A single web platform that replaces the scattered registers, WhatsApp groups and notice boards a college
runs on today. It brings attendance, timetable, assignments, results and announcements into one place,
with three role-based dashboards and an AI assistant that answers questions from the user's own campus data.

Built as a final-year BE (Information Technology) project at Trinity College of Engineering and Research, Pune.

| | |
|---|---|
| **Backend** | Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA, JWT |
| **Frontend** | React 18, Vite, JavaScript, Tailwind CSS, React Router, Axios, Recharts |
| **Database** | PostgreSQL 16 |
| **AI** | Groq chat completions API, called from Java only |
| **Project group** | PR2026-27_01 |

---

## Table of contents

1. [What it does](#what-it-does)
2. [Screens by role](#screens-by-role)
3. [Architecture](#architecture)
4. [Getting started](#getting-started)
5. [Demo accounts](#demo-accounts)
6. [Environment variables](#environment-variables)
7. [How BLE attendance works](#how-ble-attendance-works)
8. [How the AI assistant works](#how-the-ai-assistant-works)
9. [Security model](#security-model)
10. [API reference](#api-reference)
11. [Project structure](#project-structure)
12. [Running the tests](#running-the-tests)
13. [Design decisions and deviations](#design-decisions-and-deviations)
14. [Limitations and future work](#limitations-and-future-work)

---

## What it does

**The problem.** Attendance is taken on paper and typed into a spreadsheet days later. Students find out
they are short of the 75% requirement when it is too late to fix. Timetables, assignment deadlines and
notices live in different places, and nobody has a single view of any of it.

**The solution.** One platform where:

- attendance is captured in the classroom and the percentage updates immediately;
- a student can see exactly how many more lectures they can afford to miss;
- faculty set assignments, collect submissions and enter marks in one flow;
- the administration sees department-level numbers without asking anyone for a report;
- anyone can ask the assistant a plain question ("am I below 75% in any subject?") and get an answer
  built from their own records.

### Modules

| Module | What it covers |
|---|---|
| Authentication | Registration, login, JWT issue and validation, role resolution |
| User management | Students, faculty, departments, subjects, classrooms |
| Timetable | Weekly lecture slots per class, today's classes per user |
| Attendance | Sessions, BLE/demo detection, absentee marking on close, percentages |
| Assignments | Creation, submission by link, grading with feedback |
| Academics | Internal and external marks, totals, grades, semester summaries |
| Announcements | Campus-wide and department notices with priority |
| Notifications | In-app alerts when work is set or graded |
| AI assistant | Role-scoped question answering over the user's campus data |
| Dashboards | One aggregated endpoint per role |

---

## Screens by role

**Student** — dashboard, attendance (subject-wise plus full history), timetable, assignments, academic
record, announcements, assistant, profile.

**Faculty** — dashboard, take attendance, student directory, timetable, assignments (with submissions and
grading), marks entry, announcements, assistant, profile.

**Admin** — campus dashboard, students, faculty, departments, subjects, classrooms, timetable, attendance
log, assignments, announcements, reports with CSV export, assistant, profile.

---

## Architecture

```
┌─────────────────────────────┐
│  React + Vite (browser)     │
│  Tailwind · Router · Axios  │
└──────────────┬──────────────┘
               │  JSON over HTTPS, JWT in the Authorization header
┌──────────────▼──────────────┐
│  Spring Boot monolith       │
│                             │
│  Controller  ← REST, validation, @PreAuthorize
│  Service     ← business rules, transactions
│  Repository  ← Spring Data JPA
│  Entity      ← JPA model
└──────┬───────────────┬──────┘
       │               │
┌──────▼──────┐  ┌─────▼────────┐
│ PostgreSQL  │  │  Groq API    │
└─────────────┘  └──────────────┘
```

One deployable unit. The layering is strict: a controller never touches a repository, and an entity never
leaves the service layer — DTOs cross that boundary, which is what keeps password hashes out of responses.

Full diagrams (use case, class, ER, sequence) are in [`docs/`](docs/).

---

## Getting started

### Option A — Docker (everything at once)

```bash
git clone <your-repo-url> smart-campus-platform
cd smart-campus-platform
cp .env.example .env          # edit JWT_SECRET, and GROQ_API_KEY if you want the assistant
docker compose up --build
```

- Frontend: <http://localhost:3000>
- API: <http://localhost:8080/api>
- PostgreSQL: `localhost:5432`

The database schema is created from the JPA entities on first start, and demo data is seeded automatically.

### Option B — run the parts yourself

**Prerequisites:** JDK 17+, Maven 3.9+, Node.js 18+, PostgreSQL 14+.

**1. Database**

```sql
CREATE DATABASE smartcampus;
CREATE USER smartcampus WITH PASSWORD 'smartcampus';
GRANT ALL PRIVILEGES ON DATABASE smartcampus TO smartcampus;
```

**2. Backend**

```bash
cd backend
export DATABASE_URL=jdbc:postgresql://localhost:5432/smartcampus
export DATABASE_USERNAME=smartcampus
export DATABASE_PASSWORD=smartcampus
export JWT_SECRET=a-long-random-string-of-at-least-32-characters
export GROQ_API_KEY=            # optional
mvn spring-boot:run
```

Runs on <http://localhost:8080>. On Windows use `set` instead of `export`.

**3. Frontend**

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Runs on <http://localhost:5173>.

---

## Demo accounts

Seeded on first start. All three share the same password.

| Role | Email | Password |
|---|---|---|
| Admin | `admin@smartcampus.com` | `Demo@1234` |
| Faculty | `faculty@smartcampus.com` | `Demo@1234` |
| Student | `student@smartcampus.com` | `Demo@1234` |

The login page has one-click buttons for all three.

**Seeded data:** 2 departments, 3 faculty, 12 students, 6 subjects, 3 classrooms, 15 timetable slots,
36 closed attendance sessions with records, 4 assignments with submissions (two already graded),
3 announcements and a full set of academic records.

Turn seeding off with `SEED_ENABLED=false`. It is skipped automatically if the database already has users.

> Change `Demo@1234` before putting this anywhere real. It exists so a reviewer can sign in in five seconds.

---

## Environment variables

| Variable | Default | Purpose |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/smartcampus` | JDBC URL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | `smartcampus` | Database credentials |
| `JWT_SECRET` | *(empty)* | HS256 signing key, minimum 32 characters. Empty → a random key is generated per run and a warning is logged |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:4173` | Comma-separated origins allowed to call the API |
| `ATTENDANCE_MODE` | `demo` | `demo` or `ble` — selects the attendance implementation |
| `SEED_ENABLED` | `true` | Load demo data on an empty database |
| `GROQ_API_KEY` | *(empty)* | Enables the assistant. Empty → the app runs and reports the assistant as not configured |
| `GROQ_MODEL` | `llama-3.3-70b-versatile` | Model used for the assistant |
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Where the frontend sends requests |

No secret has a hard-coded default anywhere in the source.

---

## How BLE attendance works

**Be clear about this in the viva: the platform ships with two attendance implementations, and the one
enabled by default is simulated.**

### The design

Attendance capture sits behind an interface:

```
AttendanceService              (interface: createSession, detect, closeSession, getSession, mode)
   │
   └── AbstractAttendanceService   (session lifecycle, enrolment check, duplicate prevention, persistence)
         ├── DemoAttendanceService   ATTENDANCE_MODE=demo  (default)
         └── BleAttendanceService    ATTENDANCE_MODE=ble
```

Everything except one step is shared. The step that differs is `resolveStudent` — turning a detection
event into a student:

- **DemoAttendanceService** takes a `studentId` sent by the faculty screen. No Bluetooth is involved.
- **BleAttendanceService** takes a `bleDeviceId` and an `rssi` from a real scan, looks the device up in
  the indexed `ble_device_id` column, and rejects anything weaker than `app.attendance.min-rssi` (-85 dBm)
  so a phone in the corridor is not counted as present.

Spring picks the implementation at start-up with `@ConditionalOnProperty`. The controllers, the database
schema and the frontend are identical either way — only the source of the detection changes.

### What "demo mode" actually means

The faculty attendance screen shows an amber banner reading **"Demo BLE detection"** and explains that no
Bluetooth hardware is in use. Pressing *Simulate detection* next to a student posts exactly the same
payload a real scanner would post, so the full flow — session open → detections arrive → session closed →
absentees written — is demonstrated end to end.

Every record stores how it was created in `detection_method`: `DEMO`, `BLE`, `MANUAL` or `SYSTEM`. Simulated
attendance is never presented as a hardware-verified scan, in the UI or in the database.

### Getting to real BLE

The backend is finished for real scanning. What is missing is the client that collects advertisements:

1. Register each student's device identifier (admin → students → BLE device id; the demo data already does this).
2. Set `ATTENDANCE_MODE=ble` and restart the backend.
3. Have the faculty device scan for advertisements and POST each one:

```http
POST /api/attendance/sessions/42/detect
Authorization: Bearer <faculty token>

{ "bleDeviceId": "BLE-IT-3", "rssi": -63 }
```

A Web Bluetooth page in the browser, or a small Android scanner app, can produce these. That client is
outside the scope of this repository, which is deliberate: it keeps the platform working on any machine
during a review, and the attendance logic under test does not change when the hardware arrives.

### The lifecycle

1. Faculty starts a session (subject, classroom, lecture number, duration, semester, division). Status `OPEN`.
2. Detections arrive. Each one marks that student `PRESENT`. A unique constraint on
   `(session_id, student_id)` makes a repeated detection a harmless no-op rather than a duplicate row.
3. Faculty closes the session. The class list (same department, semester and division) is compared against
   the students already marked present, and everyone else is written as `ABSENT` with method `SYSTEM`.
   Status `CLOSED`.
4. Percentages are recalculated from the records by the database aggregation query, not in the browser.

**Attendance percentage** = present lectures ÷ total lectures recorded, per subject and overall.

**Lectures you can still miss** — from `present / (total + x) ≥ 0.75`:

```
x = floor( present / 0.75 − total )
```

18 present out of 20 → `18/0.75 − 20 = 4`. Four more lectures may be missed before dropping below 75%.

---

## How the AI assistant works

```
Browser  ──"What is my attendance?"──▶  AiController
                                            │ (JWT identifies the user)
                                            ▼
                                     CampusContextService
                                            │ builds a small, role-scoped snapshot:
                                            │ attendance, today's classes, pending work,
                                            │ marks, recent announcements
                                            ▼
                                       GroqAiService  ──▶  api.groq.com
                                            │            (system prompt + context + question)
                                            ◀──  answer text
                                            ▼
                                     { answer, configured, model }
```

Five properties of this design are worth stating plainly:

1. **The API key never leaves the backend.** The browser calls `/api/ai/chat`; only Java talks to Groq.
2. **The model never touches the database.** It receives a text snapshot the backend assembled and nothing else.
3. **The snapshot is built from the JWT**, not from any id the browser sends, so a student cannot ask about
   another student's marks.
4. **The context is small** — a few hundred tokens of the user's own summary rather than table dumps, which
   keeps the call fast and cheap.
5. **The system prompt forbids invention**: answer only from the context, and say so when the answer is not there.

Without `GROQ_API_KEY` the application still starts; the assistant page shows a banner and every question
returns *"AI assistant is not configured. Please add GROQ_API_KEY."*

**No Python anywhere.** The AI integration is a plain HTTP call from `RestTemplate` in Java.

---

## Security model

- **Passwords** are hashed with BCrypt. The `password` field never appears in any DTO.
- **JWT**, HS256, 24-hour expiry, carrying the email as subject and the role as a claim. Stateless: no
  server-side session table.
- **Every request** passes through `JwtAuthenticationFilter`, which validates the token and loads the user
  into the `SecurityContext`.
- **Authorisation** is enforced with `@PreAuthorize` on the controller methods, not by hiding links.
  A student who calls `GET /api/students` directly gets `403`, which is covered by a test.
- **Ownership checks** in the services: a faculty member can only close their own attendance sessions and
  grade their own assignments; a student can only read their own attendance and marks.
- **CORS** is restricted to the configured origins.
- **Validation** with Bean Validation on every request DTO, collected into field-level messages by
  `GlobalExceptionHandler`.

### Access control matrix

| Endpoint group | Student | Faculty | Admin |
|---|:---:|:---:|:---:|
| `POST /api/auth/**` | public | public | public |
| `GET /api/students` (directory) | — | read | full |
| `POST/PUT/DELETE /api/students` | — | — | full |
| `/api/faculty` | — | options only | full |
| `/api/departments`, `/api/subjects`, `/api/classrooms` | read | read | full |
| `/api/timetable` (own) | read | read | full |
| `POST /api/attendance/sessions/**` | — | own sessions | full |
| `/api/attendance/me/summary` | own | — | — |
| `/api/assignments` | read own class | own | read all |
| `POST /api/submissions` | own | — | — |
| `PUT /api/submissions/{id}/grade` | — | own assignments | full |
| `POST /api/academics` | — | full | full |
| `/api/announcements` | read | post | full |
| `/api/ai/chat` | own data | own data | own data |

---

## API reference

Base URL `http://localhost:8080/api`. Everything except login, register and `/departments/public`
needs `Authorization: Bearer <token>`.

<details>
<summary><strong>Full endpoint list</strong></summary>

**Auth** — `POST /auth/register`, `POST /auth/login`, `GET /auth/me`

**Dashboards** — `GET /dashboard/student`, `GET /dashboard/faculty`, `GET /dashboard/admin`

**Students** — `GET /students` (paged, `search`, `departmentId`, `semester`, `sort`), `GET /students/me`,
`GET /students/class`, `GET /students/{id}`, `POST /students`, `PUT /students/{id}`, `DELETE /students/{id}`

**Faculty** — `GET /faculty`, `GET /faculty/options`, `GET /faculty/me`, `GET /faculty/{id}`,
`POST /faculty`, `PUT /faculty/{id}`, `DELETE /faculty/{id}`

**Departments** — `GET /departments/public`, `GET /departments`, `GET /departments/{id}`,
`POST /departments`, `PUT /departments/{id}`, `DELETE /departments/{id}`

**Subjects** — `GET /subjects`, `GET /subjects/options`, `GET /subjects/mine?facultyId=`,
`GET /subjects/{id}`, `POST /subjects`, `PUT /subjects/{id}`, `DELETE /subjects/{id}`

**Classrooms** — `GET /classrooms`, `GET /classrooms/options`, `POST /classrooms`,
`PUT /classrooms/{id}`, `DELETE /classrooms/{id}`

**Timetable** — `GET /timetable/me`, `GET /timetable/today`, `GET /timetable`, `GET /timetable/class`,
`POST /timetable`, `PUT /timetable/{id}`, `DELETE /timetable/{id}`

**Attendance** — `GET /attendance/mode`, `POST /attendance/sessions`,
`POST /attendance/sessions/{id}/detect`, `POST /attendance/sessions/{id}/close`,
`GET /attendance/sessions/{id}`, `GET /attendance/sessions`, `GET /attendance/sessions/all`,
`GET /attendance/me/summary`, `GET /attendance/student/{id}/summary`, `GET /attendance/student/{id}`,
`GET /attendance/overview`

**Assignments** — `GET /assignments`, `GET /assignments/{id}`, `POST /assignments`,
`PUT /assignments/{id}`, `DELETE /assignments/{id}`

**Submissions** — `POST /submissions`, `GET /submissions/me`, `GET /submissions/assignment/{id}`,
`PUT /submissions/{id}/grade`

**Announcements** — `GET /announcements`, `POST /announcements`, `DELETE /announcements/{id}`

**Academics** — `GET /academics/me`, `GET /academics/student/{id}`, `POST /academics`, `DELETE /academics/{id}`

**Notifications** — `GET /notifications/me`, `GET /notifications/me/unread-count`, `PUT /notifications/{id}/read`

**AI** — `POST /ai/chat`, `GET /ai/status`

</details>

A ready-to-run Postman collection is in [`postman/Smart-Campus.postman_collection.json`](postman/).
Run *Auth → Login* first; it saves the token into a collection variable that every other request uses.

### Error shape

Every failure returns the same JSON:

```json
{
  "timestamp": "2026-03-01T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/assignments",
  "fieldErrors": { "title": "Title is required" }
}
```

---

## Project structure

```
smart-campus-platform/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/smartcampus/
│       │   ├── config/          SecurityConfig, RestClientConfig, DataSeeder
│       │   ├── controller/      15 REST controllers
│       │   ├── dto/             request and response records, grouped by module
│       │   ├── entity/          15 JPA entities and enums
│       │   ├── exception/       custom exceptions + GlobalExceptionHandler
│       │   ├── mapper/          entity → DTO conversion
│       │   ├── repository/      Spring Data repositories
│       │   ├── security/        JwtService, JwtAuthenticationFilter, CurrentUser
│       │   ├── service/         business logic + AttendanceService / AiService interfaces
│       │   │   └── impl/        DemoAttendanceService, BleAttendanceService, GroqAiService
│       │   └── util/            DateUtils, GradeCalculator
│       ├── main/resources/      application.yml
│       └── test/java/           unit and integration tests
├── frontend/
│   └── src/
│       ├── components/          Button, Input, DataTable, Modal, ChartCard, Sidebar, ...
│       ├── context/             AuthContext, ToastContext
│       ├── hooks/               useApiData, useDebouncedValue
│       ├── layouts/             DashboardLayout
│       ├── pages/               auth, student, faculty, admin, shared
│       ├── routes/              AppRoutes
│       ├── services/            axios instance + every endpoint
│       └── utils/               constants, formatting helpers
├── docs/                        architecture, diagrams, curriculum mapping, interview guide
├── postman/
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Running the tests

```bash
cd backend
mvn test
```

The suite runs against an in-memory H2 database, so no PostgreSQL instance is needed.

| Test | What it proves |
|---|---|
| `GradeCalculatorTest` | Percentage rounding, the divide-by-zero guard, grade bands |
| `AttendanceSummaryTest` | Attendance percentage and the "lectures you can miss" formula, with mocked repositories |
| `SmartCampusApiTest` | Login succeeds and fails correctly, protected endpoints reject anonymous calls, a student gets 403 on an admin endpoint, admin paging works, a student reads their own summary, faculty create an assignment, validation returns 400 |

---

## Design decisions and deviations

Things a reviewer is likely to ask about, and the reasoning:

**Roles are an enum column, not a `roles` table.** A user has exactly one role and the set never changes at
runtime, so a join table would add a query and a migration path for no benefit. `Role` is a Java enum stored
as a string and handed to Spring Security as a `GrantedAuthority`. If the college later needs multiple roles
per user or runtime-defined roles, this becomes a `roles` table and a `user_roles` join.

**Demo data is seeded in Java (`DataSeeder`), not `data.sql`.** Passwords must be BCrypt-hashed at runtime —
a SQL file would need a hard-coded hash — and inserting rows with explicit ids leaves the PostgreSQL identity
sequences behind, so the next insert from the application collides. A `CommandLineRunner` avoids both problems
and is skipped automatically when the database already has users.

**Service interfaces only where there is a real second implementation.** `AttendanceService` and `AiService`
are interfaces because demo/BLE and a swappable AI provider are genuine variation points. The CRUD services
are concrete classes: an interface with exactly one implementation is indirection without a purpose.

**Shared pages across roles.** The timetable, announcements, assistant and profile screens are identical for
every role, so they are written once and mounted under `/student/...`, `/faculty/...` and `/admin/...`. The
backend decides whose data comes back, from the JWT.

**Submissions are links, not uploads.** File storage (S3 or similar) is a deployment concern that adds no
academic value here. Students paste a Drive or GitHub link; the grading flow is otherwise complete.

**One monolith.** At the scale of a single college, microservices would add network calls, deployment
complexity and distributed-transaction problems to solve none of the actual requirements.

**Lombok** is used for getters, setters and builders on entities. If your IDE shows errors, install the
Lombok plugin and enable annotation processing.

---

## Limitations and future work

Known gaps, stated honestly:

- **BLE scanning client.** The backend accepts real BLE detections; the device-side scanner is not in this
  repository. Demo mode covers the flow in the meantime.
- **No file uploads.** Assignments and submissions use links.
- **No email or push notifications.** Notifications are in-app only.
- **No refresh tokens.** A JWT lasts 24 hours and then the user signs in again.
- **Faculty and admin cannot edit their own profile** from the UI; the admin edits user records.
- **Reports are basic** — department attendance and counts, exported as CSV from the browser.

Natural next steps: the Android or Web Bluetooth scanner, file uploads, email notifications, refresh tokens,
a parent portal, and attendance trend prediction.

---

## Team

Project group **PR2026-27_01**, Department of Information Technology,
Trinity College of Engineering and Research, Pune.

- Aary Ghadage
- Pratik Pawar
- Omkar Dhere

**Guide:** Dr. Vilas S. Gaikwad

---

Built for academic use. Change the demo password and set a real `JWT_SECRET` before deploying anywhere.
