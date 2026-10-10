package com.kinetic.model;

import java.time.LocalDate;

/** Factory that turns an {@link ActivityType} into the matching concrete subclass. */
public final class ActivityFactory {
    private ActivityFactory() {
    }

    public static Activity create(ActivityType type, Long id, long userId, LocalDate date, int durationMin,
                                  double distanceKm, int intensity, int calories, String notes) {
        return switch (type) {
            case RUN -> new RunActivity(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
            case WALK -> new WalkActivity(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
            case CYCLE -> new CycleActivity(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
            case SWIM -> new SwimActivity(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
            case STRENGTH -> new StrengthActivity(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
            case HIIT -> new HiitActivity(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
            case YOGA -> new YogaActivity(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
        };
    }
}
