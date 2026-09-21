# Interview and viva guide

Thirty-five questions you are likely to be asked about this project, with answers that stay close to the
code. Read the answer, then open the file it names — being able to point at the line matters more than
reciting the paragraph.

---

## Project and design

### 1. Explain your project in two minutes.

A college runs on paper attendance registers, spreadsheets, WhatsApp groups and notice boards. Attendance
reaches a student weeks late, usually after they have already fallen below the 75% requirement, and nobody
has one view of attendance, timetable, assignments and results together.

The Smart Campus Platform puts all of that in one web application with three role-based dashboards. Faculty
capture attendance in the classroom and percentages update immediately. Students see subject-wise attendance
and exactly how many more lectures they can afford to miss. Assignments, marks and announcements live in the
same place, and an AI assistant answers plain questions from the signed-in user's own records.

It is a Spring Boot backend with PostgreSQL, a React frontend, JWT authentication, and attendance capture
written behind an interface so simulated detection and real BLE detection are interchangeable.

### 2. Why a monolith and not microservices?

Scale decides this. One college is thousands of users, not millions, and every module shares the same data:
attendance needs students, subjects and timetable in the same transaction. Splitting that across services
would turn a local join into network calls and a local transaction into a distributed one, which is a hard
problem to solve for no benefit here.

The code is still modular — controller, service, repository, clear package boundaries — so if attendance ever
needed to scale independently, it could be extracted. Starting with a monolith and extracting later is the
easier direction to travel.

### 3. Why Spring Boot, React and PostgreSQL?

**Spring Boot** gives security, validation, transactions and data access as configured defaults, and
Spring Security is a well-audited implementation of authentication — not something worth writing myself.

**React** suits an application with many stateful screens, and the component model keeps the table, modal
and chart written once and reused across 26 pages.

**PostgreSQL** because the data is deeply relational (students → departments → subjects → sessions → records)
and correctness depends on constraints. The unique constraint on `(session_id, student_id)` is what actually
prevents duplicate attendance. A document database would push that guarantee into application code.

### 4. Walk through the layers of your backend.

`Controller → Service → Repository → Entity`, with DTOs crossing the controller boundary.

- **Controller** maps HTTP, validates with `@Valid`, checks the role with `@PreAuthorize`, returns a DTO.
- **Service** holds business rules, transactions and ownership checks.
- **Repository** is Spring Data JPA; derived query methods and a few JPQL queries.
- **Entity** is the JPA model and never leaves the service layer.

The rule that a controller never touches a repository is what stops business logic scattering into HTTP
handlers. The rule that entities never leave the service layer is what keeps the BCrypt password hash out
of responses — `User` has a `password` field, `StudentResponse` does not.

### 5. Why DTOs? Why not just return the entity?

Three reasons. **Security:** returning `User` would serialise the password hash. **Coupling:** the API shape
would change every time the database changes. **Shape:** `AttendanceSummaryResponse` carries computed values
like `lecturesCanMiss` that are not columns anywhere.

Response DTOs are Java records — immutable, no boilerplate. Request DTOs are records too, with Bean
Validation annotations on the components.

### 6. What design patterns did you use, and why those?

Only where they earn their place.

- **Strategy** for attendance: `AttendanceService` with `DemoAttendanceService` and `BleAttendanceService`.
  There are genuinely two ways to detect a student, so the variation is real.
- **Template method** in `AbstractAttendanceService`: the lifecycle is fixed, only `resolveStudent` varies.
- **Repository**, provided by Spring Data.
- **Dependency injection** through constructors everywhere, which is also what makes the services testable.

I deliberately did not add a factory or a builder where a constructor was enough. A pattern used without a
reason is just indirection.

---

## Security

### 7. How does JWT authentication work here?

On login, `AuthService` hands the credentials to Spring Security's `AuthenticationManager`, which loads the
user and compares the BCrypt hash. If it matches, `JwtService` signs a token with HS256 containing the email
as subject, the role as a claim, and a 24-hour expiry.

