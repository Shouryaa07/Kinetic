package com.kinetic.model;

import java.time.LocalDate;

/** Walking and hiking. */
public class WalkActivity extends DistanceActivity {

    public WalkActivity(Long id, long userId, LocalDate date, int durationMin, double distanceKm,
                        int intensity, int calories, String notes) {
        super(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
    }

    @Override
    public ActivityType type() {
        return ActivityType.WALK;
    }

    @Override
    protected double met() {
        return speedKmh() > 0 ? clamp(1.5 + speedKmh() * 0.45, 2.0, 7.0) : 2.5 + intensity() * 0.4;
    }
}
