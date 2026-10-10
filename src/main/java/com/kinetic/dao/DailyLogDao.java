package com.kinetic.dao;

import com.kinetic.db.Database;
import com.kinetic.model.DailyLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Database access for {@link DailyLog}. One row per member per day. */
public class DailyLogDao extends JdbcDao implements Dao<DailyLog, Long> {
    private static final String COLUMNS = "id, user_id, log_date, steps, water_ml, sleep_min";
    private static final RowMapper<DailyLog> MAPPER = rs -> new DailyLog(rs.getLong("id"), rs.getLong("user_id"),
            rs.getObject("log_date", LocalDate.class), rs.getInt("steps"), rs.getInt("water_ml"), rs.getInt("sleep_min"));

    public DailyLogDao(Database db) {
        super(db);
    }

    @Override
    public Optional<DailyLog> findById(Long id) {
        return queryOne("SELECT " + COLUMNS + " FROM daily_logs WHERE id = ?", MAPPER, id);
    }

    public Optional<DailyLog> findByDate(long userId, LocalDate date) {
        return queryOne("SELECT " + COLUMNS + " FROM daily_logs WHERE user_id = ? AND log_date = ?", MAPPER, userId, date);
    }

    @Override
    public List<DailyLog> findAll() {
        return query("SELECT " + COLUMNS + " FROM daily_logs ORDER BY log_date", MAPPER);
    }

    public List<DailyLog> findBetween(long userId, LocalDate from, LocalDate to) {
        return query("SELECT " + COLUMNS + " FROM daily_logs WHERE user_id = ? AND log_date BETWEEN ? AND ? ORDER BY log_date",
                MAPPER, userId, from, to);
    }

    /** Update-or-insert on (user, day). */
    @Override
    public DailyLog save(DailyLog d) {
        int changed = update("UPDATE daily_logs SET steps = ?, water_ml = ?, sleep_min = ? WHERE user_id = ? AND log_date = ?",
                d.steps(), d.waterMl(), d.sleepMin(), d.userId(), d.date());
        if (changed == 0) {
            return d.withId(insert("INSERT INTO daily_logs (user_id, log_date, steps, water_ml, sleep_min) VALUES (?, ?, ?, ?, ?)",
                    d.userId(), d.date(), d.steps(), d.waterMl(), d.sleepMin()));
        }
        return findByDate(d.userId(), d.date()).orElse(d);
    }

    @Override
    public boolean deleteById(Long id) {
        return update("DELETE FROM daily_logs WHERE id = ?", id) > 0;
    }
}
