package com.smartcampus.config;

import com.smartcampus.entity.*;
import com.smartcampus.repository.*;
import com.smartcampus.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Loads demo data the first time the application starts against an empty database.
 *
 * This is written in Java rather than data.sql on purpose: passwords have to be
 * BCrypt-hashed at runtime, and letting Hibernate generate the ids keeps the Postgres
 * identity sequences in step (a hand-written SQL insert with explicit ids does not).
 *
 * Disable with SEED_ENABLED=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    /** Demo only. Every seeded account shares this password; change it before any real use. */
    private static final String DEMO_PASSWORD = "Demo@1234";

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    private final TimetableRepository timetableRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AnnouncementRepository announcementRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Registration is impossible without at least one department (the form's dropdown is
        // loaded from /api/departments/public). The "users exist" check below would otherwise
        // skip department creation on a database that already has accounts but no departments.
        if (departmentRepository.count() == 0) {
            log.info("No departments found - creating the default departments.");
            findOrCreateDepartment("Information Technology", "IT", "Dr. Vilas S. Gaikwad");
            findOrCreateDepartment("Computer Engineering", "CS", "Dr. Anita Rao");
        }

        if (userRepository.count() > 0) {
            log.info("Demo data already present, skipping seeding.");
            return;
        }
        log.info("Seeding demo data...");

        Department it = findOrCreateDepartment("Information Technology", "IT", "Dr. Vilas S. Gaikwad");
        Department cs = findOrCreateDepartment("Computer Engineering", "CS", "Dr. Anita Rao");

        User adminUser = createUser("Campus Administrator", "admin@smartcampus.com", Role.ROLE_ADMIN);

        Faculty gaikwad = createFaculty("Dr. Vilas Gaikwad", "faculty@smartcampus.com", "FAC-001", it, "Professor");
        Faculty kulkarni = createFaculty("Prof. Sneha Kulkarni", "sneha.kulkarni@smartcampus.com", "FAC-002", it,
                "Assistant Professor");
        Faculty deshmukh = createFaculty("Prof. Rahul Deshmukh", "rahul.deshmukh@smartcampus.com", "FAC-003", cs,
                "Associate Professor");

        // Subjects: four for IT semester 7, two for CS semester 5.
        Subject dbms = createSubject("Database Management Systems", "IT701", it, gaikwad, 7, 4);
        Subject ml = createSubject("Machine Learning", "IT702", it, kulkarni, 7, 4);
        Subject iot = createSubject("Internet of Things", "IT703", it, gaikwad, 7, 3);
        Subject testing = createSubject("Software Testing", "IT704", it, kulkarni, 7, 3);
        Subject os = createSubject("Operating Systems", "CS501", cs, deshmukh, 5, 4);
        Subject networks = createSubject("Computer Networks", "CS502", cs, deshmukh, 5, 4);

        Classroom a101 = createClassroom("A-101", "Main Building", 70);
        Classroom a102 = createClassroom("A-102", "Main Building", 70);
        Classroom lab1 = createClassroom("LAB-1", "IT Block", 40);

        // Students: eight in IT semester 7 division A, four in CS semester 5 division A.
        String[] itNames = {"Aary Ghadage", "Pratik Pawar", "Omkar Dhere", "Sanya Kulkarni",
                "Rohit Jadhav", "Neha Patil", "Karan Shetty", "Isha Deshpande"};
        List<Student> itStudents = new ArrayList<>();
        for (int i = 0; i < itNames.length; i++) {
            itStudents.add(createStudent(itNames[i],
                    i == 0 ? "student@smartcampus.com" : emailFor(itNames[i]),
                    "IT21" + String.format("%03d", i + 1), it, 7, "A", "BLE-IT-" + (i + 1)));
        }

        String[] csNames = {"Vikram Singh", "Priya Menon", "Arjun Nair", "Meera Joshi"};
        List<Student> csStudents = new ArrayList<>();
        for (int i = 0; i < csNames.length; i++) {
            csStudents.add(createStudent(csNames[i], emailFor(csNames[i]),
                    "CS22" + String.format("%03d", i + 1), cs, 5, "A", "BLE-CS-" + (i + 1)));
        }

        seedTimetable(it, cs, dbms, ml, iot, testing, os, networks, a101, a102, lab1);
        seedAttendance(List.of(dbms, ml, iot, testing), itStudents, a101);
        seedAttendance(List.of(os, networks), csStudents, a102);
        seedAssignments(dbms, ml, os, itStudents, csStudents);
        seedAnnouncements(adminUser, gaikwad.getUser(), it);
        seedAcademics(itStudents, List.of(dbms, ml, iot, testing), 7);
        seedAcademics(csStudents, List.of(os, networks), 5);

        log.info("Demo data ready. Sign in with admin@smartcampus.com / {}", DEMO_PASSWORD);
    }

    // ---------------------------------------------------------------- helpers

    private Department findOrCreateDepartment(String name, String code, String hodName) {
        return departmentRepository.findByCode(code).orElseGet(() -> departmentRepository.save(
                Department.builder().name(name).code(code).hodName(hodName).build()));
    }

    private User createUser(String fullName, String email, Role role) {
        return userRepository.save(User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .role(role)
                .active(true)
                .build());
    }

    private String emailFor(String fullName) {
        return fullName.toLowerCase().replace(" ", ".") + "@smartcampus.com";
    }

    private Faculty createFaculty(String name, String email, String code, Department department, String designation) {
        return facultyRepository.save(Faculty.builder()
                .user(createUser(name, email, Role.ROLE_FACULTY))
                .employeeCode(code)
                .department(department)
                .designation(designation)
                .phone("+91 90000 0" + code.substring(code.length() - 4))
                .build());
    }

    private Student createStudent(String name, String email, String roll, Department department,
                                  int semester, String division, String bleDeviceId) {
        return studentRepository.save(Student.builder()
                .user(createUser(name, email, Role.ROLE_STUDENT))
                .rollNumber(roll)
                .department(department)
                .semester(semester)
                .division(division)
                .phone("+91 98765 " + roll.substring(roll.length() - 5))
                .bleDeviceId(bleDeviceId)
                .build());
    }

    private Subject createSubject(String name, String code, Department department, Faculty faculty,
                                  int semester, int credits) {
        return subjectRepository.save(Subject.builder()
                .name(name).code(code).department(department).faculty(faculty)
                .semester(semester).credits(credits).build());
    }

    private Classroom createClassroom(String room, String building, int capacity) {
        return classroomRepository.save(Classroom.builder()
                .roomNumber(room).building(building).capacity(capacity).build());
    }

    private void seedTimetable(Department it, Department cs, Subject dbms, Subject ml, Subject iot,
                               Subject testing, Subject os, Subject networks,
                               Classroom a101, Classroom a102, Classroom lab1) {
        addSlot(dbms, a101, DayOfWeek.MONDAY, "09:00", "10:00", 7, "A");
        addSlot(ml, a101, DayOfWeek.MONDAY, "10:15", "11:15", 7, "A");
        addSlot(iot, lab1, DayOfWeek.TUESDAY, "09:00", "11:00", 7, "A");
        addSlot(testing, a101, DayOfWeek.TUESDAY, "11:15", "12:15", 7, "A");
        addSlot(dbms, a101, DayOfWeek.WEDNESDAY, "09:00", "10:00", 7, "A");
        addSlot(ml, lab1, DayOfWeek.WEDNESDAY, "14:00", "16:00", 7, "A");
        addSlot(iot, a101, DayOfWeek.THURSDAY, "09:00", "10:00", 7, "A");
        addSlot(testing, a101, DayOfWeek.THURSDAY, "10:15", "11:15", 7, "A");
        addSlot(dbms, a101, DayOfWeek.FRIDAY, "11:15", "12:15", 7, "A");
        addSlot(ml, a101, DayOfWeek.FRIDAY, "09:00", "10:00", 7, "A");
        addSlot(iot, lab1, DayOfWeek.SATURDAY, "09:00", "11:00", 7, "A");

        addSlot(os, a102, DayOfWeek.MONDAY, "09:00", "10:00", 5, "A");
        addSlot(networks, a102, DayOfWeek.TUESDAY, "10:15", "11:15", 5, "A");
        addSlot(os, a102, DayOfWeek.THURSDAY, "11:15", "12:15", 5, "A");
        addSlot(networks, a102, DayOfWeek.FRIDAY, "09:00", "10:00", 5, "A");
    }

    private void addSlot(Subject subject, Classroom classroom, DayOfWeek day,
                         String start, String end, int semester, String division) {
        timetableRepository.save(TimetableEntry.builder()
                .subject(subject)
                .faculty(subject.getFaculty())
                .classroom(classroom)
                .dayOfWeek(day)
                .startTime(LocalTime.parse(start))
                .endTime(LocalTime.parse(end))
                .semester(semester)
                .division(division)
                .build());
    }

    /** Six closed sessions per subject with a realistic mix of present and absent records. */
    private void seedAttendance(List<Subject> subjects, List<Student> students, Classroom classroom) {
        Random random = new Random(42);   // fixed seed: the demo looks the same every time

        for (Subject subject : subjects) {
            for (int lecture = 1; lecture <= 6; lecture++) {
                LocalDate date = LocalDate.now().minusDays(7L * lecture);
                AttendanceSession session = sessionRepository.save(AttendanceSession.builder()
                        .subject(subject)
                        .faculty(subject.getFaculty())
                        .classroom(classroom)
                        .sessionDate(date)
                        .lectureNumber(lecture)
                        .durationMinutes(60)
                        .semester(subject.getSemester())
                        .division("A")
                        .status(SessionStatus.CLOSED)
                        .mode(DetectionMethod.DEMO)
                        .startedAt(date.atTime(9, 0))
                        .closedAt(date.atTime(10, 0))
                        .build());

                for (Student student : students) {
                    boolean present = random.nextInt(100) < 82;   // roughly 82% attendance
                    recordRepository.save(AttendanceRecord.builder()
                            .session(session)
                            .student(student)
                            .status(present ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT)
                            .detectionMethod(present ? DetectionMethod.DEMO : DetectionMethod.SYSTEM)
                            .signalStrength(present ? -55 - random.nextInt(25) : null)
                            .markedAt(date.atTime(9, 5))
                            .build());
                }
            }
        }
    }

    private void seedAssignments(Subject dbms, Subject ml, Subject os,
                                 List<Student> itStudents, List<Student> csStudents) {
        Assignment normalisation = assignmentRepository.save(Assignment.builder()
                .title("Normalisation case study")
                .description("Normalise the given college database schema up to BCNF and justify each step.")
                .subject(dbms).faculty(dbms.getFaculty())
                .deadline(LocalDateTime.now().plusDays(6).withHour(23).withMinute(59).withSecond(0).withNano(0))
                .maxMarks(20).attachmentUrl("https://example.edu/assignments/dbms-normalisation.pdf")
                .semester(7).division("A").build());

        Assignment indexing = assignmentRepository.save(Assignment.builder()
                .title("Query optimisation report")
                .description("Compare execution plans for three queries with and without indexes.")
                .subject(dbms).faculty(dbms.getFaculty())
                .deadline(LocalDateTime.now().minusDays(3).withHour(23).withMinute(59).withSecond(0).withNano(0))
                .maxMarks(20).semester(7).division("A").build());

        Assignment regression = assignmentRepository.save(Assignment.builder()
                .title("Linear regression from scratch")
                .description("Implement gradient descent for a single-variable model and plot the loss curve.")
                .subject(ml).faculty(ml.getFaculty())
                .deadline(LocalDateTime.now().plusDays(12).withHour(23).withMinute(59).withSecond(0).withNano(0))
                .maxMarks(25).semester(7).division("A").build());

        Assignment scheduling = assignmentRepository.save(Assignment.builder()
                .title("CPU scheduling simulation")
                .description("Simulate FCFS, SJF and round robin and compare average waiting time.")
                .subject(os).faculty(os.getFaculty())
                .deadline(LocalDateTime.now().plusDays(9).withHour(23).withMinute(59).withSecond(0).withNano(0))
                .maxMarks(20).semester(5).division("A").build());

        // Past assignment: most students submitted, two of them already graded.
        for (int i = 0; i < itStudents.size(); i++) {
            if (i % 4 == 3) {
                continue;   // one student in four did not submit
            }
            Student student = itStudents.get(i);
            boolean graded = i < 2;
            submissionRepository.save(AssignmentSubmission.builder()
                    .assignment(indexing)
                    .student(student)
                    .submissionUrl("https://example.edu/submissions/" + student.getRollNumber() + "-indexing.pdf")
                    .remarks("Submitted through the campus portal")
                    .submittedAt(LocalDateTime.now().minusDays(4))
                    .status(graded ? SubmissionStatus.GRADED : SubmissionStatus.SUBMITTED)
                    .marksObtained(graded ? 16 + i : null)
                    .feedback(graded ? "Clear comparison of the execution plans. Add the index size trade-off." : null)
                    .gradedAt(graded ? LocalDateTime.now().minusDays(1) : null)
                    .build());
        }

        submissionRepository.save(AssignmentSubmission.builder()
                .assignment(normalisation)
                .student(itStudents.get(1))
                .submissionUrl("https://example.edu/submissions/early-normalisation.pdf")
                .submittedAt(LocalDateTime.now().minusDays(1))
                .status(SubmissionStatus.SUBMITTED)
                .build());

        submissionRepository.save(AssignmentSubmission.builder()
                .assignment(scheduling)
                .student(csStudents.get(0))
                .submissionUrl("https://example.edu/submissions/cpu-scheduling.zip")
                .submittedAt(LocalDateTime.now().minusHours(20))
                .status(SubmissionStatus.SUBMITTED)
                .build());

        // regression has no submissions yet, so the empty state is visible in the demo
        assignmentRepository.save(regression);
    }

    private void seedAnnouncements(User admin, User facultyUser, Department it) {
        announcementRepository.save(Announcement.builder()
                .title("Mid-semester examination timetable")
                .message("The mid-semester examination begins on the 12th. The detailed schedule is on the notice board.")
                .department(null).priority(Priority.HIGH).createdBy(admin).build());

        announcementRepository.save(Announcement.builder()
                .title("IoT lab rescheduled")
                .message("Saturday's IoT lab moves to LAB-1 at 09:00. Bring your BLE kits.")
                .department(it).priority(Priority.NORMAL).createdBy(facultyUser).build());

        announcementRepository.save(Announcement.builder()
                .title("Library timings extended")
                .message("The central library will stay open until 22:00 during the examination period.")
                .department(null).priority(Priority.LOW).createdBy(admin).build());
    }

    private void seedAcademics(List<Student> students, List<Subject> subjects, int semester) {
        Random random = new Random(7);
        for (Student student : students) {
            for (Subject subject : subjects) {
                int internal = 24 + random.nextInt(16);      // out of 40
                int external = 34 + random.nextInt(26);      // out of 60
                int total = internal + external;
                academicRecordRepository.save(AcademicRecord.builder()
                        .student(student)
                        .subject(subject)
                        .semester(semester)
                        .internalMarks(internal)
                        .externalMarks(external)
                        .totalMarks(total)
                        .grade(GradeCalculator.gradeFor(total))
                        .build());
            }
        }
    }
}
