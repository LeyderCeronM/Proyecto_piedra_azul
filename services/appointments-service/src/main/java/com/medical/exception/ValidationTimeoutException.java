package com.medical.exception;

/**
 * Thrown when the patient validation service (users-service) does not respond within the timeout.
 */
public class ValidationTimeoutException extends RuntimeException {

    public ValidationTimeoutException() {
        super("Validation service timeout - please try again");
    }
}
