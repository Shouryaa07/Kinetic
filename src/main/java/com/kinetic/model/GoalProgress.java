package com.kinetic.model;

/** A goal together with how much of it has been achieved this week. */
public record GoalProgress(Goal goal, double current) {

    /** Completion from 0 to 100, capped. */
    public int percent() {
        if (goal.target() <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round(current / goal.target() * 100));
    }

    public boolean achieved() {
        return current >= goal.target();
    }
}
