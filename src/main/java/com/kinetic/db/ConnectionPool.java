package com.kinetic.db;

import com.kinetic.exception.DataAccessException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A fixed-size JDBC connection pool built on a {@link BlockingQueue}.
 *
 * <p>Borrowed connections are dynamic proxies: calling {@code close()} on one returns the real
 * connection to the pool instead of closing it, so ordinary try-with-resources code works.
 * Threads that find the pool empty wait up to five seconds, which makes the pool safe to
 * share between request threads.
 */
public final class ConnectionPool implements AutoCloseable {
    private static final long WAIT_SECONDS = 5;

    private final String url;
    private final String user;
    private final String password;
    private final BlockingQueue<Connection> idle;
    private final List<Connection> all = new CopyOnWriteArrayList<>();

    public ConnectionPool(String url, String user, String password, int size) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.idle = new ArrayBlockingQueue<>(size);
        try {
            for (int i = 0; i < size; i++) {
                Connection c = open();
                all.add(c);
                idle.add(c);
            }
        } catch (SQLException e) {
            close();
            throw new DataAccessException("Cannot connect to " + url, e);
        }
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** Borrows a connection, waiting briefly if all are in use. */
    public Connection borrow() {
        try {
            Connection raw = idle.poll(WAIT_SECONDS, TimeUnit.SECONDS);
            if (raw == null) {
                throw new DataAccessException("Connection pool exhausted");
            }
            raw = ensureValid(raw);
            return wrap(raw);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DataAccessException("Interrupted while waiting for a connection", e);
        }
    }

    /** Replaces a connection that the database has dropped. */
    private Connection ensureValid(Connection raw) {
        try {
            if (raw.isValid(2)) {
                return raw;
            }
            all.remove(raw);
            Connection fresh = open();
            all.add(fresh);
            return fresh;
        } catch (SQLException e) {
            idle.offer(raw);
            throw new DataAccessException("Cannot validate connection", e);
        }
    }

    private Connection wrap(Connection raw) {
        boolean[] released = {false};
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("close")) {
                if (!released[0]) {
                    released[0] = true;
                    release(raw);
                }
                return null;
            }
            if (name.equals("isClosed")) {
                return released[0];
            }
            try {
                return method.invoke(raw, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, handler);
    }

    private void release(Connection raw) {
        try {
            if (!raw.getAutoCommit()) {
                raw.rollback();
                raw.setAutoCommit(true);
            }
        } catch (SQLException ignored) {
            // a broken connection is replaced by ensureValid on the next borrow
        }
        idle.offer(raw);
    }

    @Override
    public void close() {
        for (Connection c : all) {
            try {
                c.close();
            } catch (SQLException ignored) {
                // shutting down
            }
        }
        all.clear();
        idle.clear();
    }
}
