package com.kinetic.model;

import java.time.LocalDate;

/** Intermediate class for activities measured in kilometres, adding speed and pace. */
public abstract class DistanceActivity extends Activity {

    protected DistanceActivity(Long id, long userId, LocalDate date, int durationMin, double distanceKm,
                               int intensity, int calories, String notes) {
        super(id, userId, date, durationMin, distanceKm, intensity, calories, notes);
    }

    /** Average speed in km/h, or 0 when no distance was recorded. */
    public double speedKmh() {
        return distanceKm() > 0 && durationMinutes() > 0 ? distanceKm() / (durationMinutes() / 60.0) : 0;
    }

    /** Pace as m:ss per kilometre, or an empty string when there is no distance. */
    public String pace() {
        if (distanceKm() <= 0) {
            return "";
        }
        double minutesPerKm = durationMinutes() / distanceKm();
        int whole = (int) minutesPerKm;
        int seconds = (int) Math.round((minutesPerKm - whole) * 60);
        if (seconds == 60) {
            whole++;
            seconds = 0;
        }
        return String.format("%d:%02d /km", whole, seconds);
    }

    @Override
    public String summary() {
        if (distanceKm() <= 0) {
            return super.summary();
        }
        return String.format("%.1f km, %d min", distanceKm(), durationMinutes());
    }
}
