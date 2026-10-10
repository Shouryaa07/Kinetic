package com.kinetic.model;

import java.util.Collection;

/** Summed figures for a set of activities. */
public record Totals(int workouts, int minutes, int calories, double distanceKm) {
    public static final Totals EMPTY = new Totals(0, 0, 0, 0);

    public static Totals of(Collection<? extends Trackable> items) {
        return new Totals(
                items.size(),
                items.stream().mapToInt(Trackable::durationMinutes).sum(),
                items.stream().mapToInt(Trackable::caloriesBurned).sum(),
                items.stream().mapToDouble(Trackable::distanceKm).sum());
    }
}
