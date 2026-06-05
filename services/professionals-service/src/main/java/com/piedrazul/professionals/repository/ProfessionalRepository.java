package com.piedrazul.professionals.repository;

import com.piedrazul.professionals.entity.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    Optional<Professional> findByDocumentNumber(String documentNumber);

    List<Professional> findBySpecialty(String specialty);

    boolean existsByDocumentNumber(String documentNumber);
}
