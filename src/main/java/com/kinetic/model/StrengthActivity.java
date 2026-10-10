package com.kinetic.model;

import java.time.LocalDate;

/** Weight training and bodyweight strength work. */
public class StrengthActivity extends Activity {

    public StrengthActivity(Long id, long userId, LocalDate date, int durationMin, double distanceKm,
                            int intensity, int calories, String notes) {
        super(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
    }

    @Override
    public ActivityType type() {
        return ActivityType.STRENGTH;
    }

    @Override
    protected double met() {
        return 2.5 + intensity() * 0.9;
    }
}
