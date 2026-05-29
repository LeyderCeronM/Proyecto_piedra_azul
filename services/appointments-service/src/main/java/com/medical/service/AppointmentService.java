package com.medical.service;

import com.medical.dto.AppointmentResponse;
import com.medical.dto.CreateAppointmentRequest;
import com.medical.dto.UpdateAppointmentRequest;

import java.util.List;

/**
 * Service interface defining the appointment lifecycle contract.
 * Following DIP (ADR-004) — depend on abstractions, not implementations.
 */
public interface AppointmentService {

    /**
     * Create a new appointment with async patient validation.
     */
    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    /**
     * Get all appointments.
     */
    List<AppointmentResponse> getAllAppointments();

    /**
     * Get an appointment by its ID.
     */
    AppointmentResponse getAppointmentById(Long id);

    /**
     * Get all appointments for a specific patient by document number.
     */
    List<AppointmentResponse> getAppointmentsByPatient(String documentNumber);

    /**
     * Update/reschedule an existing appointment.
     */
    AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request);

    /**
     * Cancel an existing appointment.
     */
    void cancelAppointment(Long id);
}
