package com.kinetic.model;

import java.time.LocalDate;

/** Cycling, outdoors or on a stationary bike. */
public class CycleActivity extends DistanceActivity {

    public CycleActivity(Long id, long userId, LocalDate date, int durationMin, double distanceKm,
                         int intensity, int calories, String notes) {
        super(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
    }

    @Override
    public ActivityType type() {
        return ActivityType.CYCLE;
    }

    @Override
    protected double met() {
        return speedKmh() > 0 ? clamp(speedKmh() * 0.45, 4.0, 15.0) : 4 + intensity() * 1.2;
    }

    @Override
    public String summary() {
        return distanceKm() > 0 ? String.format("%.1f km, %.1f km/h", distanceKm(), speedKmh()) : super.summary();
    }
}
