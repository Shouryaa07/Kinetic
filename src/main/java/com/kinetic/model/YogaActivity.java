package com.kinetic.model;

import java.time.LocalDate;

/** Yoga, stretching and mobility work. */
public class YogaActivity extends Activity {

    public YogaActivity(Long id, long userId, LocalDate date, int durationMin, double distanceKm,
                        int intensity, int calories, String notes) {
        super(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
    }

    @Override
    public ActivityType type() {
        return ActivityType.YOGA;
    }

    @Override
    protected double met() {
        return 1.8 + intensity() * 0.5;
    }
}
