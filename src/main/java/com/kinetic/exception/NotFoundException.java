package com.kinetic.exception;

/** Raised when a requested record does not exist or does not belong to the caller. */
public class NotFoundException extends KineticException {
    public NotFoundException(String message) {
        super(message);
    }
}
