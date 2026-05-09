# Implementation Plan: User Registration API

**Branch**: `001-user-registration-api` | **Date**: 2026-05-05 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/001-user-registration-api/spec.md`

## Summary

Implement a REST endpoint `POST /api/v1/users` that registers a new user (first name,
last name, address, phone, email) in an in-memory store and returns the created record
with an auto-generated UUID. The service enforces field presence, `telefono` format
(`09XXXXXXXX`), email format, and case-insensitive email uniqueness. All validation
errors are returned together in a structured JSON envelope. The implementation follows
the N-layer architecture mandated by the project constitution and uses AOP for
centralised logging.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.2.x, spring-boot-starter-web,
spring-boot-starter-validation, spring-boot-starter-aop,
springdoc-openapi-starter-webmvc-ui 2.x, Lombok (optional)
**Storage**: In-memory `ConcurrentHashMap` (no database — FR-004)
**Testing**: JUnit 5 + Mockito (unit), Spring Boot Test / MockMvc (functional)
**Target Platform**: JVM / local development server (port 8080)
**Project Type**: Spring Boot web service (single module, Maven)
**Performance Goals**: No specific targets — in-memory, single node; SC-001 requires
response verifiable in under 5 seconds
**Constraints**: In-memory only; no persistence across restarts; no auth required
**Scale/Scope**: Single endpoint; single in-memory store; no concurrent-write
correctness required beyond basic thread-safety (`ConcurrentHashMap`)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **N-Layer Architecture**: controller / service / domain / repository / dto /
  exceptions / aspect — all planned with no cross-layer leakage. Controllers return
  DTOs only. Domain entity carries no framework annotations.
- [x] **API Versioning**: Single endpoint at `/api/v1/users`. No breaking changes in
  this version.
- [x] **Testing**: Unit tests planned for `UserServiceImpl` and domain validation;
  functional tests planned for `UserController` via `@SpringBootTest` + MockMvc.
  80% coverage gate enforced by JaCoCo.
- [x] **AOP Logging**: `LoggingAspect` intercepts `controller` and `service` packages.
  Zero `log.*` calls in service, domain, or repository classes.
- [x] **SOLID/DRY/YAGNI**: Constructor injection throughout. `UserRepository` interface
  satisfies DIP. `InMemoryUserRepository` is the only implementation — no speculative
  abstraction. Single `GlobalExceptionHandler` handles all error mapping (SRP).

**Result**: All gates PASS. No violations to justify.

## Project Structure

### Documentation (this feature)

```text
specs/001-user-registration-api/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── openapi.yml      # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

```text
src/main/java/com/example/userservice/
├── UserServiceApplication.java          # Spring Boot entry point
├── controller/
│   └── UserController.java              # POST /api/v1/users → HTTP 201 / 400
├── service/
│   ├── UserService.java                 # Interface: registerUser(CreateUserRequest)
│   └── UserServiceImpl.java             # Impl: validates uniqueness, calls repository
├── domain/
│   └── User.java                        # Plain domain entity — no framework deps
├── repository/
│   ├── UserRepository.java              # Interface: save, findByCorreo, existsByCorreo
│   └── InMemoryUserRepository.java      # ConcurrentHashMap-backed impl
├── dto/
│   ├── request/
│   │   └── CreateUserRequest.java       # @NotBlank, @Pattern, @Email annotations
│   └── response/
│       ├── UserResponse.java            # 201 response DTO
│       └── ErrorResponse.java           # 400 response envelope
├── exceptions/
│   ├── UserAlreadyExistsException.java  # Thrown when correo duplicate detected
│   └── GlobalExceptionHandler.java      # @ControllerAdvice — maps exceptions → ErrorResponse
└── aspect/
    └── LoggingAspect.java               # @Aspect @Around — controller + service logging

src/main/resources/
├── application.properties               # Server port, logging config
└── logback-spring.xml                   # Structured JSON logging (Logstash encoder)

src/test/java/com/example/userservice/
├── unit/
│   └── UserServiceImplTest.java         # JUnit 5 + Mockito — service logic
└── functional/
    └── UserControllerFunctionalTest.java # @SpringBootTest + MockMvc — full HTTP stack

pom.xml                                  # Maven build descriptor
```

**Structure Decision**: Single Spring Boot module (Option 1). No multi-module setup —
YAGNI: this is a single-feature service with no concrete requirement for module
separation.

## Complexity Tracking

> No constitution violations. Table not required.
