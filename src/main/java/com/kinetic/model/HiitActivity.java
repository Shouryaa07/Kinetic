package com.kinetic.model;

import java.time.LocalDate;

/** High-intensity interval training. */
public class HiitActivity extends Activity {

    public HiitActivity(Long id, long userId, LocalDate date, int durationMin, double distanceKm,
                        int intensity, int calories, String notes) {
        super(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
    }

    @Override
    public ActivityType type() {
        return ActivityType.HIIT;
    }

    @Override
    protected double met() {
        return 5 + intensity() * 1.4;
    }
}
