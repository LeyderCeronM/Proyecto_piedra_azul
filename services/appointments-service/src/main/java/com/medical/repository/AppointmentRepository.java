package com.medical.repository;

import com.medical.entity.Appointment;
import com.medical.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Appointment entity.
 * Provides derived queries for common lookups.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find all appointments for a specific patient.
     */
    List<Appointment> findByPatientDocument(String patientDocument);

    /**
     * Find appointments for a professional on a specific date (for conflict detection).
     */
    List<Appointment> findByProfessionalIdAndAppointmentDate(Long professionalId, LocalDate appointmentDate);

    /**
     * Find appointments filtered by status.
     */
    List<Appointment> findByStatus(AppointmentStatus status);
}
