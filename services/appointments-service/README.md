# Appointments Service

**Version:** 1.0.0  
**Project:** Piedrazul Medical Service Network  
**Authors:** Juan Jose Rodriguez  
**Stack:** Java 21, Spring Boot 3.2.5, PostgreSQL, RabbitMQ, Maven

---

## Descripción

Microservicio responsable de la gestión del ciclo de vida de citas médicas: creación, consulta, reprogramación y cancelación. Valida pacientes de forma asíncrona con `users-service` mediante RabbitMQ.

---

## Puerto

| Servicio | Puerto |
|----------|--------|
| appointments-service | **8082** |

---

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/appointments` | Crear cita (validación async via RabbitMQ) |
| `GET` | `/api/appointments` | Listar todas las citas |
| `GET` | `/api/appointments/{id}` | Obtener cita por ID |
| `GET` | `/api/appointments/patient/{documentNumber}` | Listar citas por paciente |
| `PUT` | `/api/appointments/{id}` | Actualizar/reprogramar cita |
| `PATCH` | `/api/appointments/{id}/cancel` | Cancelar cita |
| `GET` | `/actuator/health` | Health check |

---

## Arquitectura

```
com.medical/
├── AppointmentsServiceApplication.java  ← Main class
├── entity/                              ← JPA entities
│   ├── Appointment.java
│   └── AppointmentStatus.java
├── repository/                          ← Spring Data repositories
│   └── AppointmentRepository.java
├── dto/                                 ← Request/Response DTOs
│   ├── CreateAppointmentRequest.java
│   ├── UpdateAppointmentRequest.java
│   ├── AppointmentResponse.java
│   ├── PatientValidationRequest.java
│   └── PatientValidationResponse.java
├── service/                             ← Business logic
│   ├── AppointmentService.java          (interface — DIP)
│   └── AppointmentServiceImpl.java
├── facade/                              ← Facade pattern (ADR-003)
│   └── PatientValidationFacade.java
├── controller/                          ← REST endpoints
│   └── AppointmentController.java
├── exception/                           ← Exception handlers
│   ├── AppointmentNotFoundException.java
│   ├── InvalidAppointmentException.java
│   ├── ValidationTimeoutException.java
│   └── GlobalExceptionHandler.java
└── config/                              ← Configuration
    └── RabbitMQConfig.java
```

---

## Requisitos

- Java 21+
- PostgreSQL 14+ (appointments_db en puerto 5433)
- RabbitMQ 3+ (puerto 5672)
- Maven 3.9+

---

## Inicio Rápido

```bash
# 1. Levantar infraestructura
docker-compose up -d

# 2. Compilar
cd services/appointments-service
mvn clean package -DskipTests

# 3. Ejecutar
java -jar target/appointments-service-1.0.0-SNAPSHOT.jar
```

---

## Patrones de Diseño

| Patrón | Implementación |
|--------|----------------|
| **Facade** (ADR-003) | `PatientValidationFacade` — encapsula comunicación async con users-service |
| **DIP** (ADR-004) | `AppointmentService` interface → `AppointmentServiceImpl` |
| **SRP** (ADR-004) | Controller, Service, Repository, ExceptionHandler separados |

---

## Comunicación Inter-Servicio

```
appointments-service  →  RabbitMQ  →  users-service
   (publish request)     (queue)     (@RabbitListener)
                                          ↓
appointments-service  ←  RabbitMQ  ←  users-service
  (@RabbitListener)      (queue)     (publish response)
```

- **Exchange:** `medical.exchange` (Topic)
- **Request Queue:** `patient.validation.requests`
- **Response Queue:** `patient.validation.responses`
- **Timeout:** 5 segundos