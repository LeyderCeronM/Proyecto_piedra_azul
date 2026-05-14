package com.medical.factory;

import com.medical.dto.CreateUserRequest;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import org.springframework.stereotype.Component;

/**
 * Strategy for creating users with SCHEDULER role.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Component
public class SchedulerCreationStrategy implements IUserCreationStrategy {

  @Override
  public UserRole getSupportedRole() {
    return UserRole.SCHEDULER;
  }

  @Override
  public User createUser(CreateUserRequest request, String encodedPassword) {
    return User.builder()
        .username(request.getUsername())
        .passwordHash(encodedPassword)
        .email(request.getEmail())
        .role(UserRole.SCHEDULER)
        .active(true)
        .build();
  }
}
