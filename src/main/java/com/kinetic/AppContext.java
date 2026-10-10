package com.kinetic;

import com.kinetic.concurrent.AuditLogger;
import com.kinetic.concurrent.OnlineUsers;
import com.kinetic.dao.ActivityDao;
import com.kinetic.dao.AuditDao;
import com.kinetic.dao.BodyMetricDao;
import com.kinetic.dao.DailyLogDao;
import com.kinetic.dao.GoalDao;
import com.kinetic.dao.UserDao;
import com.kinetic.db.Database;
import com.kinetic.service.ActivityService;
import com.kinetic.service.AuthService;
import com.kinetic.service.GoalService;
import com.kinetic.service.HealthService;
import com.kinetic.service.GoogleTokenVerifier;
import com.kinetic.service.StatsService;
import com.kinetic.service.TokenInfoVerifier;
import com.kinetic.util.Config;

/**
 * Composition root: builds every DAO and service once and wires them together by constructor
 * injection. Servlets fetch this single object from the ServletContext.
 */
public final class AppContext implements AutoCloseable {
    public static final String KEY = "kinetic.app";

    public final Database db;
    public final UserDao users;
    public final ActivityDao activityDao;
    public final GoalDao goalDao;
    public final AuditDao auditDao;
    public final DailyLogDao dailyDao;
    public final BodyMetricDao bodyDao;
    public final AuditLogger audit;
    public final OnlineUsers online = new OnlineUsers();
    public final StatsService stats;
    public final AuthService auth;
    public final ActivityService activities;
    public final GoalService goals;
    public final HealthService health;
    public final String googleClientId;
    public final boolean demoEnabled;

    public AppContext(Database db, GoogleTokenVerifier verifier, String googleClientId, boolean demoEnabled) {
        this.db = db;
        this.googleClientId = googleClientId == null ? "" : googleClientId.trim();
        this.demoEnabled = demoEnabled;
        this.users = new UserDao(db);
        this.activityDao = new ActivityDao(db);
        this.goalDao = new GoalDao(db);
        this.auditDao = new AuditDao(db);
        this.dailyDao = new DailyLogDao(db);
        this.bodyDao = new BodyMetricDao(db);
        this.audit = new AuditLogger(auditDao);
        this.stats = new StatsService(activityDao, goalDao, dailyDao, bodyDao);
        this.auth = new AuthService(users, verifier, audit);
        this.activities = new ActivityService(activityDao, users, audit, stats);
        this.goals = new GoalService(goalDao, db, stats);
        this.health = new HealthService(dailyDao, bodyDao, users, db, stats);
    }

    /** Builds the production context from application.properties. */
    public static AppContext fromConfig() {
        Database db = new Database(
                Config.get("db.url", "jdbc:h2:file:./data/kinetic;DB_CLOSE_DELAY=-1"),
                Config.get("db.user", "sa"),
                Config.get("db.password", ""),
                Config.getInt("db.pool.size", 8));
        db.initSchema();
        String clientId = Config.get("google.client.id", "");
        return new AppContext(db, new TokenInfoVerifier(clientId), clientId, Config.getBool("demo.enabled", true));
    }

    @Override
    public void close() {
        audit.close();
        db.close();
    }
}
