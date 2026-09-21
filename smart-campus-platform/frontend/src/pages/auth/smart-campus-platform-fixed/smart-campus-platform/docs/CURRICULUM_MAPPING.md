# Curriculum mapping

Where each BE (Information Technology) subject shows up in this project, with the file to open when an
examiner asks. Useful for the project report and for the viva.

---

## Database Management Systems

| Concept | Where |
|---|---|
| ER modelling | `docs/database-er.md` — 15 entities with relationships and cardinality |
| Normalisation to 3NF | Schema design; the one deliberate exception (`total_marks`, `grade`) is justified in the ER doc |
| Primary and foreign keys | Every `@Id` and `@JoinColumn` in `entity/` |
| Composite unique constraints | `AttendanceRecord` `(session_id, student_id)`, `AssignmentSubmission` `(assignment_id, student_id)`, `AcademicRecord` `(student_id, subject_id, semester)` |
| Aggregate queries with `GROUP BY` | `AttendanceRecordRepository.findSubjectWiseAttendance` |
| Joins | `StudentRepository.search` joins students and users for name search |
| Transactions and ACID | `@Transactional` on every service; `closeSession` is the clearest example of atomicity |
| Indexing | Unique constraints index email, roll number and `ble_device_id` — the columns on the hot paths |
| Projections | `SubjectAttendanceProjection` — reading aggregated columns without loading entities |

**Talking point:** the unique constraint on `(session_id, student_id)` is not decoration — it is what makes
duplicate attendance impossible even under a race, and the application-level check is only there to turn a
constraint violation into a friendly no-op.

---

## Object Oriented Programming and Java

| Concept | Where |
|---|---|
| Abstraction | `AttendanceService`, `AiService` — interfaces describing what, not how |
| Inheritance | `AbstractAttendanceService` → `DemoAttendanceService`, `BleAttendanceService` |
| Polymorphism | `AttendanceController` injects `AttendanceService` and gets whichever implementation is active |
| Encapsulation | Private fields with accessors; `BaseEntity` hides the auditing columns |
| Interfaces vs abstract classes | Interface for the contract, abstract class for shared lifecycle code |
| Enums | `Role`, `AttendanceStatus`, `DetectionMethod`, `SessionStatus`, `Priority`, `SubmissionStatus` |
| Records | Every DTO — immutable data carriers |
| Generics | `PageResponse<T>`, `JpaRepository<T, ID>` |
| Streams and lambdas | `AcademicService.buildSummary` groups and maps; `AttendanceQueryService` sums with `mapToLong` |
| Optional | `findById().orElseThrow(...)` throughout — no null returns from repositories |
| Exception hierarchy | Four custom runtime exceptions handled centrally |
| Static utility classes | `GradeCalculator`, `DateUtils` — private constructors, no state |

---

## Software Engineering

| Concept | Where |
|---|---|
| Layered architecture | `docs/architecture.md` |
| Separation of concerns | Controller / service / repository responsibilities, stated as rules |
| Design patterns | Strategy, template method, repository, dependency injection — each with a stated reason |
| SOLID | Single responsibility per service; open/closed via the attendance strategy; dependency inversion through constructor injection |
| Use case modelling | `docs/use-case.md` with a full main and alternate flow |
| Sequence diagrams | `docs/sequence-diagram-login.md`, `docs/sequence-diagram-attendance.md` |
| Class diagram | `docs/class-diagram.md` |
| Version control | Git, with `.gitignore` excluding build output and `.env` |
| Configuration management | Externalised config, `.env.example`, no secrets in source |
| Testing | Unit tests with mocks, integration tests through the real filter chain |
| Documentation | README, nine documents in `docs/`, a Postman collection |

---

## Web Technology

| Concept | Where |
|---|---|
| REST principles | Nouns as resources, correct verbs, correct status codes (200/201/400/401/403/404/409) |
| Stateless HTTP | JWT instead of server-side sessions |
| Request and response headers | `Authorization: Bearer`, `Content-Type`, CORS headers |
| CORS | `SecurityConfig.corsConfigurationSource` — origin whitelist |
| JSON serialisation | Jackson, with `non_null` inclusion configured |
| SPA routing | React Router with a nginx fallback to `index.html` |
| Client-side state | React Context for auth and toasts, `useState` locally |
| Responsive design | Tailwind breakpoints; the sidebar collapses under `lg` |
| Accessibility | Labels bound to inputs, `aria-label` on icon buttons, visible focus rings, Escape closes modals |

