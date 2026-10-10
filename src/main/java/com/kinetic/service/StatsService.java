package com.kinetic.service;

import com.kinetic.dao.ActivityDao;
import com.kinetic.dao.BodyMetricDao;
import com.kinetic.dao.DailyLogDao;
import com.kinetic.dao.GoalDao;
import com.kinetic.model.BodyMetric;
import com.kinetic.model.DailyLog;
import com.kinetic.model.Activity;
import com.kinetic.model.ActivityType;
import com.kinetic.model.Goal;
import com.kinetic.model.GoalProgress;
import com.kinetic.model.Totals;
import com.kinetic.util.LruCache;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Builds the {@link Dashboard} for a member using streams and collectors. Results sit in an
 * {@link LruCache} and are dropped through {@link DataChangeListener} when data changes.
 */
public class StatsService implements DataChangeListener {
    public static final int WEEKS = 17;

    private final ActivityDao activities;
    private final GoalDao goals;
    private final DailyLogDao dailyLogs;
    private final BodyMetricDao bodyMetrics;
    private final LruCache<Long, Dashboard> cache = new LruCache<>(256);

    public StatsService(ActivityDao activities, GoalDao goals, DailyLogDao dailyLogs, BodyMetricDao bodyMetrics) {
        this.activities = activities;
        this.goals = goals;
        this.dailyLogs = dailyLogs;
        this.bodyMetrics = bodyMetrics;
    }

    @Override
    public void onChange(long userId) {
        cache.remove(userId);
    }

    public Dashboard dashboard(long userId) {
        return dashboard(userId, LocalDate.now());
    }

    public Dashboard dashboard(long userId, LocalDate today) {
        Dashboard cached = cache.get(userId).orElse(null);
        if (cached != null && cached.today().equals(today)) {
            return cached;
        }
        Dashboard fresh = compute(userId, today);
        cache.put(userId, fresh);
        return fresh;
    }

    private static LocalDate weekStart(LocalDate d) {
        return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private Dashboard compute(long userId, LocalDate today) {
        LocalDate thisMonday = weekStart(today);
        LocalDate from = thisMonday.minusWeeks(WEEKS - 1);
        List<Activity> window = activities.findBetween(userId, from, today.plusDays(1));

        Map<LocalDate, List<Activity>> byWeek = window.stream()
                .collect(Collectors.groupingBy(a -> weekStart(a.date())));

        List<Dashboard.WeekPoint> weeks = new ArrayList<>();
        for (int i = 0; i < WEEKS; i++) {
            LocalDate start = from.plusWeeks(i);
            Totals t = Totals.of(byWeek.getOrDefault(start, List.of()));
            weeks.add(new Dashboard.WeekPoint(start, t.minutes(), t.workouts()));
        }

        List<Activity> thisWeekItems = byWeek.getOrDefault(thisMonday, List.of());
        Totals thisWeek = Totals.of(thisWeekItems);
        Totals lastWeek = Totals.of(byWeek.getOrDefault(thisMonday.minusWeeks(1), List.of()));

        Map<LocalDate, Integer> minutesByDay = window.stream()
                .collect(Collectors.toMap(Activity::date, Activity::durationMinutes, Integer::sum, TreeMap::new));

        LocalDate thirtyDaysAgo = today.minusDays(29);
        Map<ActivityType, Integer> minutesByType = new EnumMap<>(ActivityType.class);
        Map<ActivityType, Integer> sessionsByType = new EnumMap<>(ActivityType.class);
        for (Activity a : window) {
            if (!a.date().isBefore(thirtyDaysAgo)) {
                minutesByType.merge(a.type(), a.durationMinutes(), Integer::sum);
                sessionsByType.merge(a.type(), 1, Integer::sum);
            }
        }

        List<GoalProgress> progress = new ArrayList<>();
        for (Goal g : goals.findByUser(userId)) {
            progress.add(new GoalProgress(g, g.metric().measure(thisWeekItems)));
        }

        Map<LocalDate, DailyLog> loggedDays = dailyLogs.findBetween(userId, today.minusDays(6), today).stream()
                .collect(Collectors.toMap(DailyLog::date, l -> l));
        List<DailyLog> dailyWeek = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            dailyWeek.add(loggedDays.getOrDefault(d, new DailyLog(null, userId, d, 0, 0, 0)));
        }
        Totals todayTotals = Totals.of(window.stream().filter(a -> a.date().equals(today)).toList());
        List<BodyMetric> weights = bodyMetrics.findBetween(userId, today.minusDays(120), today.plusDays(1));

        TreeSet<LocalDate> days = new TreeSet<>(activities.activeDays(userId));
        return new Dashboard(today, thisWeek, lastWeek, currentStreak(days, today), bestStreak(days),
                weeks, minutesByDay, minutesByType, sessionsByType,
                activities.findByUser(userId, null, 5), progress,
                activities.totals(userId), activities.bests(userId),
                todayTotals, dailyWeek.get(6), dailyWeek, weights);
    }

    /**
     * Consecutive active days ending today. If nothing is logged yet today the streak still
     * counts up to yesterday, so it is not lost until the day is over.
     */
    static int currentStreak(java.util.NavigableSet<LocalDate> days, LocalDate today) {
        LocalDate cursor = days.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    static int bestStreak(java.util.NavigableSet<LocalDate> days) {
        int best = 0;
        int run = 0;
        LocalDate prev = null;
        for (LocalDate d : days) {
            run = prev != null && prev.plusDays(1).equals(d) ? run + 1 : 1;
            best = Math.max(best, run);
            prev = d;
        }
        return best;
    }

    /** Count of sessions per type across a list, used by tests and exports. */
    public static Map<ActivityType, Long> countByType(List<Activity> items) {
        Map<ActivityType, Long> out = new HashMap<>();
        items.forEach(a -> out.merge(a.type(), 1L, Long::sum));
        return out;
    }
}
