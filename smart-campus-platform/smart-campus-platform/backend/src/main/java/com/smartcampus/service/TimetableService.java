package com.smartcampus.service;

import com.smartcampus.dto.timetable.TimetableRequest;
import com.smartcampus.dto.timetable.TimetableResponse;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.mapper.TimetableMapper;
import com.smartcampus.repository.*;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Weekly lecture schedule. Students see their class timetable, faculty see the
 * lectures they teach, and admins manage the entries.
 */
@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final CurrentUser currentUser;

    /** The signed-in user's week, resolved from their role rather than from a client-supplied id. */
    @Transactional(readOnly = true)
    public List<TimetableResponse> myWeek() {
        User user = currentUser.user();
        if (user.getRole() == Role.ROLE_STUDENT) {
            Student student = currentUser.student();
            return map(timetableRepository
                    .findBySubjectDepartmentIdAndSemesterAndDivisionOrderByDayOfWeekAscStartTimeAsc(
                            student.getDepartment().getId(), student.getSemester(), student.getDivision()));
        }
        if (user.getRole() == Role.ROLE_FACULTY) {
            return map(timetableRepository.findByFacultyIdOrderByDayOfWeekAscStartTimeAsc(
                    currentUser.faculty().getId()));
        }
        return map(timetableRepository.findAllByOrderByDayOfWeekAscStartTimeAsc());
    }

    @Transactional(readOnly = true)
    public List<TimetableResponse> myToday() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        User user = currentUser.user();
        if (user.getRole() == Role.ROLE_STUDENT) {
            Student student = currentUser.student();
            return map(timetableRepository
                    .findBySubjectDepartmentIdAndSemesterAndDivisionAndDayOfWeekOrderByStartTimeAsc(
                            student.getDepartment().getId(), student.getSemester(), student.getDivision(), today));
        }
        if (user.getRole() == Role.ROLE_FACULTY) {
            return map(timetableRepository.findByFacultyIdAndDayOfWeekOrderByStartTimeAsc(
                    currentUser.faculty().getId(), today));
        }
        return myWeek().stream().filter(entry -> entry.dayOfWeek().equals(today.name())).toList();
    }

    @Transactional(readOnly = true)
    public List<TimetableResponse> listAll() {
        return map(timetableRepository.findAllByOrderByDayOfWeekAscStartTimeAsc());
    }

    @Transactional(readOnly = true)
    public List<TimetableResponse> forClass(Long departmentId, Integer semester, String division) {
        return map(timetableRepository
                .findBySubjectDepartmentIdAndSemesterAndDivisionOrderByDayOfWeekAscStartTimeAsc(
                        departmentId, semester, division.toUpperCase()));
    }

    @Transactional
    public TimetableResponse create(TimetableRequest request) {
        return TimetableMapper.toResponse(timetableRepository.save(apply(new TimetableEntry(), request)));
    }

    @Transactional
    public TimetableResponse update(Long id, TimetableRequest request) {
        TimetableEntry entry = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable entry", id));
        return TimetableMapper.toResponse(timetableRepository.save(apply(entry, request)));
    }

    @Transactional
    public void delete(Long id) {
        TimetableEntry entry = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable entry", id));
        timetableRepository.delete(entry);
    }

    private TimetableEntry apply(TimetableEntry entry, TimetableRequest request) {
        LocalTime start = DateUtils.parseTime(request.startTime());
        LocalTime end = DateUtils.parseTime(request.endTime());
        if (!end.isAfter(start)) {
            throw new BadRequestException("End time must be after start time");
        }

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", request.facultyId()));

        entry.setSubject(subject);
        entry.setFaculty(faculty);
        entry.setClassroom(request.classroomId() == null ? null : classroomRepository.findById(request.classroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", request.classroomId())));
        entry.setDayOfWeek(parseDay(request.dayOfWeek()));
        entry.setStartTime(start);
        entry.setEndTime(end);
        entry.setSemester(request.semester());
        entry.setDivision(request.division().toUpperCase());
        return entry;
    }

    private DayOfWeek parseDay(String value) {
        try {
            return DayOfWeek.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid day: use MONDAY to SATURDAY");
        }
    }

    private List<TimetableResponse> map(List<TimetableEntry> entries) {
        return entries.stream().map(TimetableMapper::toResponse).toList();
    }
}
