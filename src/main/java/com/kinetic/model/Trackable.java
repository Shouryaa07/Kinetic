package com.kinetic.model;

import java.time.LocalDate;

/**
 * Something that contributes to a fitness total. Goals and statistics are written against this
 * interface, so they work for any current or future kind of activity.
 */
public interface Trackable {
    LocalDate date();

    int durationMinutes();

    int caloriesBurned();

    double distanceKm();
}
