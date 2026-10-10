package com.kinetic.model;

import java.time.LocalDate;

/** Swimming. Effort depends on stroke and pace, so intensity drives the MET value. */
public class SwimActivity extends DistanceActivity {

    public SwimActivity(Long id, long userId, LocalDate date, int durationMin, double distanceKm,
                        int intensity, int calories, String notes) {
        super(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
    }

    @Override
    public ActivityType type() {
        return ActivityType.SWIM;
    }

    @Override
    protected double met() {
        return 3.5 + intensity() * 1.3;
    }
}
