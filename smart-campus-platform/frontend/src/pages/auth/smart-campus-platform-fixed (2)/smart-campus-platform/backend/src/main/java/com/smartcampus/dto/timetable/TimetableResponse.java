package com.smartcampus.dto.timetable;

public record TimetableResponse(
        Long id,
        String dayOfWeek,
        String startTime,
        String endTime,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long facultyId,
        String facultyName,
        Long classroomId,
        String room,
        Integer semester,
        String division,
        String departmentName
) {
}
