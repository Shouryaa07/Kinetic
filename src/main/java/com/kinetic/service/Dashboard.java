package com.kinetic.service;

import com.kinetic.model.Activity;
import com.kinetic.model.ActivityType;
import com.kinetic.model.Bests;
import com.kinetic.model.BodyMetric;
import com.kinetic.model.DailyLog;
import com.kinetic.model.GoalProgress;
import com.kinetic.model.Totals;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Everything the dashboard and progress pages show, computed once and cached. */
public record Dashboard(
        LocalDate today,
        Totals thisWeek,
        Totals lastWeek,
        int streak,
        int bestStreak,
        List<WeekPoint> weeks,
        Map<LocalDate, Integer> minutesByDay,
        Map<ActivityType, Integer> minutesByType,
        Map<ActivityType, Integer> sessionsByType,
        List<Activity> recent,
        List<GoalProgress> goals,
        Totals lifetime,
        Bests bests,
        Totals todayTotals,
        DailyLog daily,
        List<DailyLog> dailyWeek,
        List<BodyMetric> weights) {

    /** One bar in the weekly chart. */
    public record WeekPoint(LocalDate start, int minutes, int workouts) {
    }
}
