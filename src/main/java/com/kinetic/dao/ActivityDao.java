package com.kinetic.dao;

import com.kinetic.db.Database;
import com.kinetic.model.Activity;
import com.kinetic.model.ActivityFactory;
import com.kinetic.model.ActivityType;
import com.kinetic.model.Bests;
import com.kinetic.model.Totals;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Database access for {@link Activity}. Rows are rebuilt as the right subclass by the factory. */
public class ActivityDao extends JdbcDao implements Dao<Activity, Long> {
    private static final String COLUMNS =
            "id, user_id, type, performed_on, duration_min, distance_km, intensity, calories, notes";

    private static final RowMapper<Activity> MAPPER = rs -> ActivityFactory.create(
            ActivityType.valueOf(rs.getString("type")),
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getObject("performed_on", LocalDate.class),
            rs.getInt("duration_min"),
            rs.getDouble("distance_km"),
            rs.getInt("intensity"),
            rs.getInt("calories"),
            rs.getString("notes"));

    public ActivityDao(Database db) {
        super(db);
    }

    @Override
    public Optional<Activity> findById(Long id) {
        return queryOne("SELECT " + COLUMNS + " FROM activities WHERE id = ?", MAPPER, id);
    }

    @Override
    public List<Activity> findAll() {
        return query("SELECT " + COLUMNS + " FROM activities ORDER BY performed_on DESC, id DESC", MAPPER);
    }

    /** Newest first, optionally filtered by type. */
    public List<Activity> findByUser(long userId, ActivityType type, int limit) {
        if (type == null) {
            return query("SELECT " + COLUMNS + " FROM activities WHERE user_id = ? "
                    + "ORDER BY performed_on DESC, id DESC LIMIT ?", MAPPER, userId, limit);
        }
        return query("SELECT " + COLUMNS + " FROM activities WHERE user_id = ? AND type = ? "
                + "ORDER BY performed_on DESC, id DESC LIMIT ?", MAPPER, userId, type.name(), limit);
    }

    /** Oldest first, both dates inclusive. */
    public List<Activity> findBetween(long userId, LocalDate from, LocalDate to) {
        return query("SELECT " + COLUMNS + " FROM activities WHERE user_id = ? AND performed_on BETWEEN ? AND ? "
                + "ORDER BY performed_on, id", MAPPER, userId, from, to);
    }

    @Override
    public Activity save(Activity a) {
        if (a.getId() == null) {
            a.setId(insert("INSERT INTO activities (user_id, type, performed_on, duration_min, distance_km, "
                            + "intensity, calories, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    a.userId(), a.type().name(), a.date(), a.durationMinutes(), a.distanceKm(),
                    a.intensity(), a.caloriesBurned(), a.notes()));
        } else {
            update("UPDATE activities SET type = ?, performed_on = ?, duration_min = ?, distance_km = ?, "
                            + "intensity = ?, calories = ?, notes = ? WHERE id = ? AND user_id = ?",
                    a.type().name(), a.date(), a.durationMinutes(), a.distanceKm(), a.intensity(),
                    a.caloriesBurned(), a.notes(), a.getId(), a.userId());
        }
        return a;
    }

    /** Inserts many activities with one JDBC batch. Used by the demo-data seeder. */
    public void saveAll(List<Activity> activities) {
        db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO activities (user_id, type, performed_on, "
                    + "duration_min, distance_km, intensity, calories, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (Activity a : activities) {
                    bind(ps, a.userId(), a.type().name(), a.date(), a.durationMinutes(), a.distanceKm(),
                            a.intensity(), a.caloriesBurned(), a.notes());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            return null;
        });
    }

    @Override
    public boolean deleteById(Long id) {
        return update("DELETE FROM activities WHERE id = ?", id) > 0;
    }

    /** Deletes only if the row belongs to the user, so one member cannot remove another's data. */
    public boolean deleteOwned(long userId, long id) {
        return update("DELETE FROM activities WHERE id = ? AND user_id = ?", id, userId) > 0;
    }

    public Totals totals(long userId) {
        return queryOne("SELECT COUNT(*) AS n, COALESCE(SUM(duration_min), 0) AS mins, "
                        + "COALESCE(SUM(calories), 0) AS kcal, COALESCE(SUM(distance_km), 0) AS km "
                        + "FROM activities WHERE user_id = ?",
                rs -> new Totals(rs.getInt("n"), rs.getInt("mins"), rs.getInt("kcal"), rs.getDouble("km")),
                userId).orElse(Totals.EMPTY);
    }

    public Bests bests(long userId) {
        return queryOne("SELECT COALESCE(MAX(distance_km), 0) AS km, COALESCE(MAX(duration_min), 0) AS mins, "
                        + "COALESCE(MAX(calories), 0) AS kcal FROM activities WHERE user_id = ?",
                rs -> new Bests(rs.getDouble("km"), rs.getInt("mins"), rs.getInt("kcal")),
                userId).orElse(new Bests(0, 0, 0));
    }

    public List<LocalDate> activeDays(long userId) {
        List<LocalDate> days = new ArrayList<>(query(
                "SELECT DISTINCT performed_on FROM activities WHERE user_id = ? ORDER BY performed_on",
                rs -> rs.getObject("performed_on", LocalDate.class), userId));
        return days;
    }
}
