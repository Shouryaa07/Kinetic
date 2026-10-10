package com.kinetic.model;

/** The kinds of workout a member can log, with display metadata for the UI. */
public enum ActivityType {
    RUN("Run", "#f97316", true),
    WALK("Walk", "#22c55e", true),
    CYCLE("Cycle", "#0ea5e9", true),
    SWIM("Swim", "#06b6d4", true),
    STRENGTH("Strength", "#8b5cf6", false),
    HIIT("HIIT", "#ef4444", false),
    YOGA("Yoga", "#ec4899", false);

    private final String label;
    private final String color;
    private final boolean distanceBased;

    ActivityType(String label, String color, boolean distanceBased) {
        this.label = label;
        this.color = color;
        this.distanceBased = distanceBased;
    }

    public String label() {
        return label;
    }

    public String color() {
        return color;
    }

    public boolean distanceBased() {
        return distanceBased;
    }

    /** Lenient lookup used for request parameters. */
    public static ActivityType parse(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
