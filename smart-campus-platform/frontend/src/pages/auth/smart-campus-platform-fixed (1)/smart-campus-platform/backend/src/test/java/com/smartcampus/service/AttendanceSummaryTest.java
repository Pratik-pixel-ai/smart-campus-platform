package com.smartcampus.service;

import com.smartcampus.dto.attendance.AttendanceSummaryResponse;
import com.smartcampus.dto.attendance.SubjectAttendanceProjection;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.repository.AttendanceRecordRepository;
import com.smartcampus.repository.AttendanceSessionRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * The attendance percentage is business logic, so it is tested without a database:
 * the repository is mocked and only the calculation is exercised.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceSummaryTest {

    @Mock
    private AttendanceRecordRepository recordRepository;
    @Mock
    private AttendanceSessionRepository sessionRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private AttendanceQueryService attendanceQueryService;

    private Student student;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).fullName("Aary Ghadage").email("student@smartcampus.com").build();
        student = Student.builder().id(1L).user(user).rollNumber("IT21001")
                .department(Department.builder().id(1L).name("Information Technology").build())
                .semester(7).division("A").build();
    }

    @Test
    void overallPercentageIsPresentLecturesOverTotalLectures() {
        when(currentUser.student()).thenReturn(student);
        when(recordRepository.findSubjectWiseAttendance(1L)).thenReturn(List.of(
                projection(1L, "Database Management Systems", "IT701", 10, 8),
                projection(2L, "Machine Learning", "IT702", 10, 7)
        ));

        AttendanceSummaryResponse summary = attendanceQueryService.mySummary();

        assertEquals(20, summary.totalLectures());
        assertEquals(15, summary.presentLectures());
        assertEquals(5, summary.absentLectures());
        assertEquals(75.0, summary.overallPercentage());
        assertEquals(80.0, summary.subjects().get(0).percentage());
    }

    @Test
    void lecturesThatCanStillBeMissedRespectsTheSeventyFivePercentRule() {
        when(currentUser.student()).thenReturn(student);
        // 18 of 20 attended = 90%. 18/0.75 = 24, so four more lectures may be missed.
        when(recordRepository.findSubjectWiseAttendance(1L)).thenReturn(List.of(
                projection(1L, "Database Management Systems", "IT701", 20, 18)));

        AttendanceSummaryResponse summary = attendanceQueryService.mySummary();

        assertEquals(90.0, summary.overallPercentage());
        assertEquals(4, summary.lecturesCanMiss());
    }

    @Test
    void studentWithNoRecordsGetsZeroInsteadOfAnError() {
        when(currentUser.student()).thenReturn(student);
        when(recordRepository.findSubjectWiseAttendance(1L)).thenReturn(List.of());

        AttendanceSummaryResponse summary = attendanceQueryService.mySummary();

        assertEquals(0, summary.totalLectures());
        assertEquals(0.0, summary.overallPercentage());
        assertEquals(0, summary.lecturesCanMiss());
    }

    private SubjectAttendanceProjection projection(Long id, String name, String code, long total, long present) {
        return new SubjectAttendanceProjection() {
            @Override
            public Long getSubjectId() {
                return id;
            }

            @Override
            public String getSubjectName() {
                return name;
            }

            @Override
            public String getSubjectCode() {
                return code;
            }

            @Override
            public Long getTotalLectures() {
                return total;
            }

            @Override
            public Long getPresentLectures() {
                return present;
            }
        };
    }
}
