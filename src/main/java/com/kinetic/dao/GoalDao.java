package com.kinetic.dao;

import com.kinetic.db.Database;
import com.kinetic.model.Goal;
import com.kinetic.model.GoalMetric;
import java.util.List;
import java.util.Optional;

/** Database access for {@link Goal}. */
public class GoalDao extends JdbcDao implements Dao<Goal, Long> {
    private static final RowMapper<Goal> MAPPER = rs -> new Goal(
            rs.getLong("id"), rs.getLong("user_id"), GoalMetric.valueOf(rs.getString("metric")), rs.getDouble("target"));

    public GoalDao(Database db) {
        super(db);
    }

    @Override
    public Optional<Goal> findById(Long id) {
        return queryOne("SELECT id, user_id, metric, target FROM goals WHERE id = ?", MAPPER, id);
    }

    @Override
    public List<Goal> findAll() {
        return query("SELECT id, user_id, metric, target FROM goals ORDER BY id", MAPPER);
    }

    public List<Goal> findByUser(long userId) {
        return query("SELECT id, user_id, metric, target FROM goals WHERE user_id = ? ORDER BY id", MAPPER, userId);
    }

    /** Update-or-insert, portable across databases. */
    @Override
    public Goal save(Goal g) {
        int changed = update("UPDATE goals SET target = ? WHERE user_id = ? AND metric = ?",
                g.target(), g.userId(), g.metric().name());
        if (changed == 0) {
            g.setId(insert("INSERT INTO goals (user_id, metric, target) VALUES (?, ?, ?)",
                    g.userId(), g.metric().name(), g.target()));
        }
        return g;
    }

    public void deleteByMetric(long userId, GoalMetric metric) {
        update("DELETE FROM goals WHERE user_id = ? AND metric = ?", userId, metric.name());
    }

    @Override
    public boolean deleteById(Long id) {
        return update("DELETE FROM goals WHERE id = ?", id) > 0;
    }
}
