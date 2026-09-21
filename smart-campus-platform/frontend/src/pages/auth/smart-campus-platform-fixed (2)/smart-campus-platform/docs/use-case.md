# Use case diagram

```mermaid
graph LR
    Student(["Student"])
    Faculty(["Faculty"])
    Admin(["Administrator"])

    subgraph SmartCampus["Smart Campus Platform"]
        UC1["Register / sign in"]
        UC2["View attendance percentage"]
        UC3["View timetable"]
        UC4["Submit assignment"]
        UC5["View marks and grades"]
        UC6["Read announcements"]
        UC7["Ask the campus assistant"]

        UC8["Start attendance session"]
        UC9["Detect students (BLE / demo)"]
        UC10["Close session and mark absentees"]
        UC11["Create assignment"]
        UC12["Grade submissions"]
        UC13["Enter marks"]
        UC14["Post announcement"]
        UC15["View student directory"]

        UC16["Manage students and faculty"]
        UC17["Manage departments, subjects, classrooms"]
        UC18["Manage timetable"]
        UC19["View campus reports"]
    end

    Student --> UC1
    Student --> UC2
    Student --> UC3
    Student --> UC4
    Student --> UC5
    Student --> UC6
    Student --> UC7

    Faculty --> UC1
    Faculty --> UC3
    Faculty --> UC6
    Faculty --> UC7
    Faculty --> UC8
    Faculty --> UC9
    Faculty --> UC10
    Faculty --> UC11
    Faculty --> UC12
    Faculty --> UC13
    Faculty --> UC14
    Faculty --> UC15

    Admin --> UC1
    Admin --> UC6
    Admin --> UC7
    Admin --> UC14
    Admin --> UC15
    Admin --> UC16
    Admin --> UC17
    Admin --> UC18
    Admin --> UC19

    UC9 -.->|includes| UC8
    UC10 -.->|includes| UC8
    UC12 -.->|includes| UC11
```

## Primary use case: take attendance

| | |
|---|---|
| **Actor** | Faculty |
| **Goal** | Record who attended a lecture |
| **Precondition** | Signed in as faculty; the subject is assigned to them |
| **Trigger** | The lecture begins |

**Main flow**

1. Faculty opens *Take attendance* and selects subject, classroom, lecture number, duration and division.
2. The system creates a session with status `OPEN` and shows the class list.
3. For each student detected (BLE advertisement, or a simulated tap in demo mode) the system records a
   `PRESENT` row with the detection method.
4. Faculty closes the session.
5. The system writes `ABSENT` for every student on the list who was never detected, sets the status to
   `CLOSED`, and updates the counts.

**Alternate flows**

- *3a.* The same student is detected twice → the unique constraint on `(session, student)` means the second
  detection changes nothing and no error is shown.
- *3b.* A detected student is not in this class → the request is rejected with a clear message.
- *3c.* In BLE mode the signal is weaker than the threshold → the detection is rejected as out of range.
- *4a.* Another faculty member tries to close the session → `403`.
