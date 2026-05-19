package com.medical.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Request message for patient validation via RabbitMQ.
 * Sent from appointments-service to users-service.
 * Must match the structure expected by users-service PatientValidationListener.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientValidationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique correlation ID to match request with response.
     */
    private String correlationId;

    /**
     * Document number to validate.
     */
    private String documentNumber;

    /**
     * Timestamp when request was created.
     */
    private LocalDateTime timestamp;
}
