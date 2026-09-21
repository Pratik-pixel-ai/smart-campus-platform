# Class diagram

## Domain model

```mermaid
classDiagram
    class User {
        +Long id
        +String fullName
        +String email
        +String password
        +Role role
        +boolean active
    }

    class Student {
        +Long id
        +String rollNumber
        +Integer semester
        +String division
        +String phone
        +String bleDeviceId
    }

    class Faculty {
        +Long id
        +String employeeCode
        +String designation
        +String phone
    }

    class Department {
        +Long id
        +String name
        +String code
        +String hodName
    }

    class Subject {
        +Long id
        +String name
        +String code
        +Integer semester
        +Integer credits
    }

    class Classroom {
        +Long id
        +String roomNumber
        +String building
        +Integer capacity
    }

    class TimetableEntry {
        +Long id
        +DayOfWeek dayOfWeek
        +LocalTime startTime
        +LocalTime endTime
        +Integer semester
        +String division
    }

    class AttendanceSession {
        +Long id
        +LocalDate sessionDate
        +Integer lectureNumber
        +Integer durationMinutes
        +SessionStatus status
        +DetectionMethod mode
    }

    class AttendanceRecord {
        +Long id
        +AttendanceStatus status
        +DetectionMethod detectionMethod
        +Integer signalStrength
        +LocalDateTime markedAt
    }

    class Assignment {
        +Long id
        +String title
        +String description
        +LocalDateTime deadline
        +Integer maxMarks
    }

    class AssignmentSubmission {
        +Long id
        +String submissionUrl
        +SubmissionStatus status
        +Integer marksObtained
        +String feedback
    }

    class AcademicRecord {
        +Long id
        +Integer semester
        +Integer internalMarks
        +Integer externalMarks
        +Integer totalMarks
        +String grade
    }

    class Announcement {
        +Long id
        +String title
        +String message
        +Priority priority
    }

    class Notification {
        +Long id
        +String title
        +String message
        +boolean read
    }

    User "1" -- "0..1" Student
    User "1" -- "0..1" Faculty
    User "1" -- "*" Notification
    User "1" -- "*" Announcement : posts
    Department "1" -- "*" Student
    Department "1" -- "*" Faculty
    Department "1" -- "*" Subject
    Faculty "1" -- "*" Subject : teaches
    Subject "1" -- "*" TimetableEntry
    Classroom "1" -- "*" TimetableEntry
    Subject "1" -- "*" AttendanceSession
    Faculty "1" -- "*" AttendanceSession
    AttendanceSession "1" -- "*" AttendanceRecord
    Student "1" -- "*" AttendanceRecord
    Subject "1" -- "*" Assignment
    Assignment "1" -- "*" AssignmentSubmission
    Student "1" -- "*" AssignmentSubmission
    Student "1" -- "*" AcademicRecord
    Subject "1" -- "*" AcademicRecord
```

## Attendance strategy

```mermaid
classDiagram
    class AttendanceService {
        <<interface>>
        +createSession(request) AttendanceSessionResponse
        +detect(sessionId, request) AttendanceSessionResponse
        +closeSession(sessionId) AttendanceSessionResponse
        +getSession(sessionId) AttendanceSessionResponse
        +mode() DetectionMethod
    }

    class AbstractAttendanceService {
        <<abstract>>
        #resolveStudent(session, request)* Student
        #enrolledStudents(session) List~Student~
        #ensureEnrolled(session, student)
        #findOwnedSession(sessionId) AttendanceSession
    }

    class DemoAttendanceService {
        +resolveStudent() Student
        +mode() DEMO
    }

    class BleAttendanceService {
        -int minRssi
        +resolveStudent() Student
        +mode() BLE
    }

    AttendanceService <|.. AbstractAttendanceService
    AbstractAttendanceService <|-- DemoAttendanceService
    AbstractAttendanceService <|-- BleAttendanceService
```

`DemoAttendanceService` is active when `app.attendance.mode=demo` (the default);
`BleAttendanceService` when it is `ble`. Exactly one bean exists at runtime, so
`AttendanceController` simply injects `AttendanceService`.

## Service layer

```mermaid
classDiagram
    class AiService {
        <<interface>>
        +ask(request) AiChatResponse
        +status() AiStatusResponse
    }
    class GroqAiService
    class CampusContextService {
        +buildContext() String
    }
    AiService <|.. GroqAiService
    GroqAiService --> CampusContextService

    class DashboardService
    DashboardService --> AttendanceQueryService
    DashboardService --> TimetableService
    DashboardService --> AssignmentService
    DashboardService --> AnnouncementService
    DashboardService --> AcademicService
```
