package com.medical.factory;

import com.medical.dto.CreateUserRequest;
import com.medical.entities.Patient;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import com.medical.repository.IPatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

/**
 * Tests for PatientCreationStrategy.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@ExtendWith(MockitoExtension.class)
class PatientCreationStrategyTest {

  @Mock
  private IPatientRepository patientRepository;

  @Captor
  private ArgumentCaptor<Patient> patientCaptor;

  private PatientCreationStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new PatientCreationStrategy(patientRepository);
  }

  @Test
  void shouldCreatePatientUser_whenAllRequiredFieldsArePresent() {
    CreateUserRequest request = CreateUserRequest.builder()
        .username("juan.perez")
        .password("encoded_pass")
        .email("juan@example.com")
        .role(UserRole.PATIENT)
        .firstName("Juan")
        .lastName("Perez")
        .documentType("CC")
        .documentNumber("12345678")
        .build();

    User result = strategy.createUser(request, "encoded_pass");

    assertAll(
        () -> assertEquals(UserRole.PATIENT, result.getRole()),
        () -> assertEquals("juan.perez", result.getUsername()),
        () -> assertEquals("juan@example.com", result.getEmail()),
        () -> assertEquals("encoded_pass", result.getPasswordHash()),
        () -> assertTrue(result.getActive())
    );
  }

  @Test
  void shouldThrowException_whenFirstNameIsNull() {
    CreateUserRequest request = CreateUserRequest.builder()
        .username("juan.perez")
        .password("encoded_pass")
        .email("juan@example.com")
        .role(UserRole.PATIENT)
        .lastName("Perez")
        .documentType("CC")
        .documentNumber("12345678")
        .build();

    assertThrows(IllegalArgumentException.class,
        () -> strategy.createUser(request, "encoded_pass"));
  }

  @Test
  void shouldThrowException_whenLastNameIsNull() {
    CreateUserRequest request = CreateUserRequest.builder()
        .username("juan.perez")
        .password("encoded_pass")
        .email("juan@example.com")
        .role(UserRole.PATIENT)
        .firstName("Juan")
        .documentType("CC")
        .documentNumber("12345678")
        .build();

    assertThrows(IllegalArgumentException.class,
        () -> strategy.createUser(request, "encoded_pass"));
  }

  @Test
  void shouldThrowException_whenDocumentTypeIsNull() {
    CreateUserRequest request = CreateUserRequest.builder()
        .username("juan.perez")
        .password("encoded_pass")
        .email("juan@example.com")
        .role(UserRole.PATIENT)
        .firstName("Juan")
        .lastName("Perez")
        .documentNumber("12345678")
        .build();

    assertThrows(IllegalArgumentException.class,
        () -> strategy.createUser(request, "encoded_pass"));
  }

  @Test
  void shouldThrowException_whenDocumentNumberIsNull() {
    CreateUserRequest request = CreateUserRequest.builder()
        .username("juan.perez")
        .password("encoded_pass")
        .email("juan@example.com")
        .role(UserRole.PATIENT)
        .firstName("Juan")
        .lastName("Perez")
        .documentType("CC")
        .build();

    assertThrows(IllegalArgumentException.class,
        () -> strategy.createUser(request, "encoded_pass"));
  }

  @Test
  void shouldGetSupportedRole_whenCalled() {
    assertEquals(UserRole.PATIENT, strategy.getSupportedRole());
  }

  @Test
  void shouldCreateAndSavePatient_whenCreateAssociatedEntities() {
    User savedUser = User.builder()
        .id(1L)
        .username("juan.perez")
        .build();

    CreateUserRequest request = CreateUserRequest.builder()
        .firstName("Juan")
        .lastName("Perez")
        .documentType("CC")
        .documentNumber("12345678")
        .phone("3001234567")
        .address("Calle 123")
        .eps("Nueva EPS")
        .build();

    strategy.createAssociatedEntities(savedUser, request);

    verify(patientRepository).save(patientCaptor.capture());
    Patient savedPatient = patientCaptor.getValue();

    assertAll(
        () -> assertSame(savedUser, savedPatient.getUser()),
        () -> assertEquals("Juan", savedPatient.getFirstName()),
        () -> assertEquals("Perez", savedPatient.getLastName()),
        () -> assertEquals("CC", savedPatient.getDocumentType()),
        () -> assertEquals("12345678", savedPatient.getDocumentNumber()),
        () -> assertEquals("3001234567", savedPatient.getPhone()),
        () -> assertEquals("Calle 123", savedPatient.getAddress()),
        () -> assertEquals("Nueva EPS", savedPatient.getEps())
    );
  }
}
