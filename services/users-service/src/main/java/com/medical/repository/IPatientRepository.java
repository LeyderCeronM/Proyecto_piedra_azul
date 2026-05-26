package com.medical.repository;

import com.medical.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Patient entity.
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@Repository
public interface IPatientRepository extends JpaRepository<Patient, Long> {

  Optional<Patient> findByDocumentNumber(String documentNumber);

  Optional<Patient> findByUserId(Long userId);

  boolean existsByDocumentNumber(String documentNumber);
}