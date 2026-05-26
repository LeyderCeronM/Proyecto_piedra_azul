package com.medical.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.medical.enums.UserRole;

/**
 * Response DTO for user data.
 * Fields not relevant to the user's role are omitted from JSON.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
  private Long id;
  private String username;
  private String email;
  private UserRole role;
  private Boolean active;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Patient/Professional extended info
  private String firstName;
  private String lastName;
  private String specialty; // Only for PROFESSIONAL
  private String licenseNumber; // Only for PROFESSIONAL

  // Patient-specific fields
  private String documentType;
  private String documentNumber;
  private LocalDate birthDate;
  private String phone;
  private String address;
  private String eps;
}