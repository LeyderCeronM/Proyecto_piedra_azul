package com.piedrazul.professionals.service;

import com.piedrazul.professionals.dto.CreateProfessionalRequest;
import com.piedrazul.professionals.dto.UpdateProfessionalRequest;
import com.piedrazul.professionals.dto.ProfessionalResponse;

import java.util.List;

public interface ProfessionalService {

    ProfessionalResponse create(CreateProfessionalRequest request);

    List<ProfessionalResponse> getAll();

    ProfessionalResponse getById(Long id);

    ProfessionalResponse getByDocument(String documentNumber);

    ProfessionalResponse update(Long id, UpdateProfessionalRequest request);

    void delete(Long id);
}
