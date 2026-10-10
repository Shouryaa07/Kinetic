package com.kinetic.db;

import com.kinetic.exception.DataAccessException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Entry point to JDBC for the DAO classes. It owns the pool, creates the schema, and offers
 * transactions: inside {@link #inTransaction(TxWork)} every DAO call on the same thread shares
 * one connection, which is committed together or rolled back together.
 */
public final class Database implements AutoCloseable {
    /** A unit of JDBC work that touches a connection. */
    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(Connection connection) throws SQLException;
    }

    /** A block of DAO calls to run inside one transaction. */
    @FunctionalInterface
    public interface TxWork<T> {
        T run();
    }

    private final ConnectionPool pool;
    private final ThreadLocal<Connection> txConnection = new ThreadLocal<>();

    public Database(String url, String user, String password, int poolSize) {
        this.pool = new ConnectionPool(url, user, password, poolSize);
    }

    /** Creates tables from schema.sql if they do not exist. */
    public void initSchema() {
        String script;
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new DataAccessException("schema.sql is missing from the classpath");
            }
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataAccessException("Cannot read schema.sql", e);
        }
        withConnection(c -> {
            try (Statement st = c.createStatement()) {
                for (String sql : script.split(";")) {
                    String cleaned = stripComments(sql).trim();
                    if (!cleaned.isEmpty()) {
                        st.execute(cleaned);
                    }
                }
            }
            return null;
        });
    }

    private static String stripComments(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (!line.trim().startsWith("--")) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    /** Runs the work on the transaction's connection if one is open, else on a pooled one. */
    public <T> T withConnection(SqlFunction<T> work) {
        Connection tx = txConnection.get();
        try {
            if (tx != null) {
                return work.apply(tx);
            }
            try (Connection c = pool.borrow()) {
                return work.apply(c);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error: " + e.getMessage(), e);
        }
    }

    /** Runs the work atomically. Any exception rolls everything back. */
    public <T> T inTransaction(TxWork<T> work) {
        if (txConnection.get() != null) {
            return work.run(); // already inside a transaction: join it
        }
        Connection c = pool.borrow();
        try {
            c.setAutoCommit(false);
            txConnection.set(c);
            T result = work.run();
            c.commit();
            return result;
        } catch (SQLException e) {
            rollbackQuietly(c);
            throw new DataAccessException("Transaction failed: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollbackQuietly(c);
            throw e;
        } finally {
            txConnection.remove();
            try {
                c.close();
            } catch (SQLException ignored) {
                // returned to the pool
            }
        }
    }

    private static void rollbackQuietly(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignored) {
            // nothing more to do
        }
    }

    @Override
    public void close() {
        pool.close();
    }
}
