package com.medical.factory;

import com.medical.dto.CreateUserRequest;
import com.medical.entities.Patient;
import com.medical.entities.User;
import com.medical.enums.UserRole;
import com.medical.repository.IPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Strategy for creating users with PATIENT role.
 * Validates patient-specific required fields and creates the associated Patient
 * entity.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Component
@RequiredArgsConstructor
public class PatientCreationStrategy implements IUserCreationStrategy {

  private final IPatientRepository patientRepository;

  @Override
  public UserRole getSupportedRole() {
    return UserRole.PATIENT;
  }

  @Override
  public User createUser(CreateUserRequest request, String encodedPassword) {
    if (request.getFirstName() == null) {
      throw new IllegalArgumentException("First name is required for patient");
    }
    if (request.getLastName() == null) {
      throw new IllegalArgumentException("Last name is required for patient");
    }
    if (request.getDocumentType() == null) {
      throw new IllegalArgumentException("Document type is required for patient");
    }
    if (request.getDocumentNumber() == null) {
      throw new IllegalArgumentException("Document number is required for patient");
    }

    return User.builder()
        .username(request.getUsername())
        .passwordHash(encodedPassword)
        .email(request.getEmail())
        .role(UserRole.PATIENT)
        .active(true)
        .build();
  }

  @Override
  public void createAssociatedEntities(User savedUser, CreateUserRequest request) {
    Patient patient = Patient.builder()
        .user(savedUser)
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .documentType(request.getDocumentType())
        .documentNumber(request.getDocumentNumber())
        .phone(request.getPhone())
        .address(request.getAddress())
        .eps(request.getEps())
        .build();
    patientRepository.save(patient);
  }
}
