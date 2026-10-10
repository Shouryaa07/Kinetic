package com.kinetic.service;

import com.kinetic.dao.BodyMetricDao;
import com.kinetic.dao.DailyLogDao;
import com.kinetic.dao.UserDao;
import com.kinetic.db.Database;
import com.kinetic.exception.KineticException;
import com.kinetic.exception.NotFoundException;
import com.kinetic.exception.ValidationException;
import com.kinetic.model.BodyMetric;
import com.kinetic.model.DailyLog;
import com.kinetic.model.User;
import com.kinetic.util.Validate;
import java.time.LocalDate;
import java.util.Optional;

/** Daily steps, water and sleep, plus weight history. */
public class HealthService {
    private final DailyLogDao daily;
    private final BodyMetricDao metrics;
    private final UserDao users;
    private final Database db;
    private final DataChangeListener listener;

    public HealthService(DailyLogDao daily, BodyMetricDao metrics, UserDao users, Database db, DataChangeListener listener) {
        this.daily = daily;
        this.metrics = metrics;
        this.users = users;
        this.db = db;
        this.listener = listener;
    }

    private static LocalDate pastDate(String raw) throws ValidationException {
        LocalDate d = Validate.date(raw, "Date");
        if (d.isAfter(LocalDate.now().plusDays(1))) {
            throw new ValidationException("Date", "Date cannot be in the future.");
        }
        return d;
    }

    /** Saves steps, water (ml) and sleep (hours). A blank field keeps the value already stored for that day. */
    public DailyLog saveDaily(long userId, String dateRaw, String stepsRaw, String waterRaw, String sleepHoursRaw)
            throws KineticException {
        LocalDate date = pastDate(dateRaw);
        DailyLog current = daily.findByDate(userId, date).orElse(new DailyLog(null, userId, date, 0, 0, 0));
        int steps = isBlank(stepsRaw) ? current.steps() : Validate.integer(stepsRaw, "Steps", 0, 100_000);
        int water = isBlank(waterRaw) ? current.waterMl() : Validate.integer(waterRaw, "Water", 0, 10_000);
        int sleep = isBlank(sleepHoursRaw) ? current.sleepMin()
                : (int) Math.round(Validate.decimal(sleepHoursRaw, "Sleep", 0, 24) * 60);
        DailyLog saved = daily.save(new DailyLog(current.id(), userId, date, steps, water, sleep));
        listener.onChange(userId);
        return saved;
    }

    /** Adds water to today's total (quick-add buttons). */
    public DailyLog addWater(long userId, String mlRaw) throws KineticException {
        int add = Validate.integer(mlRaw, "Water", 1, 2000);
        LocalDate today = LocalDate.now();
        DailyLog current = daily.findByDate(userId, today).orElse(new DailyLog(null, userId, today, 0, 0, 0));
        DailyLog saved = daily.save(new DailyLog(current.id(), userId, today, current.steps(),
                Math.min(10_000, current.waterMl() + add), current.sleepMin()));
        listener.onChange(userId);
        return saved;
    }

    /** Records a weight and keeps the member's current weight (used for calories) in step with the latest entry. */
    public BodyMetric recordWeight(long userId, String dateRaw, String weightRaw) throws KineticException {
        LocalDate date = pastDate(dateRaw);
        double kg = Validate.decimal(weightRaw, "Weight", 25, 350);
        if (kg <= 0) {
            throw new ValidationException("Weight", "Enter your weight in kg.");
        }
        return recordWeight(userId, date, kg);
    }

    public BodyMetric recordWeight(long userId, LocalDate date, double kg) throws KineticException {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Member not found."));
        BodyMetric saved = db.inTransaction(() -> {
            BodyMetric m = metrics.save(new BodyMetric(null, userId, date, kg));
            Optional<BodyMetric> latest = metrics.latest(userId);
            latest.ifPresent(l -> {
                user.setWeightKg(l.weightKg());
                users.save(user);
            });
            return m;
        });
        listener.onChange(userId);
        return saved;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
