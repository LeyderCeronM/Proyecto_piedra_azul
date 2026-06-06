package com.medical.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.medical.client.dto.AppointmentDto;

import java.util.List;

/**
 * Simple singleton client for the Appointments microservice.
 * Usage: AppointmentServiceClient.getInstance("http://localhost:8081").getAllAppointments()
 */
public class AppointmentServiceClient extends BaseServiceClient {
    private static AppointmentServiceClient INSTANCE;
    private static final String ENDPOINT = "/appointments";

    private AppointmentServiceClient(String baseUrl) {
        super(baseUrl);
    }

    public static synchronized AppointmentServiceClient getInstance(String baseUrl) {
        if (INSTANCE == null) {
            INSTANCE = new AppointmentServiceClient(baseUrl);
        }
        return INSTANCE;
    }

    public List<AppointmentDto> getAllAppointments() throws ServiceException {
        return sendGet(ENDPOINT, new TypeReference<List<AppointmentDto>>() {});
    }

    public AppointmentDto createAppointment(AppointmentDto dto) throws ServiceException {
        return sendPost(ENDPOINT, dto, AppointmentDto.class);
    }

    public AppointmentDto updateAppointment(Long id, AppointmentDto dto) throws ServiceException {
        return sendPut(ENDPOINT + "/" + id, dto, AppointmentDto.class);
    }
}
