package com.kinetic.exception;

/** Raised when authentication fails: wrong password, locked account, invalid Google token. */
public class AuthException extends KineticException {
    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
