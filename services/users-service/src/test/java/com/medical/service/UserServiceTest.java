package com.medical.service;

import com.medical.dto.CreateUserRequest;
import com.medical.dto.UserResponse;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import com.medical.factory.UserCreationFactory;
import com.medical.factory.IUserCreationStrategy;
import com.medical.repository.IUserRepository;
import com.medical.repository.IPatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UserService - following TDD (RED → GREEN → REFACTOR)
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

  @Mock
  private IUserRepository userRepository;

  @Mock
  private IPatientRepository patientRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserCreationFactory creationFactory;

  @Mock
  private IUserCreationStrategy mockStrategy;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, patientRepository, passwordEncoder, creationFactory);
  }

  @Test
  void shouldCreateUser_whenValidPatientData() {
    // Given
    CreateUserRequest request = CreateUserRequest.builder()
        .username("juan.perez")
        .password("Password123!") // Valid: 8+ chars, uppercase, number, special
        .email("juan@example.com")
        .role(UserRole.PATIENT)
        .firstName("Juan")
        .lastName("Perez")
        .documentType("CC")
        .documentNumber("12345678")
        .build();

    User expectedUser = User.builder()
        .username("juan.perez")
        .passwordHash("encoded_password")
        .email("juan@example.com")
        .role(UserRole.PATIENT)
        .active(true)
        .build();

    when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    when(userRepository.existsByUsername("juan.perez")).thenReturn(false);
    when(userRepository.existsByEmail("juan@example.com")).thenReturn(false);
    when(creationFactory.getStrategy(UserRole.PATIENT)).thenReturn(mockStrategy);
    when(mockStrategy.createUser(request, "encoded_password")).thenReturn(expectedUser);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(1L);
      return user;
    });

    // When
    UserResponse response = userService.createUser(request);

    // Then
    assertNotNull(response);
    assertEquals("juan.perez", response.getUsername());
    assertEquals("juan@example.com", response.getEmail());
    assertEquals(UserRole.PATIENT, response.getRole());
    assertTrue(response.getActive());
    verify(userRepository, times(1)).save(any(User.class));
    verify(creationFactory).getStrategy(UserRole.PATIENT);
    verify(mockStrategy).createUser(request, "encoded_password");
    verify(mockStrategy).createAssociatedEntities(any(User.class), eq(request));
  }

  @Test
  void shouldThrowException_whenUsernameAlreadyExists() {
    // Given
    CreateUserRequest request = CreateUserRequest.builder()
        .username("existing.user")
        .password("Password123!") // Valid password
        .email("new@example.com")
        .role(UserRole.PATIENT)
        .build();

    when(userRepository.existsByUsername("existing.user")).thenReturn(true);

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldThrowException_whenEmailAlreadyExists() {
    // Given
    CreateUserRequest request = CreateUserRequest.builder()
        .username("new.user")
        .password("Password123!") // Valid password
        .email("existing@example.com")
        .role(UserRole.PATIENT)
        .build();

    when(userRepository.existsByUsername("new.user")).thenReturn(false);
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldThrowException_whenPasswordLessThan8Characters() {
    // Given
    CreateUserRequest request = CreateUserRequest.builder()
        .username("new.user")
        .password("1234567") // 7 characters
        .email("new@example.com")
        .role(UserRole.PATIENT)
        .build();

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
  }

  @Test
  @org.junit.jupiter.api.Disabled("Pending: integration with professionals-service")
  void shouldThrowException_whenProfessionalRoleWithoutProfessionalId() {
    // Given - Creating a user with PROFESSIONAL role but no professional
    // association
    CreateUserRequest request = CreateUserRequest.builder()
        .username("dr.smith")
        .password("Password123!") // Valid password
        .email("dr.smith@example.com")
        .role(UserRole.PROFESSIONAL)
        .firstName("John")
        .lastName("Smith")
        .specialty("Neuralterapia")
        .licenseNumber("MED-12345")
        // No professionalId provided - should fail
        .build();

    when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    when(userRepository.existsByUsername("dr.smith")).thenReturn(false);
    when(userRepository.existsByEmail("dr.smith@example.com")).thenReturn(false);

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
  }
}
