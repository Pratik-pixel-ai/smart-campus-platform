package com.smartcampus.util;

/**
 * Grade bands for academic records. Kept in one place so the same rule is used by
 * every caller and can be changed without touching the services.
 */
public final class GradeCalculator {

    private GradeCalculator() {
    }

    public static String gradeFor(int totalMarks) {
        if (totalMarks >= 90) return "O";
        if (totalMarks >= 80) return "A+";
        if (totalMarks >= 70) return "A";
        if (totalMarks >= 60) return "B+";
        if (totalMarks >= 50) return "B";
        if (totalMarks >= 40) return "C";
        return "F";
    }

    /** Rounds a percentage to two decimal places. */
    public static double percentage(long part, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((part * 10000.0) / total) / 100.0;
    }
}
