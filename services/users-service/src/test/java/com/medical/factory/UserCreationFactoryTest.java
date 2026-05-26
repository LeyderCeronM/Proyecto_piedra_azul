package com.medical.factory;

import com.medical.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for UserCreationFactory.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@ExtendWith(MockitoExtension.class)
class UserCreationFactoryTest {

  @Mock
  private IUserCreationStrategy patientStrategy;

  @Mock
  private IUserCreationStrategy professionalStrategy;

  @Mock
  private IUserCreationStrategy adminStrategy;

  @Mock
  private IUserCreationStrategy schedulerStrategy;

  private UserCreationFactory factory;

  @BeforeEach
  void setUp() {
    when(patientStrategy.getSupportedRole()).thenReturn(UserRole.PATIENT);
    when(professionalStrategy.getSupportedRole()).thenReturn(UserRole.PROFESSIONAL);
    when(adminStrategy.getSupportedRole()).thenReturn(UserRole.ADMIN);
    when(schedulerStrategy.getSupportedRole()).thenReturn(UserRole.SCHEDULER);

    factory = new UserCreationFactory(
        List.of(patientStrategy, professionalStrategy, adminStrategy, schedulerStrategy));
  }

  @Test
  void shouldReturnPatientStrategy_whenRoleIsPATIENT() {
    IUserCreationStrategy result = factory.getStrategy(UserRole.PATIENT);
    assertEquals(patientStrategy, result);
  }

  @Test
  void shouldReturnProfessionalStrategy_whenRoleIsPROFESSIONAL() {
    IUserCreationStrategy result = factory.getStrategy(UserRole.PROFESSIONAL);
    assertEquals(professionalStrategy, result);
  }

  @Test
  void shouldReturnAdminStrategy_whenRoleIsADMIN() {
    IUserCreationStrategy result = factory.getStrategy(UserRole.ADMIN);
    assertEquals(adminStrategy, result);
  }

  @Test
  void shouldReturnSchedulerStrategy_whenRoleIsSCHEDULER() {
    IUserCreationStrategy result = factory.getStrategy(UserRole.SCHEDULER);
    assertEquals(schedulerStrategy, result);
  }

  @Test
  void shouldThrowException_whenRoleIsNull() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> factory.getStrategy(null));
    assertTrue(exception.getMessage().contains("No strategy found for role: null"));
  }
}
