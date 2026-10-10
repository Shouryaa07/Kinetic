package com.kinetic.model;

import java.time.LocalDate;

/**
 * Base class for every logged workout.
 *
 * <p>Subclasses decide how intense a session was by supplying a MET value (metabolic
 * equivalent of task). The calorie estimate itself is a template method defined once here:
 * {@code kcal = MET x body weight (kg) x hours}. This is the main use of inheritance and
 * polymorphism in the project: callers hold an {@code Activity} and never test its type.
 */
public abstract class Activity implements Entity, Trackable, Comparable<Activity> {
    private Long id;
    private final long userId;
    private final LocalDate date;
    private final int durationMin;
    private final double distanceKm;
    private final int intensity;
    private final String notes;
    private int calories;

    protected Activity(Long id, long userId, LocalDate date, int durationMin, double distanceKm,
                       int intensity, int calories, String notes) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.durationMin = durationMin;
        this.distanceKm = distanceKm;
        this.intensity = intensity;
        this.calories = calories;
        this.notes = notes;
    }

    /** The category of this activity. */
    public abstract ActivityType type();

    /** Metabolic equivalent for this particular session. */
    protected abstract double met();

    /** One-line description of the effort, such as "5.2 km, 5:12 /km". */
    public String summary() {
        return durationMin + " min";
    }

    /** Template method: the same formula for every subclass, driven by its {@link #met()}. */
    public final int estimateCalories(double weightKg) {
        return (int) Math.round(met() * weightKg * durationMin / 60.0);
    }

    public void applyCalories(double weightKg) {
        this.calories = estimateCalories(weightKg);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long userId() {
        return userId;
    }

    @Override
    public LocalDate date() {
        return date;
    }

    @Override
    public int durationMinutes() {
        return durationMin;
    }

    @Override
    public int caloriesBurned() {
        return calories;
    }

    @Override
    public double distanceKm() {
        return distanceKm;
    }

    public int intensity() {
        return intensity;
    }

    public String notes() {
        return notes;
    }

    /** Natural order: oldest first, ties broken by id. */
    @Override
    public int compareTo(Activity other) {
        int byDate = date.compareTo(other.date);
        if (byDate != 0) {
            return byDate;
        }
        return Long.compare(id == null ? 0 : id, other.id == null ? 0 : other.id);
    }

    protected static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
