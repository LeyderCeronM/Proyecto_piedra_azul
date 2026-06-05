package com.piedrazul.professionals.controller;

import com.piedrazul.professionals.config.RabbitMQConfig;
import com.piedrazul.professionals.dto.CreateProfessionalRequest;
import com.piedrazul.professionals.dto.ProfessionalResponse;
import com.piedrazul.professionals.dto.UpdateProfessionalRequest;
import com.piedrazul.professionals.messenger.ProfessionalEvents;
import com.piedrazul.professionals.service.ProfessionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/professionals")
@RequiredArgsConstructor
public class ProfessionalsRestController {

    private final ProfessionalService professionalService;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping
    public ResponseEntity<ProfessionalResponse> createProfessional(
            @Valid @RequestBody CreateProfessionalRequest request) {
        ProfessionalResponse response = professionalService.create(request);

        // Publish created event
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MEDICAL_EXCHANGE,
                RabbitMQConfig.PROFESSIONALS_CREATED_ROUTING_KEY,
                new ProfessionalEvents(response)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> getAll() {
        return ResponseEntity.ok(professionalService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.getById(id));
    }

    @GetMapping("/document/{documentNumber}")
    public ResponseEntity<ProfessionalResponse> getByDocument(@PathVariable String documentNumber) {
        return ResponseEntity.ok(professionalService.getByDocument(documentNumber));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> updateProfessional(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfessionalRequest request) {

        ProfessionalResponse response = professionalService.update(id, request);

        // Publish updated event
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MEDICAL_EXCHANGE,
                RabbitMQConfig.PROFESSIONALS_UPDATED_ROUTING_KEY,
                new ProfessionalEvents(response)
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessional(@PathVariable Long id) {
        professionalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
