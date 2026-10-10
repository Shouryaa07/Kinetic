package com.kinetic.dao;

import com.kinetic.db.Database;

/** Append-only security and usage log. Written from a background thread by AuditLogger. */
public class AuditDao extends JdbcDao {

    public AuditDao(Database db) {
        super(db);
    }

    public void insert(Long userId, String event, String detail) {
        update("INSERT INTO audit_log (user_id, event, detail) VALUES (?, ?, ?)", userId, event, detail);
    }

    public int count(String event) {
        return queryOne("SELECT COUNT(*) AS n FROM audit_log WHERE event = ?", rs -> rs.getInt("n"), event).orElse(0);
    }
}
