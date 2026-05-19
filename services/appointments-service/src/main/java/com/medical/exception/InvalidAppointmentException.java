package com.medical.exception;

/**
 * Thrown when appointment data is invalid (past date, time conflict, invalid patient).
 */
public class InvalidAppointmentException extends RuntimeException {

    public InvalidAppointmentException(String message) {
        super(message);
    }
}
