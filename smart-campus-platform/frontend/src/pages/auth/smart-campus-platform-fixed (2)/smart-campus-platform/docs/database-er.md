# Database design

PostgreSQL. The schema is generated from the JPA entities (`ddl-auto: update`), so the entity classes are
the single source of truth.

## ER diagram

```mermaid
erDiagram
    USERS ||--o| STUDENTS : "profile"
    USERS ||--o| FACULTY : "profile"
    USERS ||--o{ NOTIFICATIONS : receives
    USERS ||--o{ ANNOUNCEMENTS : posts

    DEPARTMENTS ||--o{ STUDENTS : has
    DEPARTMENTS ||--o{ FACULTY : has
    DEPARTMENTS ||--o{ SUBJECTS : offers
    DEPARTMENTS ||--o{ ANNOUNCEMENTS : targets

    FACULTY ||--o{ SUBJECTS : teaches
    FACULTY ||--o{ TIMETABLE : conducts
    FACULTY ||--o{ ATTENDANCE_SESSIONS : starts
    FACULTY ||--o{ ASSIGNMENTS : sets

    SUBJECTS ||--o{ TIMETABLE : scheduled_in
    SUBJECTS ||--o{ ATTENDANCE_SESSIONS : for
    SUBJECTS ||--o{ ASSIGNMENTS : for
    SUBJECTS ||--o{ ACADEMIC_RECORDS : graded_in

    CLASSROOMS ||--o{ TIMETABLE : hosts
    CLASSROOMS ||--o{ ATTENDANCE_SESSIONS : hosts

    ATTENDANCE_SESSIONS ||--o{ ATTENDANCE_RECORDS : contains
    STUDENTS ||--o{ ATTENDANCE_RECORDS : appears_in
    STUDENTS ||--o{ ASSIGNMENT_SUBMISSIONS : submits
    STUDENTS ||--o{ ACADEMIC_RECORDS : earns
    ASSIGNMENTS ||--o{ ASSIGNMENT_SUBMISSIONS : receives

    USERS {
        bigint id PK
        varchar full_name
        varchar email UK
        varchar password "BCrypt hash"
        varchar role "ROLE_STUDENT | ROLE_FACULTY | ROLE_ADMIN"
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    STUDENTS {
        bigint id PK
        bigint user_id FK,UK
        varchar roll_number UK
        bigint department_id FK
        int semester
        varchar division
        varchar phone
        varchar ble_device_id UK "advertised BLE identifier"
    }

    FACULTY {
        bigint id PK
        bigint user_id FK,UK
        varchar employee_code UK
        bigint department_id FK
        varchar designation
        varchar phone
    }

    DEPARTMENTS {
        bigint id PK
        varchar name UK
        varchar code UK
        varchar hod_name
    }

    SUBJECTS {
        bigint id PK
        varchar name
        varchar code UK
        bigint department_id FK
        bigint faculty_id FK "nullable until assigned"
        int semester
        int credits
    }

    CLASSROOMS {
        bigint id PK
        varchar room_number UK
        varchar building
        int capacity
    }

    TIMETABLE {
        bigint id PK
        bigint subject_id FK
        bigint faculty_id FK
        bigint classroom_id FK
        varchar day_of_week
        time start_time
        time end_time
        int semester
        varchar division
    }

    ATTENDANCE_SESSIONS {
        bigint id PK
        bigint subject_id FK
        bigint faculty_id FK
        bigint classroom_id FK
        date session_date
        int lecture_number
        int duration_minutes
        int semester
        varchar division
        varchar status "OPEN | CLOSED"
        varchar mode "DEMO | BLE"
        timestamp started_at
        timestamp closed_at
    }

    ATTENDANCE_RECORDS {
        bigint id PK
        bigint session_id FK
        bigint student_id FK
        varchar status "PRESENT | ABSENT"
        varchar detection_method "DEMO | BLE | MANUAL | SYSTEM"
        int signal_strength
        timestamp marked_at
    }

    ASSIGNMENTS {
        bigint id PK
        varchar title
        varchar description
        bigint subject_id FK
        bigint faculty_id FK
        timestamp deadline
        int max_marks
        varchar attachment_url
        int semester
        varchar division
    }

    ASSIGNMENT_SUBMISSIONS {
        bigint id PK
        bigint assignment_id FK
        bigint student_id FK
        varchar submission_url
        varchar remarks
        timestamp submitted_at
        varchar status "SUBMITTED | LATE | GRADED"
        int marks_obtained
        varchar feedback
        timestamp graded_at
    }

    ACADEMIC_RECORDS {
        bigint id PK
        bigint student_id FK
        bigint subject_id FK
        int semester
        int internal_marks
        int external_marks
        int total_marks
        varchar grade
    }

    ANNOUNCEMENTS {
        bigint id PK
        varchar title
        varchar message
        bigint department_id FK "null = campus-wide"
        bigint subject_id FK
        varchar priority "LOW | NORMAL | HIGH"
        bigint created_by FK
    }

    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        varchar title
        varchar message
        boolean is_read
    }
```

## Constraints that carry business rules

| Constraint | Table | What it prevents |
|---|---|---|
| `uk_attendance_session_student` on `(session_id, student_id)` | `attendance_records` | Marking one student twice in one lecture, even if two detections race |
| `uk_submission_assignment_student` on `(assignment_id, student_id)` | `assignment_submissions` | Duplicate submissions; a resubmission updates the existing row |
| `uk_academic_student_subject_sem` on `(student_id, subject_id, semester)` | `academic_records` | Two conflicting mark sheets for the same subject |
| `uk_users_email` | `users` | Two accounts on one email |
| `uk_students_roll_number` | `students` | Two students on one roll number |
| `ble_device_id` unique | `students` | One device mapping to two students |

The database enforces these, not only the service layer, so a bug in application code cannot corrupt the data.

## Normalisation

The schema is in **3NF**:

- Every non-key column depends on the whole primary key.
- Repeating groups are separate tables (`attendance_records`, not columns on the session).
- Derived values are the deliberate exception: `total_marks` and `grade` are stored on `academic_records`
  because a mark sheet must reflect the rule that applied when it was published, even if the grade bands
  change later. They are always computed in `AcademicService`, never supplied by the client.

## Indexing

Unique constraints create indexes automatically, which covers the lookups that matter: email on login,
roll number on search, and `ble_device_id` on every BLE detection. Foreign keys are indexed by PostgreSQL
convention for the joins used by the dashboards.
