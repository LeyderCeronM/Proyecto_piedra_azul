package com.medical.factory;

import com.medical.dto.CreateUserRequest;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ProfessionalCreationStrategy.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
class ProfessionalCreationStrategyTest {

  private ProfessionalCreationStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new ProfessionalCreationStrategy();
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
}
