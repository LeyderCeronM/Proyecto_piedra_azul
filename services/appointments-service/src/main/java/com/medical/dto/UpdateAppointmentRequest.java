package com.medical.dto;

import lombok.*;

/**
 * Request DTO for updating/rescheduling an appointment.
 * Only date and time can be changed (per postman-endpoints.md PUT /api/appointments/{id}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAppointmentRequest {

    private String date;

    private String time;
}
