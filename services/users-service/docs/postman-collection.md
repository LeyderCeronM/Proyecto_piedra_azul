# Users Service — Postman Collection

> Endpoints para probar el microservicio de usuarios.
> Base URL: `http://localhost:8081`

---

## Índice

| # | Método | Endpoint | Descripción |
|---|--------|----------|-------------|
| 1 | `POST` | `/api/users` | Crear usuario |
| 2 | `GET` | `/api/users` | Listar todos los usuarios |
| 3 | `GET` | `/api/users/{id}` | Obtener usuario por ID |
| 4 | `PUT` | `/api/users/{id}` | Actualizar usuario |
| 5 | `PATCH` | `/api/users/{id}/deactivate` | Desactivar usuario |
| 6 | `GET` | `/api/users/patients/validate/{documentNumber}` | Validar paciente por documento |
| 7 | `GET` | `/api/users/search/username/{username}` | Buscar por username |
| 8 | `GET` | `/api/users/search/email/{email}` | Buscar por email |
| 9 | `GET` | `/api/users/search/role/{role}` | Buscar por rol |
| 10 | `GET` | `/api/users/search/status?active=true\|false` | Buscar por estado |
| 11 | `GET` | `/api/users/search/advanced?username=&email=&role=&active=` | Búsqueda combinada |
| 12 | `GET` | `/actuator/health` | Health check |

---

## 1. Crear usuario

> **`POST /api/users`**

Roles disponibles: `ADMIN`, `SCHEDULER`, `PATIENT`, `PROFESSIONAL`.

### ADMIN / SCHEDULER

```json
{
  "username": "admin1",
  "password": "Admin123!",
  "email": "admin1@medical.com",
  "role": "ADMIN"
}
```

### PATIENT

```json
{
  "username": "juanperez",
  "password": "Clave123!",
  "email": "juan@email.com",
  "role": "PATIENT",
  "firstName": "Juan",
  "lastName": "Pérez",
  "documentType": "CC",
  "documentNumber": "1234567890",
  "phone": "3001234567",
  "address": "Calle 123 #45-67",
  "eps": "Nueva EPS"
}
```

### PROFESSIONAL

```json
{
  "username": "dralopez",
  "password": "Clave123!",
  "email": "lopez@medical.com",
  "role": "PROFESSIONAL",
  "firstName": "María",
  "lastName": "López",
  "specialty": "Neuralterapia",
  "licenseNumber": "LIC-2024-001",
  "phone": "3109876543"
}
```

**Response — `201 Created`:**

```json
{
  "id": 1,
  "username": "juanperez",
  "email": "juan@email.com",
  "role": "PATIENT",
  "active": true,
  "createdAt": "2026-05-22T10:30:00",
  "updatedAt": "2026-05-22T10:30:00",
  "firstName": "Juan",
  "lastName": "Pérez",
  "specialty": null,
  "licenseNumber": null,
  "documentType": "CC",
  "documentNumber": "1234567890",
  "birthDate": null,
  "phone": "3001234567",
  "address": "Calle 123 #45-67",
  "eps": "Nueva EPS"
}
```

> Los datos del paciente se cargan automáticamente desde la entidad `Patient` asociada. Si el rol es `PROFESSIONAL`, se incluyen `specialty`, `licenseNumber` y `phone`.

---

## 2. Listar todos los usuarios

> **`GET /api/users`**

**Response — `200 OK`** (solo campos base, sin enriquecimiento):

```json
[
  {
    "id": 1,
    "username": "juanperez",
    "email": "juan@email.com",
    "role": "PATIENT",
    "active": true,
    "createdAt": "2026-05-22T10:30:00",
    "updatedAt": "2026-05-22T10:30:00"
  }
]
```

> `GET /api/users` retorna solo campos base de User, independientemente del rol. Para obtener datos enriquecidos usar `GET /api/users/{id}` o los endpoints de búsqueda específica.

---

## 3. Obtener usuario por ID

> **`GET /api/users/{id}`**

**Response — `200 OK`** (PATIENT):

```json
{
  "id": 1,
  "username": "juanperez",
  "email": "juan@email.com",
  "role": "PATIENT",
  "active": true,
  "createdAt": "2026-05-22T10:30:00",
  "updatedAt": "2026-05-22T10:30:00",
  "firstName": "Juan",
  "lastName": "Pérez",
  "specialty": null,
  "licenseNumber": null,
  "documentType": "CC",
  "documentNumber": "1234567890",
  "birthDate": null,
  "phone": "3001234567",
  "address": "Calle 123 #45-67",
  "eps": "Nueva EPS"
}
```

**Response — `200 OK`** (PROFESSIONAL):

