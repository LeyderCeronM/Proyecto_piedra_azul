package com.medical.factory;

import com.medical.dto.CreateUserRequest;
import com.medical.entities.User;
import com.medical.enums.UserRole;

/**
 * Strategy interface for creating users of different roles.
 * Each role has its own strategy that knows how to build the User entity
 * and optionally create associated entities (e.g., Patient record).
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
public interface IUserCreationStrategy {

  /**
   * Returns the role this strategy supports.
   */
  UserRole getSupportedRole();

  /**
   * Build the User entity for this role.
   * The password is already encoded by the service.
   * Throw IllegalArgumentException for role-specific validation failures.
   */
  User createUser(CreateUserRequest request, String encodedPassword);

  /**
   * Called after User is saved (has an ID).
   * Override for roles that need to create associated entities (e.g., Patient).
   */
  default void createAssociatedEntities(User savedUser, CreateUserRequest request) {
    // no-op by default
  }
}
