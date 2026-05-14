package com.medical.factory;

import com.medical.dto.CreateUserRequest;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import org.springframework.stereotype.Component;

/**
 * Strategy for creating users with PROFESSIONAL role.
 * Validates that specialty and license number are provided.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Component
public class ProfessionalCreationStrategy implements IUserCreationStrategy {

  @Override
  public UserRole getSupportedRole() {
    return UserRole.PROFESSIONAL;
  }

  @Override
  public User createUser(CreateUserRequest request, String encodedPassword) {
    if (request.getSpecialty() == null || request.getLicenseNumber() == null) {
      throw new IllegalArgumentException("A professional must have specialty and license number");
    }

    return User.builder()
        .username(request.getUsername())
        .passwordHash(encodedPassword)
        .email(request.getEmail())
        .role(UserRole.PROFESSIONAL)
        .active(true)
        .build();
  }
}