```json
{
  "id": 2,
  "username": "dralopez",
  "email": "lopez@medical.com",
  "role": "PROFESSIONAL",
  "active": true,
  "createdAt": "2026-05-22T10:30:00",
  "updatedAt": "2026-05-22T10:30:00",
  "firstName": "María",
  "lastName": "López",
  "specialty": "Neuralterapia",
  "licenseNumber": "LIC-2024-001",
  "phone": "3109876543",
  "documentType": null,
  "documentNumber": null,
  "birthDate": null,
  "address": null,
  "eps": null
}
```

**Response — `400 Bad Request`** (usuario no encontrado):

```
User not found
```

---

## 4. Actualizar usuario

> **`PUT /api/users/{id}`**

Todos los campos son opcionales — solo se actualizan los que se envían.

```json
{
  "username": "juanperez2",
  "email": "juan2@email.com",
  "password": "NuevaClave1!",
  "role": "PATIENT"
}
```

**Response — `200 OK`:**

```json
{
  "id": 1,
  "username": "juanperez2",
  "email": "juan2@email.com",
  "role": "PATIENT",
  "active": true,
  "createdAt": "2026-05-22T10:30:00",
  "updatedAt": "2026-05-22T10:45:00",
  "firstName": "Juan",
  "lastName": "Pérez",
  "specialty": null,
  "licenseNumber": null,
  "documentType": "CC",
  "documentNumber": "1234567890",
  "birthDate": null,
  "phone": "3001234567",
  "address": "Calle 123 #45-67",
  "eps": "Nueva EPS"
}
```

---

## 5. Desactivar usuario

> **`PATCH /api/users/{id}/deactivate`**

**Response — `204 No Content`** (body vacío).

**Response — `400 Bad Request`:**

```
The user is already inactive
```

```
It is not possible to deactivate the last administrator of the system
```

---

## 6. Validar paciente por documento

> **`GET /api/users/patients/validate/{documentNumber}`**

Endpoint REST de respaldo. La validación asíncrona real va por RabbitMQ.

**Response — `200 OK`:**

```json
true
```

o

```json
false
```

---

## 7. Health Check

> **`GET /actuator/health`**

**Response — `200 OK`:**

```json
{
  "status": "UP"
}
```

---

## 7. Buscar por username

> **`GET /api/users/search/username/{username}`**
> Busca usuarios cuyo username coincida exactamente.

```json
// Response 200 — List<UserResponse>
[
  {
    "id": 1,
    "username": "testadmin",
    "email": "testadmin@medical.com",
    "role": "ADMIN",
    "active": true
  }
]
```

## 8. Buscar por email

> **`GET /api/users/search/email/{email}`**
> Busca usuarios cuyo email coincida exactamente.

## 9. Buscar por rol

> **`GET /api/users/search/role/{role}`**
> Rol válido: `ADMIN`, `SCHEDULER`, `PATIENT`, `PROFESSIONAL`. Rol inválido → HTTP 400.
>
> **Enriquecimiento:** Para rol `PATIENT` la respuesta incluye firstName, lastName, documentType, documentNumber, birthDate, phone, address, eps.
> Para rol `PROFESSIONAL` incluye firstName, lastName, specialty, licenseNumber, phone.
> Para `ADMIN`/`SCHEDULER` solo campos base.

## 10. Buscar por estado

> **`GET /api/users/search/status?active=true|false`**
> Filtra por usuarios activos o inactivos. Parámetro `active` requerido.

## 11. Búsqueda combinada

> **`GET /api/users/search/advanced?username=&email=&role=&active=`**
> Todos los parámetros son opcionales. Devuelve usuarios que coinciden con TODOS los filtros provistos (AND lógico).

---

## Reglas de validación

| Campo | Regla |
|-------|-------|
| `username` | 4–50 caracteres, único |
| `password` | Mínimo 8 chars, 1 mayúscula, 1 número, 1 especial |
| `email` | Formato válido, único |
| `role` | `ADMIN`, `SCHEDULER`, `PATIENT`, `PROFESSIONAL` |
| `documentNumber` | Único (para PATIENT) |
| `licenseNumber` | Único (para PROFESSIONAL) |
| Último admin activo | No se puede desactivar |

---

## Orden sugerido para pruebas en Postman

1. Crear un ADMIN → `POST /api/users`
2. Crear un PATIENT → `POST /api/users`
3. Crear un PROFESSIONAL → `POST /api/users`
4. Listar todos → `GET /api/users`
5. Obtener por ID → `GET /api/users/1`
6. Actualizar → `PUT /api/users/1`
7. Validar paciente → `GET /api/users/patients/validate/1234567890`
8. Desactivar → `PATCH /api/users/3/deactivate`
9. Buscar por username → `GET /api/users/search/username/testadmin`
10. Buscar por email → `GET /api/users/search/email/patient@email.com`
11. Buscar por rol → `GET /api/users/search/role/PATIENT`
12. Buscar activos → `GET /api/users/search/status?active=true`
13. Búsqueda combinada → `GET /api/users/search/advanced?role=ADMIN&active=true`
14. Health check → `GET /actuator/health`
