package com.kinetic.service;

import java.util.concurrent.ConcurrentHashMap;

/** Blocks an email after five failed sign-ins in ten minutes. Thread-safe via atomic compute. */
final class LoginThrottle {
    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MS = 10 * 60_000L;

    private record State(int failures, long windowStart) {
    }

    private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();

    boolean isBlocked(String key, long now) {
        State s = states.get(key);
        return s != null && now - s.windowStart() < WINDOW_MS && s.failures() >= MAX_FAILURES;
    }

    void failure(String key, long now) {
        states.compute(key, (k, s) -> s == null || now - s.windowStart() >= WINDOW_MS
                ? new State(1, now) : new State(s.failures() + 1, s.windowStart()));
    }

    void success(String key) {
        states.remove(key);
    }
}
