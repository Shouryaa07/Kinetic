package com.kinetic.dao;

import com.kinetic.db.Database;
import com.kinetic.model.BodyMetric;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Database access for {@link BodyMetric}. One weight per member per day. */
public class BodyMetricDao extends JdbcDao implements Dao<BodyMetric, Long> {
    private static final String COLUMNS = "id, user_id, measured_on, weight_kg";
    private static final RowMapper<BodyMetric> MAPPER = rs -> new BodyMetric(rs.getLong("id"), rs.getLong("user_id"),
            rs.getObject("measured_on", LocalDate.class), rs.getDouble("weight_kg"));

    public BodyMetricDao(Database db) {
        super(db);
    }

    @Override
    public Optional<BodyMetric> findById(Long id) {
        return queryOne("SELECT " + COLUMNS + " FROM body_metrics WHERE id = ?", MAPPER, id);
    }

    @Override
    public List<BodyMetric> findAll() {
        return query("SELECT " + COLUMNS + " FROM body_metrics ORDER BY measured_on", MAPPER);
    }

    public List<BodyMetric> findBetween(long userId, LocalDate from, LocalDate to) {
        return query("SELECT " + COLUMNS + " FROM body_metrics WHERE user_id = ? AND measured_on BETWEEN ? AND ? ORDER BY measured_on",
                MAPPER, userId, from, to);
    }

    /** Newest first. */
    public List<BodyMetric> findRecent(long userId, int limit) {
        return query("SELECT " + COLUMNS + " FROM body_metrics WHERE user_id = ? ORDER BY measured_on DESC LIMIT ?", MAPPER, userId, limit);
    }

    public Optional<BodyMetric> latest(long userId) {
        return findRecent(userId, 1).stream().findFirst();
    }

    /** Update-or-insert on (user, day). */
    @Override
    public BodyMetric save(BodyMetric m) {
        int changed = update("UPDATE body_metrics SET weight_kg = ? WHERE user_id = ? AND measured_on = ?",
                m.weightKg(), m.userId(), m.date());
        if (changed == 0) {
            return m.withId(insert("INSERT INTO body_metrics (user_id, measured_on, weight_kg) VALUES (?, ?, ?)",
                    m.userId(), m.date(), m.weightKg()));
        }
        return m;
    }

    @Override
    public boolean deleteById(Long id) {
        return update("DELETE FROM body_metrics WHERE id = ?", id) > 0;
    }
}
