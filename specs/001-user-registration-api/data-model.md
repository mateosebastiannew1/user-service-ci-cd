# Data Model: User Registration API

**Feature**: 001-user-registration-api
**Date**: 2026-05-05

## Domain Entities

### User

Represents a registered user in the system. This is the sole domain entity for this
feature. It lives in `com.example.userservice.domain`.

**Constitution note**: MUST NOT carry JPA, Spring, or Jackson annotations. It is a
plain Java object. Serialisation concerns belong in the DTO layer.

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | `UUID` | Auto-generated, non-null, immutable | Generated at creation time |
| `nombre` | `String` | Non-blank | First name |
| `apellido` | `String` | Non-blank | Last name |
| `direccion` | `String` | Non-blank | Address |
| `telefono` | `String` | Non-blank, 10 digits, starts with `09` | Stored as-is after format validation |
| `correo` | `String` | Non-blank, valid email, unique (case-insensitive) | Stored lowercase-normalised |

**Identity**: `correo` (lowercased) is the natural business key for uniqueness checks.
`id` (UUID) is the system surrogate key returned to callers.

**Lifecycle**: Single state — `REGISTERED`. No state transitions in this version.

**Invariants**:
- `correo` is normalised to lowercase on creation and never mutated.
- `id` is assigned once at creation and never changed.
- No two `User` objects may have the same lowercased `correo`.

---

## DTOs

DTOs live in `com.example.userservice.dto` and are the only objects crossing the
HTTP boundary. Domain entities MUST NOT be serialised directly.

### CreateUserRequest (request DTO)

Used as the `@RequestBody` of `POST /api/v1/users`.

| Field | Type | Validation | Bean Validation Annotation |
|-------|------|------------|----------------------------|
| `nombre` | `String` | Non-blank | `@NotBlank` |
| `apellido` | `String` | Non-blank | `@NotBlank` |
| `direccion` | `String` | Non-blank | `@NotBlank` |
| `telefono` | `String` | Non-blank + pattern `09\d{8}` | `@NotBlank` + `@Pattern(regexp = "09\\d{8}")` |
| `correo` | `String` | Non-blank + valid email format | `@NotBlank` + `@Email` |

### UserResponse (response DTO)

Returned in the `201` response body after successful registration.

| Field | Type | Notes |
|-------|------|-------|
| `id` | `String` (UUID) | UUID rendered as string |
| `nombre` | `String` | |
| `apellido` | `String` | |
| `direccion` | `String` | |
| `telefono` | `String` | |
| `correo` | `String` | Lowercased-normalised value |

### ErrorResponse (error DTO)

Returned for all `400` responses.

| Field | Type | Notes |
|-------|------|-------|
| `message` | `String` | Top-level summary (e.g., `"Validation failed"`, `"usuario existente"`) |
| `errors` | `List<FieldError>` | Empty list `[]` for duplicate-email errors |

### FieldError (nested in ErrorResponse)

| Field | Type | Notes |
|-------|------|-------|
| `field` | `String` | Name of the offending field (e.g., `"nombre"`) |
| `message` | `String` | Reason (e.g., `"es requerido"`, `"formato invalido"`) |

---

## Repository Contract

Interface: `com.example.userservice.repository.UserRepository`

```
UserRepository
  + save(User): User
  + findByCorreo(String correoLowercase): Optional<User>
  + existsByCorreo(String correoLowercase): boolean
```

Implementation: `InMemoryUserRepository` — backed by `ConcurrentHashMap<String, User>`
keyed on lowercased `correo`.

**Constitution note**: This interface is defined in the `repository` package. The
`service` layer depends on the interface, not the implementation (DIP satisfied).

---

## Validation Rules Summary

| Field | Rule | Error message |
|-------|------|---------------|
| `nombre` | Must be present and non-blank | `"es requerido"` |
| `apellido` | Must be present and non-blank | `"es requerido"` |
| `direccion` | Must be present and non-blank | `"es requerido"` |
| `telefono` | Must be present, non-blank, AND match `09\d{8}` | `"es requerido"` / `"formato invalido"` |
| `correo` | Must be present, non-blank, AND valid email format | `"es requerido"` / `"formato invalido"` |
| `correo` | Must be unique (case-insensitive) across all users | `"usuario existente"` (top-level message) |

All field-level errors are collected in a single pass and returned together.
Uniqueness check only runs after all field validations pass.
