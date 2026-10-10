package com.kinetic.model;

import java.time.LocalDate;

/** Steps, water and sleep for one member on one day. */
public record DailyLog(Long id, long userId, LocalDate date, int steps, int waterMl, int sleepMin) implements Entity {
    @Override
    public Long getId() {
        return id;
    }

    public DailyLog withId(Long newId) {
        return new DailyLog(newId, userId, date, steps, waterMl, sleepMin);
    }
}