---

## Computer Networks

| Concept | Where |
|---|---|
| Client–server model | React client, Spring Boot server, PostgreSQL |
| Application layer protocols | HTTP/HTTPS for the API, JDBC over TCP to the database |
| BLE and RSSI | `BleAttendanceService` — signal strength as a proximity proxy, with a threshold |
| Ports and services | 5173 dev frontend, 8080 API, 5432 database, 3000 production frontend |
| Same-origin policy | Why CORS configuration is needed at all |
| Timeouts | 20-second connect and read timeout on the Groq client |

**Talking point:** RSSI is a crude distance estimate — it falls off with distance but is affected by
obstacles and orientation. That is exactly why the threshold is configurable rather than hard-coded, and why
proxy attendance remains a stated limitation.

---

## Operating Systems and concurrency

| Concept | Where |
|---|---|
| Concurrency | Multiple faculty taking attendance simultaneously; correctness protected by database constraints, not by locks in application code |
| Thread pools | The servlet container's request threads; a timeout on the outbound AI call so one slow provider cannot exhaust them |
| Containers and isolation | `docker-compose.yml` — three isolated services on one network |
| Process lifecycle | `CommandLineRunner` running once at start-up for seeding |

---

## Data Structures and Algorithms

Used where the problem called for it, not bolted on:

| Structure | Where and why |
|---|---|
| `HashMap` | `closeSession` builds a map of present student ids so the absentee calculation is one pass instead of a query per student |
| `HashSet` | The attendance UI holds present ids in a set for O(1) "is this student already marked?" |
| `TreeMap` | `AcademicService` groups records by semester in sorted order, so the UI needs no sorting |
| Hash index | `ble_device_id` unique column — a BLE detection is an indexed lookup, not a scan |
| Aggregation | Counting present and total in the database rather than iterating in Java |

**Complexity:** closing a session with `n` students and `m` already present is O(n + m) — the map build plus
one pass over the class list. The naive version is O(n) database round trips.

---

## Artificial Intelligence and Machine Learning

| Concept | Where |
|---|---|
| Large language model integration | `GroqAiService` — chat completions over an OpenAI-compatible API |
| Prompt engineering | System prompt constraining the model to the supplied context |
| Context construction (RAG-style) | `CampusContextService` retrieves the user's own records and puts them in the prompt |
| Temperature and token limits | 0.2 and 500 output tokens, to keep answers close to the data and bounded |
| Safety and scoping | Context built from the JWT identity; no database access from the model |
| Graceful degradation | Missing key or provider failure handled without a 500 |

**Talking point:** this is retrieval-augmented generation in its simplest honest form. The retrieval step is
a set of scoped service calls rather than a vector search, because the corpus is one user's structured
records, not a document collection. A vector database here would be complexity for its own sake.

---

## Project management

| Aspect | Detail |
|---|---|
| Problem identification | Manual attendance and scattered campus information |
| Requirement analysis | Three roles, nine modules, an access control matrix |
| Technology selection | Justified in the README and in this guide |
| Implementation | ~140 Java classes, ~60 React files, layered and documented |
| Testing | Unit and integration tests, runnable with `mvn test` |
| Deployment | Docker Compose, one command |
| Documentation | README, nine docs, Postman collection |
| Honest limitations | Stated in the README rather than hidden |

---

## Quick reference for the viva

| If asked about | Open |
|---|---|
| Database design | `docs/database-er.md` |
| System architecture | `docs/architecture.md` |
| BLE attendance | `service/AttendanceService.java` and `service/impl/` |
| Security | `config/SecurityConfig.java`, `security/JwtService.java` |
| AI integration | `docs/ai-flow.md`, `service/impl/GroqAiService.java` |
| Business logic | `service/AttendanceQueryService.java` |
| Testing | `src/test/java/com/smartcampus/` |
| Likely questions | `docs/INTERVIEW_GUIDE.md` |
