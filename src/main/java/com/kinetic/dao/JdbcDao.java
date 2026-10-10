package com.kinetic.dao;

import com.kinetic.db.Database;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared JDBC plumbing: prepared statements, parameter binding, mapping and generated keys.
 * Every query uses {@link PreparedStatement}, so user input is never concatenated into SQL.
 */
public abstract class JdbcDao {
    protected final Database db;

    protected JdbcDao(Database db) {
        this.db = db;
    }

    protected <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> rows = new ArrayList<>();
                    while (rs.next()) {
                        rows.add(mapper.map(rs));
                    }
                    return rows;
                }
            }
        });
    }

    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> rows = query(sql, mapper, params);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    protected int update(String sql, Object... params) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                bind(ps, params);
                return ps.executeUpdate();
            }
        });
    }

    /** Runs an INSERT and returns the generated primary key. */
    protected long insert(String sql, Object... params) {
        return db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(ps, params);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    return keys.next() ? keys.getLong(1) : 0L;
                }
            }
        });
    }

    protected static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
