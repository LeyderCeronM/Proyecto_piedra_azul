package com.medical.factory;

import com.medical.dto.CreateUserRequest;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import org.springframework.stereotype.Component;

/**
 * Strategy for creating users with ADMIN role.
 */
@Component
public class AdminCreationStrategy implements IUserCreationStrategy {

  @Override
  public UserRole getSupportedRole() {
    return UserRole.ADMIN;
  }

  @Override
  public User createUser(CreateUserRequest request, String encodedPassword) {
    return User.builder()
        .username(request.getUsername())
        .passwordHash(encodedPassword)
        .email(request.getEmail())
        .role(UserRole.ADMIN)
        .active(true)
        .build();
  }
}
