package com.kinetic.service;

import com.kinetic.dao.GoalDao;
import com.kinetic.db.Database;
import com.kinetic.exception.KineticException;
import com.kinetic.exception.ValidationException;
import com.kinetic.model.Goal;
import com.kinetic.model.GoalMetric;
import java.util.List;
import java.util.Map;

/** Weekly goals. Saving all four metrics happens in one transaction. */
public class GoalService {
    private final GoalDao goals;
    private final Database db;
    private final DataChangeListener listener;

    public GoalService(GoalDao goals, Database db, DataChangeListener listener) {
        this.goals = goals;
        this.db = db;
        this.listener = listener;
    }

    public List<Goal> forUser(long userId) {
        return goals.findByUser(userId);
    }

    /** A blank or zero value removes the goal; anything else is upserted. All or nothing. */
    public void saveAll(long userId, Map<GoalMetric, String> raw) throws KineticException {
        Map<GoalMetric, Double> parsed = new java.util.EnumMap<>(GoalMetric.class);
        for (Map.Entry<GoalMetric, String> e : raw.entrySet()) {
            String v = e.getValue();
            if (v == null || v.isBlank()) {
                parsed.put(e.getKey(), 0.0);
                continue;
            }
            try {
                double d = Double.parseDouble(v.trim().replace(',', '.'));
                if (d < 0 || d > 100_000) {
                    throw new ValidationException(e.getKey().label(), e.getKey().label() + " goal is out of range.");
                }
                parsed.put(e.getKey(), d);
            } catch (NumberFormatException ex) {
                throw new ValidationException(e.getKey().label(), e.getKey().label() + " goal must be a number.");
            }
        }
        db.inTransaction(() -> {
            parsed.forEach((metric, target) -> {
                if (target <= 0) {
                    goals.deleteByMetric(userId, metric);
                } else {
                    goals.save(new Goal(null, userId, metric, target));
                }
            });
            return null;
        });
        listener.onChange(userId);
    }
}
