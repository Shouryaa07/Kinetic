package com.kinetic.model;

import java.time.LocalDate;

/** A weight measurement on a given day. */
public record BodyMetric(Long id, long userId, LocalDate date, double weightKg) implements Entity {
    @Override
    public Long getId() {
        return id;
    }

    public BodyMetric withId(Long newId) {
        return new BodyMetric(newId, userId, date, weightKg);
    }
}
