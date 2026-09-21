package com.smartcampus.dto.attendance;

/**
 * One detection event sent by the faculty client.
 * DemoAttendanceService uses studentId; BleAttendanceService uses bleDeviceId + rssi.
 */
public record DetectionRequest(
        Long studentId,
        String bleDeviceId,
        Integer rssi
) {
}
