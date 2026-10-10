package com.kinetic.service;

import com.kinetic.concurrent.AuditLogger;
import com.kinetic.dao.ActivityDao;
import com.kinetic.dao.UserDao;
import com.kinetic.exception.KineticException;
import com.kinetic.exception.NotFoundException;
import com.kinetic.exception.ValidationException;
import com.kinetic.model.Activity;
import com.kinetic.model.ActivityFactory;
import com.kinetic.model.ActivityType;
import com.kinetic.model.User;
import com.kinetic.util.Validate;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Logging, listing and deleting workouts. */
public class ActivityService {
    private final ActivityDao activities;
    private final UserDao users;
    private final AuditLogger audit;
    private final DataChangeListener listener;

    public ActivityService(ActivityDao activities, UserDao users, AuditLogger audit, DataChangeListener listener) {
        this.activities = activities;
        this.users = users;
        this.audit = audit;
        this.listener = listener;
    }

    /** Validates the form values and builds the right Activity subclass, with calories for the member's weight. */
    private Activity build(Long id, long userId, String typeRaw, String dateRaw, String durationRaw, String distanceRaw,
                           String intensityRaw, String notesRaw) throws KineticException {
        ActivityType type = ActivityType.parse(typeRaw);
        if (type == null) {
            throw new ValidationException("Type", "Choose an activity type.");
        }
        LocalDate date = Validate.date(dateRaw, "Date");
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new ValidationException("Date", "Date cannot be in the future.");
        }
        int duration = Validate.integer(durationRaw, "Duration", 1, 1440);
        double distance = type.distanceBased() ? Validate.decimal(distanceRaw, "Distance", 0, 1000) : 0;
        int intensity = Validate.integer(intensityRaw, "Intensity", 1, 5);
        String notes = notesRaw == null ? "" : notesRaw.trim();
        if (notes.length() > 500) {
            throw new ValidationException("Notes", "Notes must be at most 500 characters.");
        }
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Member not found."));
        Activity a = ActivityFactory.create(type, id, userId, date, duration, distance, intensity, 0,
                notes.isEmpty() ? null : notes);
        a.applyCalories(user.getWeightKg()); // polymorphic: each subclass supplies its own MET
        return a;
    }

    public Activity log(long userId, String typeRaw, String dateRaw, String durationRaw, String distanceRaw,
                        String intensityRaw, String notesRaw) throws KineticException {
        Activity a = build(null, userId, typeRaw, dateRaw, durationRaw, distanceRaw, intensityRaw, notesRaw);
        activities.save(a);
        listener.onChange(userId);
        audit.log(userId, "ACTIVITY_ADDED", a.type().name());
        return a;
    }

    /** Replaces an existing workout. The row must belong to the member. */
    public Activity update(long userId, long id, String typeRaw, String dateRaw, String durationRaw, String distanceRaw,
                           String intensityRaw, String notesRaw) throws KineticException {
        find(userId, id).orElseThrow(() -> new NotFoundException("That workout no longer exists."));
        Activity a = build(id, userId, typeRaw, dateRaw, durationRaw, distanceRaw, intensityRaw, notesRaw);
        activities.save(a);
        listener.onChange(userId);
        audit.log(userId, "ACTIVITY_EDITED", String.valueOf(id));
        return a;
    }

    /** A workout by id, only if it belongs to the member. */
    public Optional<Activity> find(long userId, long id) {
        return activities.findById(id).filter(a -> a.userId() == userId);
    }

    public List<Activity> list(long userId, ActivityType type, int limit) {
        return activities.findByUser(userId, type, limit);
    }

    public void delete(long userId, long activityId) throws NotFoundException {
        if (!activities.deleteOwned(userId, activityId)) {
            throw new NotFoundException("That workout no longer exists.");
        }
        listener.onChange(userId);
        audit.log(userId, "ACTIVITY_DELETED", String.valueOf(activityId));
    }
}
