package com.medical.factory;

import com.medical.dto.CreateUserRequest;
import com.medical.entities.Professional;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import com.medical.repository.IProfessionalRepository;
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
 * Tests for ProfessionalCreationStrategy.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@ExtendWith(MockitoExtension.class)
class ProfessionalCreationStrategyTest {

  @Mock
  private IProfessionalRepository professionalRepository;

  @Captor
  private ArgumentCaptor<Professional> professionalCaptor;

  private ProfessionalCreationStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new ProfessionalCreationStrategy(professionalRepository);
  }

  @Test
  void shouldCreateProfessionalUser_whenAllRequiredFieldsArePresent() {
    CreateUserRequest request = CreateUserRequest.builder()
        .username("dr.smith")
        .password("encoded_pass")
        .email("dr.smith@example.com")
        .role(UserRole.PROFESSIONAL)
        .specialty("Neuralterapia")
        .licenseNumber("MED-12345")
        .build();

    User result = strategy.createUser(request, "encoded_pass");

    assertAll(
        () -> assertEquals(UserRole.PROFESSIONAL, result.getRole()),
        () -> assertEquals("dr.smith", result.getUsername()),
        () -> assertEquals("dr.smith@example.com", result.getEmail()),
        () -> assertEquals("encoded_pass", result.getPasswordHash()),
        () -> assertTrue(result.getActive())
    );
  }

  @Test
  void shouldThrowException_whenSpecialtyIsNull() {
    CreateUserRequest request = CreateUserRequest.builder()
        .username("dr.smith")
        .password("encoded_pass")
        .email("dr.smith@example.com")
        .role(UserRole.PROFESSIONAL)
        .licenseNumber("MED-12345")
        .build();

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> strategy.createUser(request, "encoded_pass"));
    assertEquals("A professional must have specialty and license number", exception.getMessage());
  }

  @Test
  void shouldThrowException_whenLicenseNumberIsNull() {
    CreateUserRequest request = CreateUserRequest.builder()
        .username("dr.smith")
        .password("encoded_pass")
        .email("dr.smith@example.com")
        .role(UserRole.PROFESSIONAL)
        .specialty("Neuralterapia")
        .build();

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> strategy.createUser(request, "encoded_pass"));
    assertEquals("A professional must have specialty and license number", exception.getMessage());
  }

  @Test
  void shouldGetSupportedRole_whenCalled() {
    assertEquals(UserRole.PROFESSIONAL, strategy.getSupportedRole());
  }

  @Test
  void shouldCreateProfessionalEntity_whenCreateAssociatedEntities() {
    // Given
    User savedUser = User.builder().id(1L).username("dr.smith").build();

    CreateUserRequest request = CreateUserRequest.builder()
        .firstName("John")
        .lastName("Smith")
        .specialty("Neuralterapia")
        .licenseNumber("MED-12345")
        .phone("3001112233")
        .build();

    // When
    strategy.createAssociatedEntities(savedUser, request);

    // Then
    verify(professionalRepository).save(professionalCaptor.capture());
    Professional saved = professionalCaptor.getValue();
    assertAll(
        () -> assertEquals(savedUser, saved.getUser()),
        () -> assertEquals("John", saved.getFirstName()),
        () -> assertEquals("Smith", saved.getLastName()),
        () -> assertEquals("Neuralterapia", saved.getSpecialty()),
        () -> assertEquals("MED-12345", saved.getLicenseNumber()),
        () -> assertEquals("3001112233", saved.getPhone())
    );
  }
}
