package com.piedrazul.professionals.service;

import com.piedrazul.professionals.dto.CreateProfessionalRequest;
import com.piedrazul.professionals.dto.ProfessionalResponse;
import com.piedrazul.professionals.dto.UpdateProfessionalRequest;
import com.piedrazul.professionals.entity.Professional;
import com.piedrazul.professionals.repository.ProfessionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceImplTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @InjectMocks
    private ProfessionalServiceImpl professionalService;

    @Test
    void createProfessional_success() {
        CreateProfessionalRequest req = CreateProfessionalRequest.builder()
                .firstName("Ana")
                .lastName("Perez")
                .documentNumber("DOC123")
                .specialty("Cardiology")
                .phone("+123456789")
                .email("ana.perez@example.com")
                .build();

        Professional saved = Professional.builder()
                .id(1L)
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .documentNumber(req.getDocumentNumber())
                .specialty(req.getSpecialty())
                .phone(req.getPhone())
                .email(req.getEmail())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(professionalRepository.existsByDocumentNumber(req.getDocumentNumber())).thenReturn(false);
        when(professionalRepository.save(any(Professional.class))).thenReturn(saved);

        ProfessionalResponse resp = professionalService.create(req);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("Ana", resp.getFirstName());
        verify(professionalRepository, times(1)).save(any(Professional.class));
    }

    @Test
    void getById_found() {
        Professional p = Professional.builder()
                .id(2L)
                .firstName("Juan")
                .lastName("Lopez")
                .documentNumber("DOC999")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(professionalRepository.findById(2L)).thenReturn(Optional.of(p));

        ProfessionalResponse resp = professionalService.getById(2L);

        assertNotNull(resp);
        assertEquals(2L, resp.getId());
        assertEquals("Juan", resp.getFirstName());
    }

    @Test
    void updateProfessional_success() {
        Professional existing = Professional.builder()
                .id(3L)
                .firstName("Old")
                .lastName("Name")
                .documentNumber("DOC555")
                .specialty("General")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UpdateProfessionalRequest req = UpdateProfessionalRequest.builder()
                .firstName("New")
                .specialty("Dermatology")
                .build();

        Professional updated = Professional.builder()
                .id(3L)
                .firstName("New")
                .lastName("Name")
                .documentNumber("DOC555")
                .specialty("Dermatology")
                .active(true)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(professionalRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(professionalRepository.save(any(Professional.class))).thenReturn(updated);

        ProfessionalResponse resp = professionalService.update(3L, req);

        assertNotNull(resp);
        assertEquals(3L, resp.getId());
        assertEquals("New", resp.getFirstName());
        assertEquals("Dermatology", resp.getSpecialty());
    }
}
