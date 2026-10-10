package com.kinetic.exception;

/** Unchecked wrapper for {@link java.sql.SQLException} so DAO signatures stay clean. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
