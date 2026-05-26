package com.medical.repository;

import com.medical.entities.User;
import com.medical.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Repository
public interface IUserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  List<User> findAllByUsername(String username);

  List<User> findAllByEmail(String email);

  List<User> findByRole(UserRole role);

  List<User> findByActive(Boolean active);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  long countByRoleAndActive(UserRole role, boolean active);
}