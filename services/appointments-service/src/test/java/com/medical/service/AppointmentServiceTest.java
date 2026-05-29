package com.medical.service;

import com.medical.dto.*;
import com.medical.entity.Appointment;
import com.medical.entity.AppointmentStatus;
import com.medical.exception.AppointmentNotFoundException;
import com.medical.exception.InvalidAppointmentException;
import com.medical.facade.PatientValidationFacade;
import com.medical.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppointmentServiceImpl.
 * Tests align with the cases documented in integration-tests.md.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientValidationFacade patientValidationFacade;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private CreateAppointmentRequest validRequest;
    private Appointment scheduledAppointment;
    private final String futureDate = LocalDate.now().plusDays(7).toString();

    @BeforeEach
    void setUp() {
        validRequest = CreateAppointmentRequest.builder()
                .patientDocument("12345678")
                .patientName("Juan Pérez")
                .patientPhone("3001234567")
                .professionalId(1L)
                .professionalName("Dr. Smith")
                .date(futureDate)
                .time("10:00")
                .durationMinutes(30)
                .reason("Chequeo General")
                .build();

        scheduledAppointment = Appointment.builder()
                .id(1L)
                .patientDocument("12345678")
                .patientName("Juan Pérez")
                .patientPhone("3001234567")
                .professionalId(1L)
                .professionalName("Dr. Smith")
                .appointmentDate(LocalDate.now().plusDays(7))
                .appointmentTime(LocalTime.of(10, 0))
                .durationMinutes(30)
                .reason("Chequeo General")
                .status(AppointmentStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("shouldCreateAppointment_whenValidData")
    void shouldCreateAppointment_whenValidData() {
        // Arrange
        PatientValidationResponse validResponse = PatientValidationResponse.builder()
                .correlationId("test-id")
                .valid(true)
                .patientName("Juan Pérez")
                .patientDocument("12345678")
                .build();

        when(patientValidationFacade.validatePatient("12345678")).thenReturn(validResponse);
        when(appointmentRepository.findByProfessionalIdAndAppointmentDate(anyLong(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(scheduledAppointment);

        // Act
        AppointmentResponse response = appointmentService.createAppointment(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("12345678", response.getPatientDocument());
        assertEquals("Juan Pérez", response.getPatientName());
        assertEquals(AppointmentStatus.SCHEDULED, response.getStatus());
        verify(patientValidationFacade).validatePatient("12345678");
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("shouldThrowException_whenPatientNotFound")
    void shouldThrowException_whenPatientNotFound() {
        // Arrange
        when(patientValidationFacade.validatePatient("99999999"))
                .thenThrow(new InvalidAppointmentException("Patient not found with document: 99999999"));

        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .patientDocument("99999999")
                .date(futureDate)
                .time("10:00")
                .build();

        // Act & Assert
        InvalidAppointmentException exception = assertThrows(
                InvalidAppointmentException.class,
                () -> appointmentService.createAppointment(request)
        );
        assertTrue(exception.getMessage().contains("Patient not found"));
    }

    @Test
    @DisplayName("shouldUsePatientNameFromValidationResponse_whenNotProvided")
    void shouldUsePatientNameFromValidationResponse_whenNotProvided() {
        // Arrange
        PatientValidationResponse validResponse = PatientValidationResponse.builder()
                .correlationId("test-id")
                .valid(true)
                .patientName("From Validation")
                .patientDocument("12345678")
                .build();

        CreateAppointmentRequest requestNoName = CreateAppointmentRequest.builder()
                .patientDocument("12345678")
                .date(futureDate)
                .time("10:00")
                .build();

        Appointment savedAppointment = Appointment.builder()
                .id(2L)
                .patientDocument("12345678")
                .patientName("From Validation")
                .appointmentDate(LocalDate.now().plusDays(7))
                .appointmentTime(LocalTime.of(10, 0))
                .durationMinutes(30)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        when(patientValidationFacade.validatePatient("12345678")).thenReturn(validResponse);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        // Act
        AppointmentResponse response = appointmentService.createAppointment(requestNoName);

        // Assert
        assertEquals("From Validation", response.getPatientName());
    }

    @Test
    @DisplayName("shouldThrowException_whenTimeSlotNotAvailable")
    void shouldThrowException_whenTimeSlotNotAvailable() {
        // Arrange
        PatientValidationResponse validResponse = PatientValidationResponse.builder()
                .correlationId("test-id")
                .valid(true)
                .patientName("Juan Pérez")
                .build();

        Appointment existingAppointment = Appointment.builder()
                .id(99L)
                .professionalId(1L)
                .appointmentDate(LocalDate.now().plusDays(7))
                .appointmentTime(LocalTime.of(10, 0))
                .durationMinutes(30)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        when(patientValidationFacade.validatePatient("12345678")).thenReturn(validResponse);
        when(appointmentRepository.findByProfessionalIdAndAppointmentDate(1L, LocalDate.now().plusDays(7)))
                .thenReturn(List.of(existingAppointment));

        // Act & Assert
        InvalidAppointmentException exception = assertThrows(
                InvalidAppointmentException.class,
                () -> appointmentService.createAppointment(validRequest)
        );
        assertTrue(exception.getMessage().contains("Time slot is not available"));
    }

    @Test
    @DisplayName("shouldThrowException_whenDateInPast")
    void shouldThrowException_whenDateInPast() {
        // Arrange
        CreateAppointmentRequest pastRequest = CreateAppointmentRequest.builder()
                .patientDocument("12345678")
                .date(LocalDate.now().minusDays(1).toString())
                .time("10:00")
                .build();

        // Act & Assert
        InvalidAppointmentException exception = assertThrows(
                InvalidAppointmentException.class,
                () -> appointmentService.createAppointment(pastRequest)
        );
        assertTrue(exception.getMessage().contains("past"));
    }

    @Test
    @DisplayName("shouldCancelAppointment_whenExistsAndScheduled")
    void shouldCancelAppointment_whenExistsAndScheduled() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(scheduledAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(scheduledAppointment);

        // Act
        appointmentService.cancelAppointment(1L);

        // Assert
        assertEquals(AppointmentStatus.CANCELLED, scheduledAppointment.getStatus());
        verify(appointmentRepository).save(scheduledAppointment);
    }

    @Test
    @DisplayName("shouldThrowException_whenCancelAlreadyCancelled")
    void shouldThrowException_whenCancelAlreadyCancelled() {
        // Arrange
        scheduledAppointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(scheduledAppointment));

        // Act & Assert
        InvalidAppointmentException exception = assertThrows(
                InvalidAppointmentException.class,
                () -> appointmentService.cancelAppointment(1L)
        );
        assertTrue(exception.getMessage().contains("already cancelled"));
    }

    @Test
    @DisplayName("shouldThrowException_whenAppointmentNotFound")
    void shouldThrowException_whenAppointmentNotFound() {
        // Arrange
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppointmentNotFoundException.class,
                () -> appointmentService.getAppointmentById(999L));
    }

    @Test
    @DisplayName("shouldReturnAllAppointments")
    void shouldReturnAllAppointments() {
        // Arrange
        when(appointmentRepository.findAll()).thenReturn(List.of(scheduledAppointment));

        // Act
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();

        // Assert
        assertEquals(1, appointments.size());
        assertEquals("12345678", appointments.get(0).getPatientDocument());
    }
}
