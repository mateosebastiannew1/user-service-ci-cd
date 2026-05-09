# Feature Specification: User Registration API

**Feature Branch**: `001-user-registration-api`
**Created**: 2026-05-05
**Status**: Draft
**Input**: User description: "Como desarrollador requiero crear una API de registro de usuarios
para almacenar la informacion de los mismos."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Successful User Registration (Priority: P1)

A developer integrating with the User Service sends a POST request to `/api/v1/users`
with a complete and valid JSON payload containing the user's first name, last name,
address, phone number, and email address. The system stores the user in memory and
responds with the full user data including a newly generated unique identifier.

**Why this priority**: This is the core happy-path scenario — without it, no other
scenario has value. All downstream use cases depend on being able to successfully
register a user.

**Independent Test**: Send a valid POST request with all required fields and verify
a `201 Created` response is returned containing the same input data plus a UUID identifier.

**Acceptance Scenarios**:

1. **Given** the system has no registered users, **When** a POST request is sent to
   `/api/v1/users` with `{"nombre":"Juan","apellido":"Perez","direccion":"Calle 1","telefono":"0912345678","correo":"juan@example.com"}`,
   **Then** the system responds with HTTP `201`, and the response body contains all
   five fields plus a non-null, non-empty `id` field in UUID format.

2. **Given** a user with email `juan@example.com` is already registered, **When** a
   second POST request is sent to `/api/v1/users` with a different email
   `pedro@example.com` and all other required fields, **Then** the system responds
   with HTTP `201` and returns the new user's data with a different UUID.

---

### User Story 2 - Duplicate Email Rejected (Priority: P2)

A developer sends a POST request to register a user whose email address already exists
in the system. The system detects the duplicate and responds with a clear error
indicating the user already exists.

**Why this priority**: Email uniqueness is a fundamental data-integrity rule. Without
it, the system would allow multiple accounts with the same identifier, corrupting the
user registry.

**Independent Test**: Register a user successfully, then attempt to register again
with the same email and verify a `400 Bad Request` with a "usuario existente" message
is returned.

**Acceptance Scenarios**:

1. **Given** a user with email `juan@example.com` is already registered, **When** a
   POST request is sent to `/api/v1/users` with the same email `juan@example.com`,
   **Then** the system responds with HTTP `400` and the body
   `{"message": "usuario existente", "errors": []}`.

---

### User Story 3 - Missing Required Fields Rejected (Priority: P3)

A developer sends a POST request to register a user but omits one or more of the
required fields (`nombre`, `apellido`, `direccion`, `telefono`, `correo`). The system
validates the payload and responds with a descriptive error identifying which fields
are missing.

**Why this priority**: Input validation protects data quality and gives integrators
immediate, actionable feedback when their payload is malformed.

**Independent Test**: Send a POST request omitting each required field one at a time
and verify that a `400 Bad Request` is returned with a message naming the missing field.

**Acceptance Scenarios**:

1. **Given** a POST request is sent to `/api/v1/users` with the `correo` field omitted,
   **Then** the system responds with HTTP `400` and the body contains
   `{"message":"Validation failed","errors":[{"field":"correo","message":"es requerido"}]}`.

2. **Given** a POST request is sent to `/api/v1/users` with the `nombre` field omitted,
   **Then** the system responds with HTTP `400` and the body contains
   `{"message":"Validation failed","errors":[{"field":"nombre","message":"es requerido"}]}`.

3. **Given** a POST request is sent to `/api/v1/users` with `nombre` and `correo` omitted,
   **Then** the system responds with HTTP `400` and the body contains
   `{"message":"Validation failed","errors":[{"field":"nombre","message":"es requerido"},{"field":"correo","message":"es requerido"}]}`
   — all missing fields reported in a single response.

4. **Given** a POST request is sent to `/api/v1/users` with `nombre` omitted and
   `telefono` set to `"12345"` (wrong format), **Then** the system responds with HTTP
   `400` and the body contains both errors in a single response:
   `{"message":"Validation failed","errors":[{"field":"nombre","message":"es requerido"},{"field":"telefono","message":"formato invalido"}]}`.

---

### Edge Cases

- What happens when the request body is empty or not valid JSON?
  → System MUST return HTTP `400` with an appropriate parse-error message.
- What happens when `correo` is present but not in a valid email format?
  → System MUST return HTTP `400` with `{"field":"correo","message":"formato invalido"}` in the errors array.