The browser stores the token and sends it as `Authorization: Bearer <token>`. On every request
`JwtAuthenticationFilter` — a `OncePerRequestFilter` — validates the signature and expiry, loads the user,
and places the authentication into the `SecurityContext` for the rest of the request.

### 8. Why JWT rather than sessions?

Sessions need server-side state, which means sticky sessions or a shared store once there is more than one
instance. A JWT is self-contained: any instance can verify it with the signing key. It also suits a separate
frontend origin, where cookies bring CORS and CSRF complications.

The honest trade-off: a JWT cannot be revoked before it expires. I limit the blast radius with a 24-hour
expiry, and `CustomUserDetailsService` reloads the user on every request, so a deactivated account stops
working immediately even though its token is still cryptographically valid.

### 9. Where is the JWT secret, and what happens if it is missing?

In the `JWT_SECRET` environment variable, read through `app.jwt.secret`. There is no hard-coded default
anywhere in the source.

If it is empty, `JwtService.init()` generates a random key for that run and logs a warning. Tokens then stop
working after a restart, which is a loud, harmless reminder to configure it. If a secret shorter than 32
characters is supplied, the application refuses to start, because HS256 needs at least 256 bits.

### 10. Why BCrypt, and why not SHA-256?

SHA-256 is designed to be fast, which is exactly wrong for passwords — a GPU can try billions per second.
BCrypt is deliberately slow and has a cost factor that can be raised as hardware improves. It also salts
every hash automatically, so two users with the same password get different hashes and one rainbow table
cannot attack the whole database.

### 11. How is role-based access enforced?

Three layers, and only two of them matter.

1. The sidebar only shows links for the user's role — a convenience, not security.
2. `ProtectedRoute` redirects the wrong role away from a URL — also only convenience.
3. **`@PreAuthorize` on the controller method** — this is the actual boundary. `GET /api/students` carries
   `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_FACULTY')")`, so a student calling it with a valid
   token gets `403` regardless of what the browser does.

There is a test for exactly that: `studentCannotReachAdminOnlyEndpoints`.

### 12. A student edits `localStorage` and sets their role to admin. What happens?

Nothing. The role in the browser only decides which links are drawn. Every request is authorised from the
role stored in the database, loaded by `CustomUserDetailsService` from the email inside the signed token.
Changing local storage cannot change the token, and forging a token requires the signing key.

They might briefly see an admin-looking sidebar; every API call behind it returns `403`.

### 13. How do you stop one student reading another student's marks?

Identity comes from the token, never from the URL. `AttendanceQueryService.mySummary()` calls
`currentUser.student()`, which resolves the student from the `SecurityContext`. There is no id to tamper with.

Where an id is unavoidable — `GET /api/attendance/student/{id}/summary`, which faculty need —
`ensureCanView` checks that a caller with `ROLE_STUDENT` is asking about themselves and throws
`UnauthorizedException` (403) otherwise.

The same pattern guards assignments: a faculty member can only grade submissions for assignments they created.

### 14. What is CORS and how did you configure it?

The browser blocks JavaScript on one origin from reading responses from another. The frontend is on `:5173`
and the API on `:8080`, which are different origins, so the API must explicitly allow it.

`SecurityConfig.corsConfigurationSource()` allows only the origins listed in `CORS_ALLOWED_ORIGINS`, only the
methods actually used, and only the `Authorization` and `Content-Type` headers. It is a whitelist, not `*` —
with credentials-bearing requests, `*` is both unsafe and invalid.

### 15. Why is CSRF disabled?

CSRF exploits credentials the browser attaches automatically — cookies. This API is stateless and
authenticates from an `Authorization` header that JavaScript must set deliberately. A cross-site form post
cannot add that header, so there is nothing to forge. Had I used cookie-based sessions, CSRF protection
would be mandatory.

### 16. How do you validate input?

Bean Validation annotations on the request records — `@NotBlank`, `@Email`, `@Min`, `@Max`, `@Size` — with
messages written for humans. `@Valid` on the controller parameter triggers it.

