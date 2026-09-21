package com.smartcampus.repository;

import com.smartcampus.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface TimetableRepository extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findBySubjectDepartmentIdAndSemesterAndDivisionOrderByDayOfWeekAscStartTimeAsc(
            Long departmentId, Integer semester, String division);

    List<TimetableEntry> findByFacultyIdOrderByDayOfWeekAscStartTimeAsc(Long facultyId);

    List<TimetableEntry> findBySubjectDepartmentIdAndSemesterAndDivisionAndDayOfWeekOrderByStartTimeAsc(
            Long departmentId, Integer semester, String division, DayOfWeek dayOfWeek);

    List<TimetableEntry> findByFacultyIdAndDayOfWeekOrderByStartTimeAsc(Long facultyId, DayOfWeek dayOfWeek);

    List<TimetableEntry> findAllByOrderByDayOfWeekAscStartTimeAsc();
}