- What happens when fields are present but contain only whitespace?
  → System MUST treat whitespace-only values as absent and return HTTP `400`.
- What happens when `telefono` does not match the `09XXXXXXXX` pattern?
  → System MUST return HTTP `400` with `{"field":"telefono","message":"formato invalido"}` in the errors array.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST expose a POST endpoint at `/api/v1/users` that accepts a
  JSON body containing `nombre`, `apellido`, `direccion`, `telefono`, and `correo`.
- **FR-002**: System MUST validate all five fields (`nombre`, `apellido`, `direccion`,
  `telefono`, `correo`) for presence (non-blank) AND format correctness in a **single
  pass**. If any combination of missing-field or format errors exists, the system MUST
  respond with HTTP `400` reporting ALL failures together in one response:
  `{"message": "Validation failed", "errors": [{"field": "<fieldName>", "message": "<reason>"}]}`.
  Reason values: `"es requerido"` for missing/blank fields, `"formato invalido"` for
  format violations. Never split errors across multiple responses.
- **FR-002a**: `telefono` MUST be exactly 10 numeric digits and start with `09`
  (e.g., `0912345678`). Format violations are reported alongside any other errors in
  the same `errors` array (see FR-002).
- **FR-003**: System MUST validate that `correo` is unique across all registered users
  using a **case-insensitive** comparison (e.g., `Juan@Example.com` equals `juan@example.com`);
  if a duplicate is detected, it MUST respond with HTTP `400` using the envelope:
  `{"message": "usuario existente", "errors": []}`.
- **FR-004**: System MUST store the registered user's data in memory (no persistent
  storage required).
- **FR-005**: On successful registration, the system MUST respond with HTTP `201` and
  return a JSON body containing all five input fields plus an `id` field holding a
  UUID generated automatically by the system.
- **FR-006**: The auto-generated `id` MUST be unique across all registered users and
  MUST follow the UUID format (e.g., `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`).
- **FR-007**: System MUST NOT accept a request body that is missing, malformed, or
  not valid JSON; such requests MUST receive HTTP `400`.

### Key Entities

- **User**: Represents a registered user. Attributes: `id` (UUID, auto-generated),
  `nombre` (required string), `apellido` (required string), `direccion` (required
  string), `telefono` (required string — exactly 10 digits, must start with `09`),
  `correo` (required string, unique case-insensitive).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer can successfully register a new user by sending a single
  POST request with a complete payload and receive a `201` response containing a
  UUID-identified user record — verifiable in under 5 seconds.
- **SC-002**: 100% of POST requests with missing required fields or invalid field
  formats receive a `400` response with the structured envelope identifying the
  specific field(s) and reason (`es requerido` or `formato invalido`).
- **SC-003**: 100% of POST requests using an email that is already registered receive
  a `400` response with the "usuario existente" message.
- **SC-004**: No two registered users share the same `id` or the same `correo` value
  (compared case-insensitively), regardless of the number of registrations attempted.

## Clarifications

### Session 2026-05-05 (run 2)

- Q: What JSON structure should the 400 error response body use? → A: Structured envelope — `{"message": "Validation failed", "errors": [{"field": "<field>", "message": "<reason>"}]}`. For duplicate email the envelope is `{"message": "usuario existente", "errors": []}`.
- Q: Should duplicate email check be case-sensitive or case-insensitive? → A: Case-insensitive — `Juan@Example.com` and `juan@example.com` are treated as the same email.
- Q: Should `telefono` follow a specific format? → A: Exactly 10 digits, must start with `09` (e.g., `0912345678`).
- Q: Should missing-field errors and format errors be combined in one 400 response? → A: Yes — all validation failures (missing fields and format errors) are combined into a single `errors` array in one `400` response.

## Assumptions

- The service is consumed by developers/other services, not directly by end users
  through a browser UI.
- In-memory storage is sufficient for this version; no database or persistence layer
  is required.
- Authentication and authorization for the registration endpoint are out of scope for
  this feature.
- Email format validation follows standard RFC 5322 conventions (e.g., must contain
  `@` and a valid domain).
- Field length limits are not explicitly specified; standard reasonable limits apply
  (e.g., 255 characters per field).
- The API follows the project constitution: N-layer architecture, URI versioning
  (`/api/v1/`), AOP logging, and SOLID/DRY/YAGNI principles.
