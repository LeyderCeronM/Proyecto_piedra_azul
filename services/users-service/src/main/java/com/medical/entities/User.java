package com.medical.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.medical.enums.UserRole;

/**
 * Base user entity for authentication and basic information.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_active", columnList = "active"),
    @Index(name = "idx_users_role_active", columnList = "role, active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role;

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}