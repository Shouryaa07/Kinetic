package com.kinetic.concurrent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe set of member ids that currently hold a signed-in session. */
public final class OnlineUsers {
    private final Set<Long> ids = ConcurrentHashMap.newKeySet();

    public void signedIn(long userId) {
        ids.add(userId);
    }

    public void signedOut(long userId) {
        ids.remove(userId);
    }

    public int count() {
        return ids.size();
    }
}
