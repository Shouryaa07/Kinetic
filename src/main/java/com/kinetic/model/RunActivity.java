package com.kinetic.model;

import java.time.LocalDate;

/** Running. With a distance, MET follows speed (about 1 MET per km/h); otherwise intensity. */
public class RunActivity extends DistanceActivity {

    public RunActivity(Long id, long userId, LocalDate date, int durationMin, double distanceKm,
                       int intensity, int calories, String notes) {
        super(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
    }

    @Override
    public ActivityType type() {
        return ActivityType.RUN;
    }

    @Override
    protected double met() {
        return speedKmh() > 0 ? clamp(speedKmh(), 6, 16) : 6 + intensity() * 1.2;
    }

    @Override
    public String summary() {
        return distanceKm() > 0 ? String.format("%.1f km, %s", distanceKm(), pace()) : super.summary();
    }
}
