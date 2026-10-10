package com.kinetic.exception;

/** Raised when user input fails validation. Carries the name of the offending field. */
public class ValidationException extends KineticException {
    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
