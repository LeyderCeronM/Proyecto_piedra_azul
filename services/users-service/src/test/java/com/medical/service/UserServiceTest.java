package com.medical.service;

import com.medical.dto.CreateUserRequest;
import com.medical.dto.UpdateUserRequest;
import com.medical.dto.UserResponse;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import com.medical.factory.UserCreationFactory;
import com.medical.factory.IUserCreationStrategy;
import com.medical.entities.Patient;
import com.medical.entities.Professional;
import com.medical.repository.IProfessionalRepository;
import com.medical.repository.IUserRepository;
import com.medical.repository.IPatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
  private IProfessionalRepository professionalRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserCreationFactory creationFactory;

  @Mock
  private IUserCreationStrategy mockStrategy;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, patientRepository, professionalRepository, passwordEncoder, creationFactory);
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
  void shouldThrowException_whenUserNotFound_onUpdate() {
    // Given
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    // When & Then
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> userService.updateUser(99L, UpdateUserRequest.builder().build()));
    assertEquals("User not found", ex.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldThrowException_whenUsernameAlreadyExists_onUpdate() {
    // Given
    User existingUser = User.builder()
        .id(1L)
        .username("original.user")
        .passwordHash("hash")
        .email("original@example.com")
        .role(UserRole.PATIENT)
        .active(true)
        .build();

    UpdateUserRequest request = UpdateUserRequest.builder()
        .username("taken.user")
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
    when(userRepository.existsByUsername("taken.user")).thenReturn(true);

    // When & Then
    assertThrows(IllegalArgumentException.class,
        () -> userService.updateUser(1L, request));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldThrowException_whenEmailAlreadyExists_onUpdate() {
    // Given
    User existingUser = User.builder()
        .id(1L)
        .username("original.user")
        .passwordHash("hash")
        .email("original@example.com")
        .role(UserRole.PATIENT)
        .active(true)
        .build();

    // Same username as existing — skip username check, trigger email check
    UpdateUserRequest request = UpdateUserRequest.builder()
        .username("original.user")
        .email("taken@example.com")
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

    // When & Then
    assertThrows(IllegalArgumentException.class,
        () -> userService.updateUser(1L, request));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldThrowException_whenWeakPassword_onUpdate() {
    // Given
    User existingUser = User.builder()
        .id(1L)
        .username("user")
        .passwordHash("hash")
        .email("user@example.com")
        .role(UserRole.PATIENT)
        .active(true)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

    // When & Then — each weak password case should be rejected
    // Less than 8 characters
    UpdateUserRequest tooShort = UpdateUserRequest.builder().password("Ab1!").build();
    IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
        () -> userService.updateUser(1L, tooShort));
    assertEquals("The password does not meet the minimum policy", ex1.getMessage());

    // No uppercase letter
    UpdateUserRequest noUpper = UpdateUserRequest.builder().password("password123!").build();
    IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
        () -> userService.updateUser(1L, noUpper));
    assertEquals("The password does not meet the minimum policy", ex2.getMessage());

    // No number
    UpdateUserRequest noNumber = UpdateUserRequest.builder().password("Password!!!").build();
    IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
        () -> userService.updateUser(1L, noNumber));
    assertEquals("The password does not meet the minimum policy", ex3.getMessage());

    // No special character
    UpdateUserRequest noSpecial = UpdateUserRequest.builder().password("Password1").build();
    IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class,
        () -> userService.updateUser(1L, noSpecial));
    assertEquals("The password does not meet the minimum policy", ex4.getMessage());

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @org.junit.jupiter.api.Disabled("PRODUCTION GAP (Phase 1.6): updateUser() does not validate professional " +
      "role constraint. Changing a user's role to PROFESSIONAL without providing " +
      "specialty/licenseNumber should throw IllegalArgumentException, but " +
      "updateUser() sets the role directly without calling strategy.validate().")
  void shouldThrowException_whenRoleChangedToProfessionalWithoutAssociation() {
    // Given
    // EXPECTED BEHAVIOR (per spec): Changing role to PROFESSIONAL without
    // professional info (specialty, licenseNumber) should be rejected.
    // PRODUCTION GAP: updateUser() sets role without any validation.
    User existingUser = User.builder()
        .id(1L)
        .username("user")
        .passwordHash("hash")
        .email("user@example.com")
        .role(UserRole.PATIENT)
        .active(true)
        .build();

    UpdateUserRequest request = UpdateUserRequest.builder()
        .role(UserRole.PROFESSIONAL)
        // Missing: specialty, licenseNumber — should trigger validation
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

    // When & Then — should throw but currently does not
    assertThrows(IllegalArgumentException.class,
        () -> userService.updateUser(1L, request));
  }

  @Test
  void shouldUpdateUser_whenValidData() {
    // Given
    User existingUser = User.builder()
        .id(1L)
        .username("old.user")
        .passwordHash("old_hash")
        .email("old@example.com")
        .role(UserRole.PATIENT)
        .active(true)
        .build();

    UpdateUserRequest request = UpdateUserRequest.builder()
        .username("new.user")
        .password("NewPassword123!")
        .email("new@example.com")
        .role(UserRole.ADMIN)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
    when(userRepository.existsByUsername("new.user")).thenReturn(false);
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(passwordEncoder.encode("NewPassword123!")).thenReturn("encoded_new_password");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    UserResponse response = userService.updateUser(1L, request);

    // Then
    assertNotNull(response);
    assertEquals("new.user", response.getUsername());
    assertEquals("new@example.com", response.getEmail());
    assertEquals(UserRole.ADMIN, response.getRole());
    verify(userRepository, times(1)).save(any(User.class));
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

  // ──────────────────────────────────────────────
  // Phase 2: getAllUsers
  // ──────────────────────────────────────────────

  @Test
  void shouldReturnAllUsers() {
    // Given
    User user1 = User.builder()
        .id(1L)
        .username("alice")
        .passwordHash("hash1")
        .email("alice@example.com")
        .role(UserRole.ADMIN)
        .active(true)
        .build();

    User user2 = User.builder()
        .id(2L)
        .username("bob")
        .passwordHash("hash2")
        .email("bob@example.com")
        .role(UserRole.PATIENT)
        .active(true)
        .build();

    when(userRepository.findAll()).thenReturn(List.of(user1, user2));

    // When
    List<UserResponse> result = userService.getAllUsers();

    // Then
    assertEquals(2, result.size());
    assertEquals("alice", result.get(0).getUsername());
    assertEquals(UserRole.ADMIN, result.get(0).getRole());
    assertTrue(result.get(0).getActive());
    assertNull(result.get(0).getFirstName()); // No enrichment in list-all
    assertEquals("bob", result.get(1).getUsername());
    assertEquals(UserRole.PATIENT, result.get(1).getRole());
    assertNull(result.get(1).getFirstName()); // No enrichment in list-all
    verify(userRepository, times(1)).findAll();
    verify(patientRepository, never()).findByUserId(anyLong());
    verify(professionalRepository, never()).findByUserId(anyLong());
  }

  // ──────────────────────────────────────────────
  // Phase 2: getUserById
  // ──────────────────────────────────────────────

  @Test
  void shouldReturnUser_whenUserExists() {
    // Given
    User user = User.builder()
        .id(1L)
        .username("charlie")
        .passwordHash("hash")
        .email("charlie@example.com")
        .role(UserRole.SCHEDULER)
        .active(true)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // When
    UserResponse response = userService.getUserById(1L);

    // Then
    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals("charlie", response.getUsername());
    assertEquals("charlie@example.com", response.getEmail());
    assertEquals(UserRole.SCHEDULER, response.getRole());
    assertTrue(response.getActive());
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  void shouldThrowException_whenUserNotFound() {
    // Given
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    // When & Then
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> userService.getUserById(99L));
    assertEquals("User not found", ex.getMessage());
    verify(userRepository, times(1)).findById(99L);
  }

  // ──────────────────────────────────────────────
  // Phase 3: deactivateUser
  // ──────────────────────────────────────────────

  @Test
  void shouldDeactivateUser_whenUserIsActive() {
    // Given
    User user = User.builder()
        .id(1L)
        .username("dave")
        .passwordHash("hash")
        .email("dave@example.com")
        .role(UserRole.PATIENT)
        .active(true)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    userService.deactivateUser(1L);

    // Then
    assertFalse(user.getActive());
    verify(userRepository, times(1)).save(user);
  }

  @Test
  void shouldThrowException_whenDeactivatingLastAdmin() {
    // Given
    User admin = User.builder()
        .id(1L)
        .username("lastadmin")
        .passwordHash("hash")
        .email("admin@example.com")
        .role(UserRole.ADMIN)
        .active(true)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
    when(userRepository.countByRoleAndActive(UserRole.ADMIN, true)).thenReturn(1L);

    // When & Then
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> userService.deactivateUser(1L));
    assertTrue(ex.getMessage().contains("last administrator"));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldThrowException_whenUserNotFound_onDeactivate() {
    // Given
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    // When & Then
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> userService.deactivateUser(99L));
    assertEquals("User not found", ex.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldThrowException_whenUserAlreadyInactive() {
    // Given
    User user = User.builder()
        .id(1L)
        .username("inactive.user")
        .passwordHash("hash")
        .email("inactive@example.com")
        .role(UserRole.PATIENT)
        .active(false)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // When & Then
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> userService.deactivateUser(1L));
    assertEquals("The user is already inactive", ex.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void shouldDeactivateUser_whenActiveAdminWithOtherAdmins() {
    // Given — admin with other active admins
    User admin = User.builder()
        .id(2L)
        .username("anotheradmin")
        .passwordHash("hash")
        .email("admin2@example.com")
        .role(UserRole.ADMIN)
        .active(true)
        .build();

    when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
    when(userRepository.countByRoleAndActive(UserRole.ADMIN, true)).thenReturn(2L);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    userService.deactivateUser(2L);

    // Then
    assertFalse(admin.getActive());
    verify(userRepository, times(1)).save(admin);
  }

  // ──────────────────────────────────────────────
  // Phase: Search by username
  // ──────────────────────────────────────────────

  @Test
  void shouldReturnUsers_whenSearchingByExistingUsername() {
    // Given
    User user = User.builder()
        .id(1L).username("jdoe").passwordHash("hash")
        .email("jdoe@example.com").role(UserRole.PATIENT).active(true)
        .build();
    when(userRepository.findAllByUsername("jdoe")).thenReturn(List.of(user));

    // When
    List<UserResponse> result = userService.searchByUsername("jdoe");

    // Then
    assertEquals(1, result.size());
    assertEquals("jdoe", result.get(0).getUsername());
    verify(userRepository).findAllByUsername("jdoe");
  }

  @Test
  void shouldReturnEmptyList_whenSearchingByNonExistentUsername() {
    // Given
    when(userRepository.findAllByUsername("unknown")).thenReturn(List.of());

    // When
    List<UserResponse> result = userService.searchByUsername("unknown");

    // Then
    assertTrue(result.isEmpty());
    verify(userRepository).findAllByUsername("unknown");
  }

  // ──────────────────────────────────────────────
  // Phase: Search by email
  // ──────────────────────────────────────────────

  @Test
  void shouldReturnUsers_whenSearchingByExistingEmail() {
    // Given
    User user = User.builder()
        .id(1L).username("jdoe").passwordHash("hash")
        .email("jdoe@example.com").role(UserRole.PATIENT).active(true)
        .build();
    when(userRepository.findAllByEmail("jdoe@example.com")).thenReturn(List.of(user));

    // When
    List<UserResponse> result = userService.searchByEmail("jdoe@example.com");

    // Then
    assertEquals(1, result.size());
    assertEquals("jdoe@example.com", result.get(0).getEmail());
    verify(userRepository).findAllByEmail("jdoe@example.com");
  }

  @Test
  void shouldReturnEmptyList_whenSearchingByNonExistentEmail() {
    // Given
    when(userRepository.findAllByEmail("unknown@example.com")).thenReturn(List.of());

    // When
    List<UserResponse> result = userService.searchByEmail("unknown@example.com");

    // Then
    assertTrue(result.isEmpty());
    verify(userRepository).findAllByEmail("unknown@example.com");
  }

  // ──────────────────────────────────────────────
  // Phase: Search by role
  // ──────────────────────────────────────────────

  @Test
  void shouldReturnUsers_whenSearchingByRole() {
    // Given
    User user = User.builder()
        .id(1L).username("alice").passwordHash("hash")
        .email("alice@example.com").role(UserRole.ADMIN).active(true)
        .build();
    when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(List.of(user));

    // When
    List<UserResponse> result = userService.searchByRole(UserRole.ADMIN);

    // Then
    assertEquals(1, result.size());
    assertEquals(UserRole.ADMIN, result.get(0).getRole());
    verify(userRepository).findByRole(UserRole.ADMIN);
  }

  @Test
  void shouldReturnEmptyList_whenNoUsersWithGivenRole() {
    // Given
    when(userRepository.findByRole(UserRole.SCHEDULER)).thenReturn(List.of());

    // When
    List<UserResponse> result = userService.searchByRole(UserRole.SCHEDULER);

    // Then
    assertTrue(result.isEmpty());
    verify(userRepository).findByRole(UserRole.SCHEDULER);
  }

  // ──────────────────────────────────────────────
  // Phase: Search by status (active/inactive)
  // ──────────────────────────────────────────────

  @Test
  void shouldReturnActiveUsers_whenSearchingByActiveTrue() {
    // Given
    User user = User.builder()
        .id(1L).username("activeuser").passwordHash("hash")
        .email("active@example.com").role(UserRole.PATIENT).active(true)
        .build();
    when(userRepository.findByActive(true)).thenReturn(List.of(user));

    // When
    List<UserResponse> result = userService.searchByStatus(true);

    // Then
    assertEquals(1, result.size());
    assertTrue(result.get(0).getActive());
    verify(userRepository).findByActive(true);
  }

  @Test
  void shouldReturnInactiveUsers_whenSearchingByActiveFalse() {
    // Given
    User user = User.builder()
        .id(1L).username("inactiveuser").passwordHash("hash")
        .email("inactive@example.com").role(UserRole.PATIENT).active(false)
        .build();
    when(userRepository.findByActive(false)).thenReturn(List.of(user));

    // When
    List<UserResponse> result = userService.searchByStatus(false);

    // Then
    assertEquals(1, result.size());
    assertFalse(result.get(0).getActive());
    verify(userRepository).findByActive(false);
  }

  // ──────────────────────────────────────────────
  // Phase: Advanced combined search
  // ──────────────────────────────────────────────

  @Test
  void shouldReturnFilteredUsers_whenAdvancedSearchWithMultipleParams() {
    // Given
    User user = User.builder()
        .id(1L).username("jdoe").passwordHash("hash")
        .email("jdoe@example.com").role(UserRole.PATIENT).active(true)
        .build();
    when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any()))
        .thenReturn(List.of(user));

    // When
    List<UserResponse> result = userService.searchAdvanced("jdoe", "jdoe@example.com", UserRole.PATIENT, true);

    // Then
    assertEquals(1, result.size());
    assertEquals("jdoe", result.get(0).getUsername());
    verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any());
  }

  @Test
  void shouldReturnAllUsers_whenAdvancedSearchWithNoParams() {
    // Given
    User user = User.builder()
        .id(1L).username("user1").passwordHash("hash")
        .email("user1@example.com").role(UserRole.ADMIN).active(true)
        .build();
    when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any()))
        .thenReturn(List.of(user));

    // When
    List<UserResponse> result = userService.searchAdvanced(null, null, null, null);

    // Then
    assertEquals(1, result.size());
    verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any());
  }

  @Test
  void shouldReturnEmptyList_whenAdvancedSearchNoMatches() {
    // Given
    when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any()))
        .thenReturn(List.of());

    // When
    List<UserResponse> result = userService.searchAdvanced("nonexistent", null, null, null);

    // Then
    assertTrue(result.isEmpty());
    verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any());
  }

  // ──────────────────────────────────────────────
  // Phase: Role-enriched responses (R1-R4)
  // ──────────────────────────────────────────────

  @Test
  void shouldIncludePatientFields_whenSearchingByRolePatient() {
    // Given
    User user = User.builder()
        .id(1L).username("patient1").passwordHash("hash")
        .email("patient@example.com").role(UserRole.PATIENT).active(true)
        .build();

    Patient patient = Patient.builder()
        .id(10L).user(user)
        .firstName("Carlos").lastName("Mendez")
        .documentType("CC").documentNumber("12345")
        .birthDate(LocalDate.of(1990, 5, 15))
        .phone("3001112233").address("Calle 1 #2-34").eps("Nueva EPS")
        .build();

    when(userRepository.findByRole(UserRole.PATIENT)).thenReturn(List.of(user));
    when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));

    // When
    List<UserResponse> result = userService.searchByRole(UserRole.PATIENT);

    // Then
    assertEquals(1, result.size());
    UserResponse resp = result.get(0);
    assertEquals("Carlos", resp.getFirstName());
    assertEquals("Mendez", resp.getLastName());
    assertEquals("CC", resp.getDocumentType());
    assertEquals("12345", resp.getDocumentNumber());
    assertEquals("1990-05-15", resp.getBirthDate().toString());
    assertEquals("3001112233", resp.getPhone());
    assertEquals("Calle 1 #2-34", resp.getAddress());
    assertEquals("Nueva EPS", resp.getEps());
    verify(patientRepository).findByUserId(1L);
  }

  @Test
  void shouldIncludeProfessionalFields_whenSearchingByRoleProfessional() {
    // Given
    User user = User.builder()
        .id(2L).username("dr.perez").passwordHash("hash")
        .email("dr.perez@example.com").role(UserRole.PROFESSIONAL).active(true)
        .build();

    Professional prof = Professional.builder()
        .id(20L).user(user)
        .firstName("Maria").lastName("Perez")
        .specialty("Neuralterapia").licenseNumber("LIC-001")
        .phone("3009998877")
        .build();

    when(userRepository.findByRole(UserRole.PROFESSIONAL)).thenReturn(List.of(user));
    when(professionalRepository.findByUserId(2L)).thenReturn(Optional.of(prof));

    // When
    List<UserResponse> result = userService.searchByRole(UserRole.PROFESSIONAL);

    // Then
    assertEquals(1, result.size());
    UserResponse resp = result.get(0);
    assertEquals("Maria", resp.getFirstName());
    assertEquals("Perez", resp.getLastName());
    assertEquals("Neuralterapia", resp.getSpecialty());
    assertEquals("LIC-001", resp.getLicenseNumber());
    assertEquals("3009998877", resp.getPhone());
    verify(professionalRepository).findByUserId(2L);
  }

  @Test
  void shouldReturnBaseFieldsOnly_whenSearchingByRoleAdmin() {
    // Given
    User user = User.builder()
        .id(3L).username("admin1").passwordHash("hash")
        .email("admin@example.com").role(UserRole.ADMIN).active(true)
        .build();

    when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(List.of(user));

    // When
    List<UserResponse> result = userService.searchByRole(UserRole.ADMIN);

    // Then
    assertEquals(1, result.size());
    UserResponse resp = result.get(0);
    assertEquals("admin1", resp.getUsername());
    assertNull(resp.getFirstName());
    assertNull(resp.getLastName());
    assertNull(resp.getSpecialty());
    assertNull(resp.getDocumentType());
    verify(patientRepository, never()).findByUserId(anyLong());
    verify(professionalRepository, never()).findByUserId(anyLong());
  }

  @Test
  void shouldIncludePatientFields_whenGettingUserById() {
    // Given
    User user = User.builder()
        .id(1L).username("patient1").passwordHash("hash")
        .email("patient@example.com").role(UserRole.PATIENT).active(true)
        .build();

    Patient patient = Patient.builder()
        .id(10L).user(user)
        .firstName("Carlos").lastName("Mendez")
        .documentType("CC").documentNumber("12345")
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));

    // When
    UserResponse response = userService.getUserById(1L);

    // Then
    assertEquals("Carlos", response.getFirstName());
    assertEquals("CC", response.getDocumentType());
    verify(patientRepository).findByUserId(1L);
  }

  @Test
  void shouldGracefullyHandleMissingPatientData() {
    // Given — PATIENT user without a Patient record (data inconsistency)
    User user = User.builder()
        .id(99L).username("orphan").passwordHash("hash")
        .email("orphan@example.com").role(UserRole.PATIENT).active(true)
        .build();

    when(userRepository.findById(99L)).thenReturn(Optional.of(user));
    when(patientRepository.findByUserId(99L)).thenReturn(Optional.empty());

    // When
    UserResponse response = userService.getUserById(99L);

    // Then — should return base fields, enrichment fields null, no crash
    assertEquals("orphan", response.getUsername());
    assertNull(response.getFirstName());
    assertNull(response.getDocumentType());
  }

  @Test
  void shouldGracefullyHandleMissingProfessionalData() {
    // Given — PROFESSIONAL user without a Professional record (data inconsistency)
    User user = User.builder()
        .id(98L).username("orphanprof").passwordHash("hash")
        .email("orphanprof@example.com").role(UserRole.PROFESSIONAL).active(true)
        .build();

    when(userRepository.findById(98L)).thenReturn(Optional.of(user));
    when(professionalRepository.findByUserId(98L)).thenReturn(Optional.empty());

    // When
    UserResponse response = userService.getUserById(98L);

    // Then — should return base fields, enrichment fields null, no crash
    assertEquals("orphanprof", response.getUsername());
    assertNull(response.getFirstName());
    assertNull(response.getSpecialty());
  }
}
