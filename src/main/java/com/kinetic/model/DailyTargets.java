package com.kinetic.model;

/** Daily goals behind the rings and meters. Based on common public-health guidance. */
public final class DailyTargets {
    public static final int MOVE_KCAL = 500;
    public static final int EXERCISE_MIN = 30;
    public static final int STEPS = 8000;
    public static final int WATER_ML = 2500;
    public static final int SLEEP_MIN = 8 * 60;

    private DailyTargets() {
    }

    /** Percentage of a target reached, capped at 100. */
    public static int percent(double value, double target) {
        return target <= 0 ? 0 : (int) Math.min(100, Math.round(value / target * 100));
    }
}
