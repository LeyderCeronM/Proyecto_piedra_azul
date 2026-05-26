package com.medical.factory;

import com.medical.dto.CreateUserRequest;
import com.medical.entities.Professional;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import com.medical.repository.IProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Strategy for creating users with PROFESSIONAL role.
 * Validates that specialty and license number are provided
 * and creates the associated Professional entity.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Component
@RequiredArgsConstructor
public class ProfessionalCreationStrategy implements IUserCreationStrategy {

  private final IProfessionalRepository professionalRepository;

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

  @Override
  public void createAssociatedEntities(User savedUser, CreateUserRequest request) {
    Professional professional = Professional.builder()
        .user(savedUser)
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .specialty(request.getSpecialty())
        .licenseNumber(request.getLicenseNumber())
        .phone(request.getPhone())
        .build();
    professionalRepository.save(professional);
  }
}
