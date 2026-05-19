package com.medical.service;

import com.medical.dto.*;
import com.medical.entity.Appointment;
import com.medical.entity.AppointmentStatus;
import com.medical.exception.AppointmentNotFoundException;
import com.medical.exception.InvalidAppointmentException;
import com.medical.facade.PatientValidationFacade;
import com.medical.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementation of the appointment lifecycle management.
 * Handles CRUD operations, business validations, and async patient verification.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientValidationFacade patientValidationFacade;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime time = LocalTime.parse(request.getTime());

        // Validate date is not in the past
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidAppointmentException(
                    "Appointment date cannot be in the past");
        }

        // Validate patient via async RabbitMQ communication with users-service
        PatientValidationResponse validation =
                patientValidationFacade.validatePatient(request.getPatientDocument());

        // Use patient name from validation if not provided in request
        String patientName = request.getPatientName() != null && !request.getPatientName().isBlank()
                ? request.getPatientName()
                : validation.getPatientName();

        // Check for time slot conflicts
        validateTimeSlotAvailable(request.getProfessionalId(), date, time,
                request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);

        // Build and persist entity
        Appointment appointment = Appointment.builder()
                .patientDocument(request.getPatientDocument())
                .patientName(patientName)
                .patientPhone(request.getPatientPhone())
                .professionalId(request.getProfessionalId())
                .professionalName(request.getProfessionalName())
                .appointmentDate(date)
                .appointmentTime(time)
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30)
                .reason(request.getReason())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment created with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        return mapToResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatient(String documentNumber) {
        return appointmentRepository.findByPatientDocument(documentNumber).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentException("Cannot update a cancelled appointment");
        }

        if (request.getDate() != null) {
            LocalDate newDate = LocalDate.parse(request.getDate());
            if (newDate.isBefore(LocalDate.now())) {
                throw new InvalidAppointmentException(
                        "Appointment date cannot be in the past");
            }
            appointment.setAppointmentDate(newDate);
        }

        if (request.getTime() != null) {
            appointment.setAppointmentTime(LocalTime.parse(request.getTime()));
        }

        // Re-validate time slot if date or time changed
        validateTimeSlotAvailable(
                appointment.getProfessionalId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getDurationMinutes(),
                id  // exclude current appointment from conflict check
        );

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment updated with id: {}", updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentException("Appointment is already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        log.info("Appointment cancelled with id: {}", id);
    }

    // ── Private helpers ──────────────────────────────────────────────

    /**
     * Validate that the time slot is available for a professional on a given date.
     */
    private void validateTimeSlotAvailable(Long professionalId, LocalDate date,
                                           LocalTime time, int durationMinutes) {
        validateTimeSlotAvailable(professionalId, date, time, durationMinutes, null);
    }

    /**
     * Validate that the time slot is available, optionally excluding a specific appointment
     * (used during updates to avoid self-conflict).
     */
    private void validateTimeSlotAvailable(Long professionalId, LocalDate date,
                                           LocalTime time, int durationMinutes,
                                           Long excludeAppointmentId) {
        if (professionalId == null) {
            return; // Skip conflict check if no professional assigned
        }

        List<Appointment> existingAppointments =
                appointmentRepository.findByProfessionalIdAndAppointmentDate(professionalId, date);

        LocalTime newStart = time;
        LocalTime newEnd = time.plusMinutes(durationMinutes);

        for (Appointment existing : existingAppointments) {
            // Skip cancelled appointments and the appointment being updated
            if (existing.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }
            if (excludeAppointmentId != null && existing.getId().equals(excludeAppointmentId)) {
                continue;
            }

            LocalTime existingStart = existing.getAppointmentTime();
            LocalTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());

            // Check overlap: new appointment overlaps if it starts before existing ends
            // AND ends after existing starts
            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                throw new InvalidAppointmentException(
                        "Time slot is not available for the selected professional");
            }
        }
    }

    /**
     * Map Appointment entity to AppointmentResponse DTO.
     */
    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientDocument(appointment.getPatientDocument())
                .patientName(appointment.getPatientName())
                .patientPhone(appointment.getPatientPhone())
                .professionalId(appointment.getProfessionalId())
                .professionalName(appointment.getProfessionalName())
                .date(appointment.getAppointmentDate())
                .time(appointment.getAppointmentTime())
                .durationMinutes(appointment.getDurationMinutes())
                .reason(appointment.getReason())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