Failures become `MethodArgumentNotValidException`, which `GlobalExceptionHandler` converts into a 400 with a
`fieldErrors` map. The frontend reads the first entry out of that map and shows it, so a validation message
never reaches the user as a stack trace.

Business rules that annotations cannot express — marks above the maximum, submitting to another class's
assignment — are checked in the service and throw `BadRequestException`.

---

## Database and JPA

### 17. Explain your database schema.

Fifteen tables. `users` holds login identity and role; `students` and `faculty` hold the academic profile
for those two roles, one-to-one with `users`. `departments`, `subjects` and `classrooms` are master data.
`timetable` is the weekly schedule.

Attendance is two tables: `attendance_sessions` (one lecture) and `attendance_records` (one row per student
per session). Assignments are `assignments` and `assignment_submissions`. Results are `academic_records`.
Then `announcements` and `notifications`.

The full ER diagram is in `docs/database-er.md`.

### 18. Is the schema normalised? Where did you denormalise, and why?

It is in 3NF. Every non-key column depends on the whole key, and repeating groups are separate tables rather
than columns.

One deliberate exception: `total_marks` and `grade` are stored on `academic_records` even though both are
derived from the internal and external marks. A published mark sheet must keep the grade that applied when it
was issued, even if the college later changes the grade bands. Both are computed in `AcademicService` using
`GradeCalculator`, never accepted from the client, so they cannot disagree with the marks.

### 19. Why is the role an enum column instead of a `roles` table?

A user has exactly one role here and roles are not defined at runtime, so a join table would add a query on
every authentication and a migration path for no gain. `Role` is a Java enum persisted as a string with
`@Enumerated(EnumType.STRING)` — not ordinal, because reordering the enum would silently corrupt existing rows.

I know the normalised answer is a `roles` table with a join table, and I would move to it the moment a user
needed more than one role or the college wanted roles configurable without a deployment. This is documented
as a deviation in the README rather than hidden.

### 20. What is the N+1 problem and where did you avoid it?

N+1 is when fetching a list issues one query for the list and then one more per row — 50 students becoming
51 queries.

The clearest place I avoided it is subject-wise attendance. The naive version loops over subjects and counts
records per subject. Instead, `findSubjectWiseAttendance` is a single grouped JPQL query returning a
projection interface with the totals already aggregated by the database.

The other place is the student assignment list: I load that student's submissions once and build a `Map` by
assignment id, then look each one up, instead of querying inside the loop.

### 21. Why `FetchType.LAZY` on the relationships?

With `EAGER`, loading one `AttendanceRecord` would pull the session, subject, department, faculty and user —
most of the database for a row I may only need the status from. `LAZY` fetches an association only when it is
actually accessed.

The cost is that accessing a lazy association outside a transaction throws `LazyInitializationException`. I
set `open-in-view: false` deliberately so that failure surfaces during development rather than silently
issuing queries while the response is being serialised. Mapping to DTOs happens inside the `@Transactional`
service method, which is where the associations are still available.

### 22. How does `@Transactional` work and where did you use it?

It wraps the method in a database transaction through a proxy: commit on normal return, roll back on an
unchecked exception.

Every service method has it — `readOnly = true` for queries, which lets the driver and Hibernate skip dirty
checking. It matters most in `closeSession`: absentee records are saved and the session status is updated in
one transaction, so a failure halfway cannot leave a closed session with half its absentees missing.

A caveat I would mention: it works through proxies, so calling a `@Transactional` method from another method
in the same class bypasses it.

### 23. How do you prevent duplicate attendance?

Two layers. The service checks `existsBySessionIdAndStudentId` before inserting and quietly returns the
unchanged session if a record exists — a repeated BLE detection is a no-op, not an error.

The real guarantee is the database: `uk_attendance_session_student` on `(session_id, student_id)`. If two
detections raced past the check, the second insert would fail at the constraint. Application checks are
convenience; the constraint is correctness.

### 24. How is pagination implemented?

Spring Data `Pageable`. The controller takes `page`, `size` and `sort` as query parameters via
`@PageableDefault`, passes them to a repository method returning `Page<T>`, and the database does the
`LIMIT`/`OFFSET` and `ORDER BY`.

