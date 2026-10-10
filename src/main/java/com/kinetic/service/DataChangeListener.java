package com.kinetic.service;

/** Observer notified when a member's activities or goals change, so cached figures can be dropped. */
@FunctionalInterface
public interface DataChangeListener {
    void onChange(long userId);
}
