package com.piedrazul.professionals.service;

import com.piedrazul.professionals.dto.CreateProfessionalRequest;
import com.piedrazul.professionals.dto.UpdateProfessionalRequest;
import com.piedrazul.professionals.dto.ProfessionalResponse;
import com.piedrazul.professionals.entity.Professional;
import com.piedrazul.professionals.exception.ResourceNotFoundException;
import com.piedrazul.professionals.repository.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionalServiceImpl implements ProfessionalService {

    private final ProfessionalRepository professionalRepository;

    @Override
    @Transactional
    public ProfessionalResponse create(CreateProfessionalRequest request) {
        // Prevent duplicates by document number
        if (professionalRepository.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new IllegalArgumentException("Professional with document already exists");
        }

        Professional p = Professional.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .documentNumber(request.getDocumentNumber())
                .specialty(request.getSpecialty())
                .phone(request.getPhone())
                .email(request.getEmail())
                .active(true)
                .build();

        Professional saved = professionalRepository.save(p);
        log.info("Professional created with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionalResponse> getAll() {
        return professionalRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalResponse getById(Long id) {
        Professional p = professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + id));
        return mapToResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalResponse getByDocument(String documentNumber) {
        Professional p = professionalRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with document: " + documentNumber));
        return mapToResponse(p);
    }

    @Override
    @Transactional
    public ProfessionalResponse update(Long id, UpdateProfessionalRequest request) {
        Professional p = professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + id));

        if (request.getFirstName() != null) p.setFirstName(request.getFirstName());
        if (request.getLastName() != null) p.setLastName(request.getLastName());
        if (request.getSpecialty() != null) p.setSpecialty(request.getSpecialty());
        if (request.getPhone() != null) p.setPhone(request.getPhone());
        if (request.getEmail() != null) p.setEmail(request.getEmail());
        if (request.getActive() != null) p.setActive(request.getActive());

        Professional updated = professionalRepository.save(p);
        log.info("Professional updated with id: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Professional p = professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found with id: " + id));
        professionalRepository.delete(p);
        log.info("Professional deleted with id: {}", id);
    }

    private ProfessionalResponse mapToResponse(Professional p) {
        return ProfessionalResponse.builder()
                .id(p.getId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .documentNumber(p.getDocumentNumber())
                .specialty(p.getSpecialty())
                .phone(p.getPhone())
                .email(p.getEmail())
                .active(p.getActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
