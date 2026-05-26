# Users Service

**Version:** 1.0.0  
**Project:** Piedrazul Medical Services Network  
**Authors:** Henry Fernando Mulato Llanten [henrymulato@unicauca.edu.co](mailto:henrymulato@unicauca.edu.co)  
**Stack:** Java 21, Spring Boot 3.5.14, PostgreSQL, Maven

---

## Tabla de Contenidos

- [Descripción](#descripción)
- [Arquitectura](#arquitectura)
- [Stack Tecnológico](#stack-tecnológico)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Requisitos](#requisitos)
- [Ejecución](#ejecución)
- [Endpoints](#endpoints)
- [User Stories Cubiertas](#user-stories-cubiertas)
- [Pruebas](#pruebas)
- [RabbitMQ](#rabbitmq)
- [Documentación Relacionada](#documentación-relacionada)

---

## Descripción

Microservicio de gestión de usuarios para el sistema de agendamiento de citas médicas del **Centro de Salud Piedra Azul**. Administra el ciclo de vida completo de los usuarios del sistema: creación, consulta, búsqueda, actualización y desactivación, con roles y permisos diferenciados.

Este servicio implementa las user stories del **Epic E1 — Manage Users** y utiliza autenticación por roles (ADMIN, SCHEDULER, PATIENT, PROFESSIONAL).

---

## Arquitectura

El servicio sigue los principios definidos en los ADRs del proyecto:

| ADR | Descripción |
|-----|-------------|
| [ADR-001](/ADRs/ADR-001-microservices-architecture.md) | Arquitectura de microservicios |
| [ADR-002](/ADRs/ADR-002-java-as-primary-language.md) | Java como lenguaje principal |
| [ADR-003](/ADRs/ADR-003-design-patterns.md) | Patrones Factory, Observer, Strategy |
| [ADR-004](/ADRs/ADR-004-solid-principles.md) | Principios SOLID |
| [ADR-005](/ADRs/ADR-005-spring-boot-for-microservices.md) | Spring Boot para microservicios |
| [ADR-006](/ADRs/ADR-006-postgresql-as-database.md) | PostgreSQL como base de datos |

### Patrón Factory (Strategy Pattern)

La creación de usuarios implementa el patrón **Strategy** mediante `UserCreationFactory`:

```
UserCreationFactory
├── AdminCreationStrategy       → Crea usuarios ADMIN
├── SchedulerCreationStrategy   → Crea usuarios SCHEDULER
├── PatientCreationStrategy     → Crea pacientes (con entidad Patient asociada)
└── ProfessionalCreationStrategy → Crea profesionales médicos (con entidad Professional asociada)
```

Cada estrategia valida los campos específicos de su rol. Las estrategias `PatientCreationStrategy` y `ProfessionalCreationStrategy` además crean las entidades asociadas (`Patient` o `Professional`) vía `createAssociatedEntities()`.

---

## Stack Tecnológico

| Componente | Versión |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.5.14 |
| Spring Data JPA | — |
| Spring Security | — |
| Spring AMQP (RabbitMQ) | — |
| PostgreSQL | 16+ |
| Maven | 3.9+ |
| JUnit 5 + Mockito | — |
| Testcontainers | 1.19.7 |
| Lombok | — |

---

## Estructura del Proyecto

```
users-service/
├── docs/
│   ├── modeling/uml/           # Diagramas PlantUML
│   └── postman-collection.md   # Endpoints para Postman
├── scripts/
│   ├── start.sh                # Inicia el servicio
│   ├── stop.sh                 # Detiene el servicio
│   └── test-endpoints.sh       # Prueba los endpoints con curl
├── src/
│   ├── main/java/com/medical/
│   │   ├── config/             # Seguridad, RabbitMQ
│   │   ├── controller/         # REST controller
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── entities/           # JPA entities (User, Patient, Professional)
│   │   ├── enums/              # UserRole
│   │   ├── factory/            # Strategy pattern para creación de usuarios
│   │   ├── messenger/          # RabbitMQ listener
│   │   ├── repository/         # JPA repositories
│   │   └── service/            # Lógica de negocio
│   ├── main/resources/
│   │   └── application.yml     # Configuración
│   └── test/java/com/medical/
│       ├── factory/            # Tests de estrategias
│       ├── integration/        # Tests de integración
│       └── service/            # Tests unitarios del servicio
├── pom.xml
└── README.md
```

---

## Requisitos

- **Java 21** (OpenJDK 21+)
- **Maven 3.9+**
- **PostgreSQL 16+** corriendo en `localhost:5432`
- **RabbitMQ** (opcional — necesario solo para validación asíncrona de pacientes)

### Base de Datos

```properties
# Valores por defecto (configurables via application.yml)
DB_NAME=users_db
DB_USER=medical_user
DB_PASSWORD=medical123
```

---

## Ejecución

### Usando scripts

```bash
# Iniciar el servicio (compila + ejecuta en background)
./scripts/start.sh

# Verificar que está corriendo
curl http://localhost:8081/actuator/health

# Probar todos los endpoints
./scripts/test-endpoints.sh

# Detener el servicio
./scripts/stop.sh
```

### Usando Maven directamente

```bash
# Compilar y ejecutar
mvn spring-boot:run

# O compilar y ejecutar el JAR
mvn clean package -DskipTests
java -jar target/users-service-1.0.0-SNAPSHOT.jar
```

### Perfil de desarrollo

La seguridad está deshabilitada para desarrollo (`permitAll()`).  
Puerto por defecto: **8081**.

---

## Endpoints

| Método | Endpoint | Descripción | Códigos HTTP |
|--------|----------|-------------|:------------:|
| `POST` | `/api/users` | Crear usuario | `201`, `400` |
| `GET` | `/api/users` | Listar todos los usuarios | `200` |
| `GET` | `/api/users/{id}` | Obtener usuario por ID | `200`, `400` |
| `PUT` | `/api/users/{id}` | Actualizar usuario | `200`, `400` |
| `PATCH` | `/api/users/{id}/deactivate` | Desactivar usuario | `204`, `400` |
| `GET` | `/api/users/patients/validate/{docNumber}` | Validar paciente por documento | `200` |
| `GET` | `/api/users/search/username/{username}` | Buscar por username | `200` |
| `GET` | `/api/users/search/email/{email}` | Buscar por email | `200` |
| `GET` | `/api/users/search/role/{role}` | Buscar por rol (con datos enriquecidos para PATIENT/PROFESSIONAL) | `200`, `400` |
| `GET` | `/api/users/search/status?active=true\|false` | Buscar por estado activo/inactivo | `200`, `400` |
| `GET` | `/api/users/search/advanced?username=&email=&role=&active=` | Búsqueda combinada (AND) | `200`, `400` |
| `GET` | `/actuator/health` | Health check | `200`, `503` |

> **Nota sobre enriquecimiento de respuesta:** `GET /api/users` retorna solo campos base de User (sin datos de Patient/Professional). Los endpoints de búsqueda específica (`GET /api/users/{id}`, `GET /api/users/search/role/{role}`, etc.) incluyen datos enriquecidos para roles `PATIENT` (firstName, lastName, documentType, documentNumber, birthDate, phone, address, eps) y `PROFESSIONAL` (firstName, lastName, specialty, licenseNumber, phone). Los campos no relevantes para el rol se omiten del JSON.

> Para ejemplos completos de request/response, ver [`docs/postman-collection.md`](docs/postman-collection.md).

### Roles Disponibles

| Rol | Descripción |
|-----|-------------|
| `ADMIN` | Acceso completo a CRUD de usuarios |
| `SCHEDULER` | Solo consulta de usuarios |
| `PATIENT` | Paciente con datos personales asociados |
| `PROFESSIONAL` | Profesional médico (médico/terapeuta) |

### Reglas de Validación

| Campo | Regla |
|-------|-------|
| `username` | 4–50 caracteres, único en el sistema |
| `password` | Mínimo 8 caracteres, 1 mayúscula, 1 número, 1 carácter especial |
| `email` | Formato válido, único en el sistema |
| `documentNumber` | Único (requerido para PATIENT) |
| `licenseNumber` | Único (requerido para PROFESSIONAL) |
| Último admin activo | No se puede desactivar |

---

## User Stories Cubiertas

### E1-US1 — Create User

| Escenario | Estado |
|-----------|--------|
| Registro exitoso con datos válidos | ✅ Cubierto |
| Rechazo por campos obligatorios faltantes | ✅ Cubierto |
| Rechazo por login duplicado | ✅ Cubierto |
| Rol médico con datos profesionales (specialty, licenseNumber) | ✅ Cubierto |
| Rol médico sin asociación profesional | ⏳ Pendiente (integración con professionals-service) |
| Rechazo por contraseña inválida | ✅ Cubierto |

### E1-US2 — Update User

| Escenario | Estado |
|-----------|--------|
| Actualización exitosa | ✅ Cubierto |
| Rechazo por datos inválidos/duplicados | ✅ Cubierto |
| Cambio a rol médico sin asociación | ⏳ Gap productivo documentado |

### E1-US3 — Consult User Data

| Escenario | Estado |
|-----------|--------|
| Listar usuarios (login, rol, estado) | ✅ Cubierto |
| Obtener usuario por ID (existe) | ✅ Cubierto |
| Obtener usuario por ID (no existe) | ✅ Cubierto |

### E1-US4 — Deactivate User

| Escenario | Estado |
|-----------|--------|
| Desactivación exitosa | ✅ Cubierto |
| Bloquear desactivación del último admin | ✅ Cubierto |
| Usuario no existe | ✅ Cubierto |
| Usuario ya inactivo | ✅ Cubierto |

### Search — Búsqueda de Usuarios

| Escenario | Estado |
|-----------|--------|
| Búsqueda por username (exacto) | ✅ Cubierto |
| Búsqueda por email (exacto) | ✅ Cubierto |
| Búsqueda por rol | ✅ Cubierto |
| Búsqueda por estado activo/inactivo | ✅ Cubierto |
| Búsqueda combinada (username + email + rol + estado) | ✅ Cubierto |
| Rol inválido retorna 400 | ✅ Cubierto |
| Parámetro faltante retorna 400 | ✅ Cubierto |
| Respuesta enriquecida con datos de Patient para rol PATIENT | ✅ Cubierto |
| Respuesta enriquecida con datos de Professional para rol PROFESSIONAL | ✅ Cubierto |
| Roles ADMIN/SCHEDULER retornan solo campos base | ✅ Cubierto |

> **Enriquecimiento de respuesta:** Los endpoints de consulta específica (`getUserById`, searchByUsername, searchByEmail, searchByRole, searchByStatus, searchAdvanced) cargan automáticamente los datos de la entidad `Patient` o `Professional` asociada cuando el rol es PATIENT o PROFESSIONAL respectivamente. `GET /api/users` (listar todos) retorna solo campos base. Los campos no relevantes para el rol se omiten del JSON (`@JsonInclude(NON_NULL)`). Si no existen datos asociados (inconsistencia), los campos enriquecidos se omiten sin lanzar excepción.

---

## Pruebas

El servicio sigue el proceso **SDD + TDD** con **strict TDD mode**.

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar solo tests unitarios
mvn test -Dtest="com.medical.service.UserServiceTest"

# Ejecutar solo tests de integración
mvn test -Dtest="com.medical.integration.UsersServiceIntegrationTest"
```

### Cobertura de Tests

| Tipo | Tests | Pasan | Saltados |
|------|:-----:|:-----:|:--------:|
| Unit (service) | 36 | 36 | 2 |
| Unit (factory) | 17 | 17 | 0 |
| Integration | 19 | 19 | 0 |
| **Total** | **72** | **72** | **2** |

> Los 2 tests saltados corresponden a escenarios pendientes de integración con `professionals-service`.
> 
> ✅ Fix aplicado: `ProfessionalCreationStrategy` ahora crea la entidad `Professional` asociada al registrar un usuario PROFESSIONAL, resolviendo el gap productivo donde el profesional se creaba sin sus datos específicos (specialty, licenseNumber).

### Convenciones de Tests

- Framework: **JUnit 5 + Mockito**
- Naming: `should{Behavior}_when{Condition}`
- Patrón: **AAA** (Arrange-Act-Assert)
- Interfaces con prefijo `I` (ej: `IUserRepository`)

---

## RabbitMQ

El servicio utiliza RabbitMQ para la **validación asíncrona de pacientes**:

| Componente | Nombre |
|------------|--------|
| Exchange | `medical.exchange` (topic) |
| Cola requests | `patient.validation.requests` |
| Cola responses | `patient.validation.responses` |
| Routing key requests | `validation.request` |
| Routing key responses | `validation.response` |

La validación síncrona está disponible via `GET /api/users/patients/validate/{documentNumber}` como respaldo para depuración.

---

## Documentación Relacionada

- [`docs/postman-collection.md`](docs/postman-collection.md) — Ejemplos de endpoints para Postman
- [`scripts/`](scripts/) — Scripts de gestión del servicio
- [`docs/modeling/uml/`](docs/modeling/uml/) — Diagramas PlantUML
- [`/docs/product/user-stories/`](/docs/product/user-stories/) — User stories del producto
- [`/docs/specs/features/users.spec.md`](/docs/specs/features/users.spec.md) — Especificación funcional
- [`/docs/product/epics.md`](/docs/product/epics.md) — Mapa de epics del sistema
- [`/ADRs/`](/ADRs/) — Architecture Decision Records

---

*Proyecto: Piedrazul Medical Services Network — Centro de Salud Piedra Azul*
