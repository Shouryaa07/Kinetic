package com.kinetic;

import com.kinetic.model.Activity;
import com.kinetic.model.ActivityFactory;
import com.kinetic.model.ActivityType;
import com.kinetic.model.AuthProvider;
import com.kinetic.model.BodyMetric;
import com.kinetic.model.DailyLog;
import com.kinetic.model.Goal;
import com.kinetic.model.GoalMetric;
import com.kinetic.model.User;
import com.kinetic.service.AuthService;
import com.kinetic.util.PasswordHasher;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Creates a demo member with sixteen weeks of believable workouts, so every screen has data. */
public final class DemoData {
    public static final String PASSWORD = "Demo@12345";

    private DemoData() {
    }

    public static void seedIfEmpty(AppContext app) {
        if (app.users.findByEmail(AuthService.DEMO_EMAIL).isPresent()) {
            return;
        }
        User u = new User();
        u.setEmail(AuthService.DEMO_EMAIL);
        u.setDisplayName("Alex Rivera");
        u.setPasswordHash(PasswordHasher.hash(PASSWORD));
        u.setProvider(AuthProvider.LOCAL);
        u.setWeightKg(72);
        u.setHeightCm(178);
        User demo = app.users.save(u);

        Random rnd = new Random(42); // fixed seed: same data every time
        LocalDate today = LocalDate.now();
        List<Activity> batch = new ArrayList<>();
        int totalDays = 16 * 7;
        for (int back = totalDays; back >= 0; back--) {
            LocalDate day = today.minusDays(back);
            double ramp = 0.45 + 0.55 * (totalDays - back) / totalDays; // gradually more active
            boolean forced = back <= 5; // guarantee a current streak
            DayOfWeek dow = day.getDayOfWeek();
            double chance = (dow == DayOfWeek.SUNDAY ? 0.25 : 0.62) * ramp + (forced ? 1 : 0);
            if (rnd.nextDouble() > chance) {
                continue;
            }
            ActivityType type = pick(dow, rnd);
            int intensity = 2 + rnd.nextInt(4);
            int minutes = 25 + rnd.nextInt(45) + (int) (ramp * 10);
            double km = 0;
            if (type.distanceBased()) {
                double speed = switch (type) {
                    case RUN -> 9.5 + rnd.nextDouble() * 2.5 + ramp;
                    case WALK -> 4.8 + rnd.nextDouble();
                    case CYCLE -> 20 + rnd.nextDouble() * 6;
                    default -> 2.0 + rnd.nextDouble() * 0.6;
                };
                km = Math.round(speed * minutes / 60.0 * 10) / 10.0;
            }
            Activity a = ActivityFactory.create(type, null, demo.getId(), day, minutes, km, intensity, 0, null);
            a.applyCalories(demo.getWeightKg());
            batch.add(a);
        }
        app.activityDao.saveAll(batch);

        // Thirty days of steps, water and sleep, and a weigh-in every four days trending down.
        for (int back = 29; back >= 0; back--) {
            LocalDate day = today.minusDays(back);
            boolean trained = batch.stream().anyMatch(a -> a.date().equals(day));
            int steps = 5200 + rnd.nextInt(3800) + (trained ? 2200 : 0);
            int water = 1500 + rnd.nextInt(1300);
            int sleep = 360 + rnd.nextInt(130);
            if (back == 0) {
                steps = 6430;
                water = 1750;
                sleep = 7 * 60 + 20;
            }
            app.dailyDao.save(new DailyLog(null, demo.getId(), day, steps, water, sleep));
        }
        for (int back = 112; back >= 0; back -= 4) {
            double kg = 75.6 - (112 - back) * 0.033 + (rnd.nextDouble() - 0.5) * 0.5;
            app.bodyDao.save(new BodyMetric(null, demo.getId(), today.minusDays(back), Math.round(kg * 10) / 10.0));
        }

        app.goalDao.save(new Goal(null, demo.getId(), GoalMetric.WORKOUTS, 6));
        app.goalDao.save(new Goal(null, demo.getId(), GoalMetric.MINUTES, 360));
        app.goalDao.save(new Goal(null, demo.getId(), GoalMetric.CALORIES, 3500));
        app.goalDao.save(new Goal(null, demo.getId(), GoalMetric.DISTANCE, 40));
    }

    private static ActivityType pick(DayOfWeek dow, Random rnd) {
        return switch (dow) {
            case MONDAY, THURSDAY -> rnd.nextBoolean() ? ActivityType.RUN : ActivityType.HIIT;
            case TUESDAY, FRIDAY -> ActivityType.STRENGTH;
            case WEDNESDAY -> rnd.nextBoolean() ? ActivityType.CYCLE : ActivityType.SWIM;
            case SATURDAY -> rnd.nextBoolean() ? ActivityType.RUN : ActivityType.CYCLE;
            case SUNDAY -> rnd.nextBoolean() ? ActivityType.YOGA : ActivityType.WALK;
        };
    }
}
