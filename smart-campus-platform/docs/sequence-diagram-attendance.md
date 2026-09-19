# Sequence: BLE attendance

## Full lifecycle

```mermaid
sequenceDiagram
    actor F as Faculty
    participant UI as Attendance screen
    participant C as AttendanceController
    participant A as AttendanceService<br/>(Demo or BLE)
    participant SR as SessionRepository
    participant RR as RecordRepository
    participant ST as StudentRepository
    participant DB as PostgreSQL

    Note over UI: GET /api/attendance/mode<br/>drives the "Demo BLE detection" banner

    F->>UI: start session (subject, room, lecture, division)
    UI->>C: POST /api/attendance/sessions
    C->>C: @PreAuthorize ROLE_FACULTY or ROLE_ADMIN
    C->>A: createSession(request)
    A->>A: currentUser.faculty() from the JWT
    A->>SR: save(status = OPEN, mode = DEMO|BLE)
    SR->>DB: INSERT
    A-->>C: session (0 present)
    C-->>UI: 201

    UI->>C: GET /api/students/class?departmentId&semester&division
    C-->>UI: class list

    loop for each detection
        Note over UI: demo: faculty taps "Simulate detection"<br/>ble: scanner posts { bleDeviceId, rssi }
        UI->>C: POST /api/attendance/sessions/{id}/detect
        C->>A: detect(sessionId, request)
        A->>A: findOwnedSession — is this my session?
        alt session already closed
            A-->>C: BadRequestException
            C-->>UI: 400 "This session is closed"
        else open
            A->>A: resolveStudent(...)
            Note right of A: Demo → studentId<br/>BLE → lookup by bleDeviceId,<br/>reject if rssi < -85
            A->>ST: find student
            A->>A: ensureEnrolled(session, student)
            A->>RR: existsBySessionIdAndStudentId?
            alt already marked
                A-->>C: unchanged session (no duplicate, no error)
            else first detection
                A->>RR: save(PRESENT, method, rssi)
                RR->>DB: INSERT
            end
            A-->>C: session with updated counts
            C-->>UI: 200 → the counter moves
        end
    end

    F->>UI: close session
    UI->>C: POST /api/attendance/sessions/{id}/close
    C->>A: closeSession(sessionId)
    A->>RR: findBySessionId → map of present student ids
    A->>ST: findByDepartmentIdAndSemesterAndDivision → class list
    A->>A: class list minus present = absentees
    A->>RR: saveAll(ABSENT, method = SYSTEM)
    A->>SR: status = CLOSED, closed_at = now
    A-->>C: final counts
    C-->>UI: 200 "12 present, 3 absent"
```

## Why closing is a separate step

Attendance is only complete when the lecture ends. Writing `ABSENT` rows up front and deleting them as
students arrive would mean far more writes and a window where the data is wrong. Closing once, at the end,
gives a single consistent picture and an accurate `closed_at` timestamp.

The absentee calculation uses a map of the students already present, so it is one pass over the class list
rather than a query per student.

## How a student sees the result

```mermaid
sequenceDiagram
    actor S as Student
    participant UI as Attendance page
    participant C as AttendanceController
    participant Q as AttendanceQueryService
    participant RR as RecordRepository
    participant DB as PostgreSQL

    S->>UI: opens "My attendance"
    UI->>C: GET /api/attendance/me/summary
    C->>C: @PreAuthorize ROLE_STUDENT
    C->>Q: mySummary()
    Q->>Q: student resolved from the JWT, not from a URL id
    Q->>RR: findSubjectWiseAttendance(studentId)
    RR->>DB: SELECT subject, COUNT(*), SUM(CASE WHEN PRESENT ...) GROUP BY subject
    DB-->>RR: one row per subject
    Q->>Q: percentage = present / total
    Q->>Q: lecturesCanMiss = floor(present / 0.75 − total)
    Q-->>C: AttendanceSummaryResponse
    C-->>UI: 200
    UI->>S: chart, progress bars, "you can miss 4 more lectures"
```

Because the student is taken from the token, `GET /api/attendance/student/99/summary` for someone else's id
returns `403` — the check is in `AttendanceQueryService.ensureCanView`, not in the browser.