The response is wrapped in my own `PageResponse` record — content, page, size, totalElements, totalPages,
last — because Spring's `Page` serialises a lot of internal structure that would then be part of my API
contract.

Search and sort are server-side too, so the browser never downloads 500 rows to display 10.

---

## Attendance and BLE

### 25. Explain the BLE attendance design. Is it real?

The design has two implementations behind one interface, and I want to be precise about which one runs by
default.

`AttendanceService` declares `createSession`, `detect`, `closeSession`, `getSession` and `mode`.
`AbstractAttendanceService` implements everything shared — the session lifecycle, the enrolment check,
duplicate prevention, persistence. Only `resolveStudent` is abstract.

`DemoAttendanceService` (default) takes a `studentId` from the faculty screen; no Bluetooth is involved.
`BleAttendanceService` takes a `bleDeviceId` and `rssi` from a real scan, maps the device to a student
through an indexed unique column, and rejects signals below −85 dBm so a phone in the corridor is not counted.

Spring picks one at start-up with `@ConditionalOnProperty` on `app.attendance.mode`.

**So: the default mode is simulated.** The UI says so in an amber banner, and every record stores
`detection_method` — `DEMO`, `BLE`, `MANUAL` or `SYSTEM` — so simulated attendance is never presented as a
hardware-verified scan. The backend for real BLE is complete; what is missing is the device-side scanner
that collects advertisements, which is outside this repository.

### 26. What would it take to make BLE real?

Three steps, no backend changes. Register each student's device identifier (the field and the admin screen
already exist). Set `ATTENDANCE_MODE=ble` and restart. Then have the faculty device scan and POST each
advertisement to the same `/detect` endpoint as `{ "bleDeviceId": "...", "rssi": -63 }`.

The client can be a Web Bluetooth page or a small Android scanner. The value of the interface is precisely
that swapping it changes one class and no controller, schema or UI code.

### 27. Why BLE rather than QR codes, GPS or face recognition?

BLE is passive: the student's phone advertises and the faculty device scans, so nobody queues to scan
anything. A QR code on a screen can be photographed and sent to a friend. GPS is accurate to several metres
and cannot tell the difference between this classroom and the corridor — BLE signal strength can, within a
few metres. Face recognition is accurate but heavy on hardware and raises consent and privacy issues that a
college would need a policy for.

BLE's weakness, which I would state up front, is proxy attendance: a student can hand their phone to a
friend. Mitigations would be a rotating advertised identifier bound to a session, or a periodic re-scan
during the lecture.

### 28. How is the attendance percentage calculated?

Present lectures divided by total lectures recorded, per subject and overall, computed by the database
aggregation query and rounded to two decimals in `GradeCalculator.percentage`. A student with no records
returns 0.0 rather than dividing by zero — there is a test for that.

It is always computed on the server. Nothing the browser sends can change what a student's attendance says.

### 29. Explain the "lectures you can still miss" calculation.

From the requirement that attendance stays at or above 75% after missing `x` more lectures:

```
present / (total + x) ≥ 0.75
present ≥ 0.75 × (total + x)
x ≤ present / 0.75 − total
```

So `x = floor(present / 0.75 − total)`, clamped at zero. With 18 present out of 20: `18 / 0.75 − 20 = 4`.
Four more lectures may be missed. This is the number students actually want, and showing it is most of the
practical value of the attendance module.

### 30. Why close the session as a separate step?

Attendance is only complete when the lecture ends. The alternative — writing everyone as absent at the start
and deleting rows as they arrive — means far more writes and a window where the stored data is wrong.

Closing once compares the class list against the students already marked present and inserts `ABSENT` for
the rest, with method `SYSTEM` so the audit trail shows the system wrote them. The comparison uses a `Map`
of present student ids, so it is one pass over the class list rather than a query per student.

---

## AI integration

### 31. How does the AI assistant work, end to end?

