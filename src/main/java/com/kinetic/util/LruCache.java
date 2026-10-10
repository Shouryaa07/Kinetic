package com.kinetic.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A small generic, thread-safe least-recently-used cache. It wraps an access-ordered
 * {@link LinkedHashMap}; because a read reorders entries, reads take the same lock as writes.
 */
public final class LruCache<K, V> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<K, V> map;

    public LruCache(int capacity) {
        this.map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        };
    }

    public Optional<V> get(K key) {
        lock.lock();
        try {
            return Optional.ofNullable(map.get(key));
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            map.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public void remove(K key) {
        lock.lock();
        try {
            map.remove(key);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return map.size();
        } finally {
            lock.unlock();
        }
    }
}
