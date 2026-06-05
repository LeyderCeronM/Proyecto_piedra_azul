# reports-service

Esqueleto del microservicio `reports-service` para Piedrazul.

Endpoints iniciales:
- `GET /api/v1/reports/statistics?from=YYYY-MM-DD&to=YYYY-MM-DD` — Devuelve estadísticas básicas (stub).

Base de datos:
- PostgreSQL: `piedrazul_reports_db`

Siguientes pasos recomendados:
- Implementar agregaciones (JPQL o consultas nativas) contra tablas de citas/pacientes.
- Añadir pruebas de integración y dataset de ejemplo.
# Reports Service

**Version:** 1.0.0  
**Project:** Piedrazul Medical Service Network
**Authors:** ... 
**Stack:** Java 21, Spring Boot, PostgreSQL, Maven
---