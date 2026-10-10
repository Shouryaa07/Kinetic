package com.kinetic.model;

/** A weekly target for one metric. */
public class Goal implements Entity {
    private Long id;
    private final long userId;
    private final GoalMetric metric;
    private final double target;

    public Goal(Long id, long userId, GoalMetric metric, double target) {
        this.id = id;
        this.userId = userId;
        this.metric = metric;
        this.target = target;
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

    public GoalMetric metric() {
        return metric;
    }

    public double target() {
        return target;
    }
}
