package com.smartcampus.mapper;

import com.smartcampus.dto.master.ClassroomResponse;
import com.smartcampus.dto.master.DepartmentResponse;
import com.smartcampus.dto.master.SubjectResponse;
import com.smartcampus.entity.Classroom;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Subject;

public final class MasterDataMapper {

    private MasterDataMapper() {
    }

    public static DepartmentResponse toResponse(Department department, long studentCount, long facultyCount) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getHodName(),
                studentCount,
                facultyCount
        );
    }

    public static SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getCode(),
                subject.getDepartment().getId(),
                subject.getDepartment().getName(),
                subject.getFaculty() == null ? null : subject.getFaculty().getId(),
                subject.getFaculty() == null ? null : subject.getFaculty().getUser().getFullName(),
                subject.getSemester(),
                subject.getCredits()
        );
    }

    public static ClassroomResponse toResponse(Classroom classroom) {
        return new ClassroomResponse(
                classroom.getId(),
                classroom.getRoomNumber(),
                classroom.getBuilding(),
                classroom.getCapacity()
        );
    }
}