The browser posts only a question to `/api/ai/chat`. `AiController` identifies the user from the JWT and
calls `GroqAiService`, which asks `CampusContextService` to build a snapshot of that user's own data —
attendance summary, today's classes, pending assignments, marks, recent announcements. That snapshot plus a
system prompt plus the question go to the Groq chat completions API over HTTPS from Java. The answer text
comes back and is returned to the browser.

It is a plain `RestTemplate` call. There is no Python anywhere in the project.

### 32. Why call the AI from Java instead of from the browser?

The API key. A browser call would put the key in the network tab of every user's machine, and anyone could
copy it and spend the quota. Keeping the call in the backend means the key lives only in the server
environment.

It also means the backend controls what data reaches the model. If the browser built the prompt, a user
could craft one asking about somebody else's marks.

### 33. Could a user make the AI leak another student's data?

No, because the model never has that data. It receives a text snapshot the backend assembled, and that
snapshot is built from `CurrentUser` — the identity inside the token — not from anything in the request body.
There is no tool use and no database access from the model.

The worst outcome of a prompt injection is a wrong or silly sentence, not a data leak. The system prompt also
tells the model to answer only from the provided context and to say plainly when the information is not there,
and temperature is 0.2 to keep it close to the data.

### 34. What happens if the AI provider is down or the key is missing?

Both are handled and neither breaks the platform. Missing key: `GroqAiService` returns
*"AI assistant is not configured. Please add GROQ_API_KEY."* with `configured: false`, and the assistant page
shows a banner. Network failure or timeout: the `RestClientException` is caught and the user sees
*"The assistant could not be reached right now."*

Neither returns a 500, and the rest of the application is unaffected — the assistant is an addition, not a
dependency. A 20-second timeout on the `RestTemplate` stops a slow provider from holding request threads.

---

## Testing, deployment and reflection

### 35. What did you test, and what would you test next?

Three test classes covering the things most likely to break.

`GradeCalculatorTest` — percentage rounding, the divide-by-zero guard, grade bands. Pure unit tests.

`AttendanceSummaryTest` — the attendance percentage and the "lectures you can miss" formula, with mocked
repositories so it tests the calculation and nothing else.

`SmartCampusApiTest` — an integration test with MockMvc and H2 running the real filter chain: login
succeeds, a wrong password returns 401, a request with no token returns 401, a student gets 403 on an admin
endpoint, admin paging works, a student reads their own summary, faculty create an assignment, and
validation returns 400 for an empty title.

Next I would add concurrency tests around duplicate detection, a test that closing a session writes exactly
the right absentees for a larger class, and frontend component tests for the attendance screen.

---

## Bonus questions worth rehearsing

**What was the hardest part?** Getting the attendance abstraction right. My first version had a single
service with an `if (mode == BLE)` branch through the middle of `detect`, which meant every change risked
breaking the other mode. Pulling the shared lifecycle into an abstract class and leaving only `resolveStudent`
to vary made both implementations obvious and made the demo/real distinction something the code states rather
than something a comment claims.

**What would you do differently?** Write the tests earlier — I added them after the services existed, and a
couple of the services would have had smaller methods if a test had been pushing back on them. I would also
add refresh tokens rather than a 24-hour expiry with no renewal.

**How does this scale to 5,000 students?** The queries that matter are indexed and aggregated in the
database, and pagination keeps response sizes bounded. Before scaling out I would add connection pool tuning,
caching for master data that rarely changes (departments, subjects), and a read replica for the reporting
queries. The JWT design already allows several stateless instances behind a load balancer.

**Why is `open-in-view` set to false?** Because the default, `true`, keeps the persistence context open while
the response is serialised, which means lazy associations quietly fire queries at rendering time — slow and
invisible. Turning it off forces the data loading to happen in the service layer where I can see it.

**Show me something you are proud of.** `AttendanceQueryService.lecturesCanMiss` — three lines derived from a
one-line inequality, tested, and the single number students care about most. And the honesty of the
`detection_method` column: the system records how each attendance row was created rather than flattening
simulated and real detection into one indistinguishable value.
