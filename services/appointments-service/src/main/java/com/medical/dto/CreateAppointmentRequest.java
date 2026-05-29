package com.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for creating a new appointment.
 * Fields align with postman-endpoints.md POST /api/appointments body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAppointmentRequest {

    @NotBlank(message = "Patient document is required")
    private String patientDocument;

    private String patientName;

    private String patientPhone;

    private Long professionalId;

    private String professionalName;

    @NotBlank(message = "Date is required")
    private String date;

    @NotBlank(message = "Time is required")
    private String time;

    private Integer durationMinutes;

    private String reason;
}
