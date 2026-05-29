package com.medical.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Response message for patient validation via RabbitMQ.
 * Received from users-service after patient validation.
 * Must match the structure published by users-service PatientValidationListener.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientValidationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Correlation ID to match with request.
     */
    private String correlationId;

    /**
     * Whether patient exists and is valid.
     */
    private boolean valid;

    /**
     * Patient name if found (for display in appointment).
     */
    private String patientName;

    /**
     * Patient document number.
     */
    private String patientDocument;

    /**
     * Error message if validation failed.
     */
    private String error;

    /**
     * Timestamp when response was created.
     */
    private String timestamp;
}
