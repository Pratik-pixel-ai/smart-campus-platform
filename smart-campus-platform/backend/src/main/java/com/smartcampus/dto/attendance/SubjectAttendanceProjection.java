package com.smartcampus.dto.attendance;

/**
 * Read-only projection filled directly by the aggregation query in
 * AttendanceRecordRepository - no entity loading needed.
 */
public interface SubjectAttendanceProjection {
    Long getSubjectId();
    String getSubjectName();
    String getSubjectCode();
    Long getTotalLectures();
    Long getPresentLectures();
}
