package com.medical.appointments.controller;

import com.medical.controller.AppointmentController;
import com.medical.repository.AppointmentRepository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AppointmentRepository repository;

    @Test
    void listReturnsOk() throws Exception {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
