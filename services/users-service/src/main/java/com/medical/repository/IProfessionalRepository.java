package com.medical.repository;

import com.medical.entities.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Professional entity.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Repository
public interface IProfessionalRepository extends JpaRepository<Professional, Long> {

  Optional<Professional> findByUserId(Long userId);
}
