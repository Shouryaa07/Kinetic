package com.kinetic.service;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.TreeSet;
import org.junit.Test;

public class StreakTest {
    private static TreeSet<LocalDate> days(LocalDate... d) {
        return new TreeSet<>(java.util.List.of(d));
    }

    private final LocalDate today = LocalDate.of(2026, 10, 9);

    @Test
    public void streakCountsBackFromToday() {
        assertEquals(3, StatsService.currentStreak(days(today, today.minusDays(1), today.minusDays(2), today.minusDays(5)), today));
    }

    @Test
    public void streakSurvivesUntilTheDayEnds() {
        assertEquals(2, StatsService.currentStreak(days(today.minusDays(1), today.minusDays(2)), today));
    }

    @Test
    public void streakBreaksAfterAMissedDay() {
        assertEquals(0, StatsService.currentStreak(days(today.minusDays(2)), today));
    }

    @Test
    public void bestStreakFindsLongestRun() {
        assertEquals(3, StatsService.bestStreak(days(today.minusDays(10), today.minusDays(9), today.minusDays(8), today.minusDays(3))));
        assertEquals(0, StatsService.bestStreak(days()));
    }
}
