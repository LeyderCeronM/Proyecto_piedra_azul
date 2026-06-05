# Piedrazul — Desktop Client

Módulo JavaFX minimal para probar endpoints del API Gateway.

Compilar:

```bash
mvn -f desktop-client/pom.xml clean package
```

Ejecutar en modo desarrollo (requiere JavaFX disponible via plugin):

```bash
mvn -f desktop-client/pom.xml javafx:run
```

Notas:
- Este POM usa Java 21 y dependencias JavaFX para Windows (clasificador `win`). Ajusta el `classifier` en el POM para otra plataforma.
- El cliente usa `HttpClient` nativo de Java y Jackson para formatear JSON.
- Endpoints utilizados de ejemplo:
  - `GET http://localhost:8080/api/v1/reports/statistics?from=2026-01-01&to=2026-06-01`
  - `GET http://localhost:8080/api/v1/appointments`
