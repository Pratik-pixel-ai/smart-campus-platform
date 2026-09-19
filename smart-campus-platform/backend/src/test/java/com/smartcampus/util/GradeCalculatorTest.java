package com.smartcampus.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Percentage and grade rules used by attendance and academic records. */
class GradeCalculatorTest {

    @Test
    void percentageIsRoundedToTwoDecimals() {
        assertEquals(66.67, GradeCalculator.percentage(2, 3));
        assertEquals(75.0, GradeCalculator.percentage(15, 20));
        assertEquals(100.0, GradeCalculator.percentage(10, 10));
    }

    @Test
    void percentageIsZeroWhenNoLecturesHaveBeenHeld() {
        // Guards against a divide-by-zero for a student whose class has not started yet.
        assertEquals(0.0, GradeCalculator.percentage(0, 0));
    }

    @Test
    void gradeBandsMapToTheExpectedLetters() {
        assertEquals("O", GradeCalculator.gradeFor(95));
        assertEquals("A+", GradeCalculator.gradeFor(80));
        assertEquals("B", GradeCalculator.gradeFor(55));
        assertEquals("F", GradeCalculator.gradeFor(31));
    }
}
