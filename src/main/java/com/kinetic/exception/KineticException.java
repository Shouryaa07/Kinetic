package com.kinetic.exception;

/**
 * Root of the checked business exceptions (bad input, failed login, missing record).
 * Callers must handle these, which keeps error paths visible in method signatures.
 * Infrastructure failures use the unchecked {@link DataAccessException} instead.
 */
public class KineticException extends Exception {
    public KineticException(String message) {
        super(message);
    }

    public KineticException(String message, Throwable cause) {
        super(message, cause);
    }
}
