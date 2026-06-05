package com.piedrazul.professionals.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piedrazul.professionals.dto.CreateProfessionalRequest;
import com.piedrazul.professionals.dto.ProfessionalResponse;
import com.piedrazul.professionals.dto.UpdateProfessionalRequest;
import com.piedrazul.professionals.service.ProfessionalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfessionalsRestController.class)
class ProfessionalsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfessionalService professionalService;

    @Test
    void createProfessional_returnsCreated() throws Exception {
        CreateProfessionalRequest req = CreateProfessionalRequest.builder()
                .firstName("Test")
                .lastName("User")
                .documentNumber("DOC1")
                .build();

        ProfessionalResponse resp = ProfessionalResponse.builder()
                .id(10L)
                .firstName("Test")
                .lastName("User")
                .documentNumber("DOC1")
                .createdAt(LocalDateTime.now())
                .build();

        when(professionalService.create(any(CreateProfessionalRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/professionals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getById_returnsOk() throws Exception {
        ProfessionalResponse resp = ProfessionalResponse.builder()
                .id(20L)
                .firstName("Get")
                .lastName("One")
                .documentNumber("DOC20")
                .createdAt(LocalDateTime.now())
                .build();

        when(professionalService.getById(20L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/professionals/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void updateProfessional_returnsOk() throws Exception {
        UpdateProfessionalRequest req = UpdateProfessionalRequest.builder()
                .firstName("Updated")
                .build();

        ProfessionalResponse resp = ProfessionalResponse.builder()
                .id(30L)
                .firstName("Updated")
                .lastName("User")
                .documentNumber("DOC30")
                .createdAt(LocalDateTime.now())
                .build();

        when(professionalService.update(any(Long.class), any(UpdateProfessionalRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/api/v1/professionals/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }
}
