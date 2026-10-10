package com.kinetic.model;

import java.util.Collection;

/**
 * What a weekly goal measures. Each constant overrides {@link #measure}, so adding a metric
 * means adding one constant and no if/else chains elsewhere. The generic bound lets the same
 * code score any collection of {@link Trackable} items.
 */
public enum GoalMetric {
    WORKOUTS("Workouts", "sessions", 4, 1) {
        @Override
        public double measure(Collection<? extends Trackable> items) {
            return items.size();
        }
    },
    MINUTES("Active minutes", "min", 150, 10) {
        @Override
        public double measure(Collection<? extends Trackable> items) {
            return items.stream().mapToInt(Trackable::durationMinutes).sum();
        }
    },
    CALORIES("Calories burned", "kcal", 1500, 50) {
        @Override
        public double measure(Collection<? extends Trackable> items) {
            return items.stream().mapToInt(Trackable::caloriesBurned).sum();
        }
    },
    DISTANCE("Distance", "km", 20, 1) {
        @Override
        public double measure(Collection<? extends Trackable> items) {
            return items.stream().mapToDouble(Trackable::distanceKm).sum();
        }
    };

    private final String label;
    private final String unit;
    private final double suggested;
    private final double step;

    GoalMetric(String label, String unit, double suggested, double step) {
        this.label = label;
        this.unit = unit;
        this.suggested = suggested;
        this.step = step;
    }

    /** Total for the given activities. */
    public abstract double measure(Collection<? extends Trackable> items);

    public String label() {
        return label;
    }

    public String unit() {
        return unit;
    }

    public double suggested() {
        return suggested;
    }

    public double step() {
        return step;
    }
}
