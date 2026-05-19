package com.medical.facade;

import com.medical.config.RabbitMQConfig;
import com.medical.dto.PatientValidationRequest;
import com.medical.dto.PatientValidationResponse;
import com.medical.exception.InvalidAppointmentException;
import com.medical.exception.ValidationTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Facade that encapsulates async communication with users-service for patient validation.
 *
 * <p>Uses RabbitMQ to publish validation requests and listens for responses,
 * correlating them via a unique correlationId with a CompletableFuture map.
 *
 * <p>Design pattern: Facade (ADR-003) — hides the complexity of async messaging
 * behind a simple synchronous-looking API for the service layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PatientValidationFacade {

    private static final long VALIDATION_TIMEOUT_SECONDS = 5;

    private final RabbitTemplate rabbitTemplate;

    /**
     * Pending validation futures indexed by correlationId.
     */
    private final ConcurrentMap<String, CompletableFuture<PatientValidationResponse>> pendingValidations
            = new ConcurrentHashMap<>();

    /**
     * Validate a patient by document number via async RabbitMQ communication with users-service.
     *
     * @param documentNumber the patient's document number
     * @return the validation response from users-service
     * @throws ValidationTimeoutException if users-service doesn't respond within timeout
     * @throws InvalidAppointmentException if patient is not found
     */
    public PatientValidationResponse validatePatient(String documentNumber) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<PatientValidationResponse> future = new CompletableFuture<>();
        pendingValidations.put(correlationId, future);

        try {
            // Publish validation request
            PatientValidationRequest request = PatientValidationRequest.builder()
                    .correlationId(correlationId)
                    .documentNumber(documentNumber)
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.MEDICAL_EXCHANGE,
                    RabbitMQConfig.VALIDATION_REQUEST_ROUTING_KEY,
                    request
            );
            log.info("Published validation request for document: {}, correlationId: {}",
                    documentNumber, correlationId);

            // Wait for response with timeout
            PatientValidationResponse response = future.get(
                    VALIDATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!response.isValid()) {
                throw new InvalidAppointmentException(
                        response.getError() != null
                                ? response.getError()
                                : "Patient not found with document: " + documentNumber);
            }

            return response;

        } catch (TimeoutException e) {
            log.error("Validation timeout for document: {}", documentNumber);
            throw new ValidationTimeoutException();
        } catch (InvalidAppointmentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during patient validation: {}", e.getMessage(), e);
            throw new ValidationTimeoutException();
        } finally {
            pendingValidations.remove(correlationId);
        }
    }

    /**
     * Listener for validation responses from users-service.
     * Completes the corresponding CompletableFuture based on correlationId.
     */
    @RabbitListener(queues = RabbitMQConfig.PATIENT_VALIDATION_RESPONSES_QUEUE)
    public void handleValidationResponse(PatientValidationResponse response) {
        log.info("Received validation response for correlationId: {}, valid: {}",
                response.getCorrelationId(), response.isValid());

        CompletableFuture<PatientValidationResponse> future =
                pendingValidations.get(response.getCorrelationId());

        if (future != null) {
            future.complete(response);
        } else {
            log.warn("No pending validation found for correlationId: {}",
                    response.getCorrelationId());
        }
    }
}
