package com.medical.dto;

import com.medical.entity.AppointmentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Response DTO for appointment data.
 * Matches the response structure in postman-endpoints.md.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private Long id;
    private String patientDocument;
    private String patientName;
    private String patientPhone;
    private Long professionalId;
    private String professionalName;
    private LocalDate date;
    private LocalTime time;
    private Integer durationMinutes;
    private String reason;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
