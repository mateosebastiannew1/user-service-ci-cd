# Quickstart: User Registration API

**Feature**: 001-user-registration-api
**Date**: 2026-05-05

## Prerequisites

- Java 17+ installed (`java -version`)
- Maven 3.9+ installed (`mvn -version`)
- No database or external services required

## Run the service

```bash
# From repository root
mvn spring-boot:run
```

Service starts on `http://localhost:8080`.
Swagger UI available at `http://localhost:8080/swagger-ui.html`.

## Validate the feature manually

### 1 — Successful registration (expect HTTP 201)

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Perez",
    "direccion": "Calle 1 #23-45",
    "telefono": "0912345678",
    "correo": "juan@example.com"
  }' | jq .
```

**Expected response** (`201 Created`):
```json
{
  "id": "<uuid>",
  "nombre": "Juan",
  "apellido": "Perez",
  "direccion": "Calle 1 #23-45",
  "telefono": "0912345678",
  "correo": "juan@example.com"
}
```

### 2 — Duplicate email (expect HTTP 400)

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Perez",
    "direccion": "Calle 1",
    "telefono": "0912345678",
    "correo": "juan@example.com"
  }' | jq .
```

**Expected response** (`400 Bad Request`):
```json
{
  "message": "usuario existente",
  "errors": []
}
```

### 3 — Missing required field (expect HTTP 400)

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "apellido": "Perez",
    "direccion": "Calle 1",
    "telefono": "0912345678",
    "correo": "pedro@example.com"
  }' | jq .
```

**Expected response** (`400 Bad Request`):
```json
{
  "message": "Validation failed",
  "errors": [
    { "field": "nombre", "message": "es requerido" }
  ]
}
```

### 4 — Invalid telefono format (expect HTTP 400)

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Pedro",
    "apellido": "Lopez",
    "direccion": "Calle 2",
    "telefono": "12345",
    "correo": "pedro@example.com"
  }' | jq .
```

**Expected response** (`400 Bad Request`):
```json
{
  "message": "Validation failed",
  "errors": [
    { "field": "telefono", "message": "formato invalido" }
  ]
}
```

### 5 — Case-insensitive duplicate email (expect HTTP 400)

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Perez",
    "direccion": "Calle 3",
    "telefono": "0987654321",
    "correo": "JUAN@EXAMPLE.COM"
  }' | jq .
```

**Expected response** (`400 Bad Request`):
```json
{
  "message": "usuario existente",
  "errors": []
}
```

## Run automated tests

```bash
# All tests (unit + functional)
mvn test

# Unit tests only
mvn test -Dtest="*Test"

# Functional tests only
mvn test -Dtest="*FunctionalTest"
```

## Coverage report

```bash
mvn verify
# Report generated at: target/site/jacoco/index.html
```

Coverage gate: **80% minimum** on `service` and `domain` packages (enforced by JaCoCo
Maven plugin — build fails if gate not met).
