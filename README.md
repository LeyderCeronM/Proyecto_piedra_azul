# Medical Services Network

**Proyecto:** Sistema de Gestión de Citas Médicas para Centro de Salud Piedra Azul  
**Stack:** Java 21, Spring Boot 3.5.14, PostgreSQL, JavaFX, Maven  
**Arquitectura:** Microservicios

---

## Descripción

Sistema de gestión de servicios médicos implementado como arquitectura de microservicios con comunicación asíncrona via RabbitMQ.

### Servicios

| Service | Puerto | Base de Datos | Responsabilidad | Estado |
|---------|--------|---------------|-----------------|--------|
| api-gateway | 8080 | — | Routing y autenticación | ✅ Completado  |
| users-service | 8081 | users_db | Gestión de usuarios, pacientes, profesionales | ✅ Completado |
| appointments-service | 8082 | appointments_db | Gestión de citas médicas | ✅ Completado  |
| professionals-service | 8083 | professionals_db | Gestión de profesionales | ✅ Completado  |
| clinical-records-service | 8084 | clinical_records_db | Registros clínicos | ✅ Completado  |
| reports-service | 8085 | reports_db | Reportes estadísticos | ✅ Completado 
| audits-service | 8086 | audits_db | Auditoría y logs | ✅ Completado  |

---

## Tech Stack

| Componente | Tecnología | ADR |
|------------|------------|-----|
| Lenguaje | Java 21 | ADR-002 |
| Framework | Spring Boot | ADR-005 |
| Base de Datos | PostgreSQL | ADR-006 |
| UI Desktop | JavaFX | ADR-007 |
| Arquitectura | Microservicios | ADR-001 |
| Patrones | Factory, Observer, Strategy | ADR-003 |
| Principios | SOLID | ADR-004 |

---

## Estructura del Proyecto

```
Medical-Services-Network/
├── ADRs/                          # Architecture Decision Records
│   ├── ADR-001-microservices-architecture.md
│   ├── ADR-002-java-as-primary-language.md
│   ├── ADR-003-design-patterns.md
│   ├── ADR-004-solid-principles.md
│   ├── ADR-005-spring-boot-for-microservices.md
│   ├── ADR-006-postgresql-as-database.md
│   └── ADR-007-javafx-for-desktop-ui.md
│
├── services/                       # Microservicios
│   ├── api-gateway/               # Puerto 8080 — ✅ Completado 
│   ├── users-service/            # Puerto 8081 — ✅ Completado
│   ├── appointments-service/      # Puerto 8082 — ✅ Completado 
│   ├── professionals-service/    # Puerto 8083 — ✅ Completado 
│   ├── clinical-records-service/  # Puerto 8084 — ✅ Completado 
│   ├── reports-service/          # Puerto 8085 — ✅ Completado 
│   └── audits-service/           # Puerto 8086 — ✅ Completado 
│
├── shared/                        # Librerías compartidas
│   └── medical-common/           # Modelos, DTOs, excepciones
│
├── docs/                         # Documentación general
│   ├── architecture/             # C4 model, diagramas
│   ├── product/                  # PRD, user stories, epics
│   └── specs/                    # Especificaciones funcionales
│
└── README.md                      # Este archivo
```

---

## Prerrequisitos

1. **Java 21** o superior
2. **PostgreSQL 14+**
3. **RabbitMQ 3** (vía Docker)
4. **Maven 3.9+**

---

## Inicio Rápido

### 1. Levantar Infraestructura

```bash
docker-compose up -d
```

### 2. Compilar Servicios

```bash
cd services/users-service && mvn clean package -DskipTests
cd services/appointments-service && mvn clean package -DskipTests
```

### 3. Iniciar Servicios

```bash
# Cada servicio en su puerto correspondiente (8081-8086)
java -jar services/users-service/target/users-service-*.jar
```

---

## Documentación

| Documento | Descripción |
|-----------|-------------|
| `ADRs/` | Architectural Decision Records |
| `docs/product/user-stories/` | Historias de usuario |

---

## Principios de Diseño

Este proyecto sigue los principios SOLID (ADR-004) y utiliza patrones de diseño (ADR-003):

- **Factory Method**: Centralización de creación de entidades
- **Observer**: Sincronización de vistas con estado
- **Strategy**: Persistencia flexible sin modificar repositorios

---

## Estado del Proyecto

| Fase | Estado |
|------|--------|
| `users-service` — CRUD, búsqueda, roles, enriquecimiento | ✅ Completado |
| `appointments-service` — Gestión de citas | ✅ Completado  |
| `professionals-service` — Gestión de profesionales | ✅ Completado  |
| `clinical-records-service` — Registros clínicos | ✅ Completado  |
| `reports-service` — Reportes estadísticos | ✅ Completado  |
| `audits-service` — Auditoría y logs | ✅ Completado  |
| `api-gateway` — Routing y autenticación | ✅ Completado  |
| Cliente JavaFX (Desktop) | ✅ Completado  |

### users-service — Funcionalidades Implementadas

El servicio de usuarios ya completado incluye:

- **CRUD completo**: Crear, consultar, actualizar y desactivar usuarios
- **4 roles diferenciados**: ADMIN, SCHEDULER, PATIENT, PROFESSIONAL
- **Factory + Strategy**: Patrón de diseño para creación de usuarios por rol
- **Enriquecimiento de respuesta**: Datos de Patient/Professional según el rol
- **Búsqueda flexible**: Por username, email, rol, estado, y búsqueda combinada
- **RabbitMQ**: Validación asíncrona de pacientes
- **72 tests** (unitarios + integración), TDD estricto
- **Spring Boot 3.5.14 + Java 21**

---

## Próximos Pasos

- [ ] Implementar appointments-service   ✅ Completado 
- [ ] Implementar professionals-service  ✅ Completado 
- [ ] Implementar clinical-records-service  ✅ Completado 
- [ ] Implementar reports-service    ✅ Completado 
- [ ] Implementar audits-service     ✅ Completado 
- [ ] Implementar API Gateway con autenticación JWT  ✅ Completado 
- [ ] Implementar cliente JavaFX   ✅ Completado 

---

## Licencia

Este proyecto es para fines educativos y de demostración.

---

## Autores

Henry Fernando Mulato Llanten - Juan Jose Rodriguez - Leyder Ceron
Centro de Salud Piedra Azul - Popayán, Colombia
