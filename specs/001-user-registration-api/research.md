# Research: User Registration API

**Feature**: 001-user-registration-api
**Date**: 2026-05-05

## Decision Log

### Language & Runtime

**Decision**: Java 17
**Rationale**: LTS release, required baseline for Spring Boot 3.x. Widely used in
enterprise and educational contexts. Records and sealed classes available if needed
(YAGNI — not used speculatively).
**Alternatives considered**: Java 21 (LTS but less ubiquitous in existing toolchains
for coursework environments), Java 11 (previous LTS, incompatible with Spring Boot 3.x).

---

### Framework

**Decision**: Spring Boot 3.2.x
**Rationale**: Constitution references Spring-specific annotations (`@RestController`,
`@Aspect`, `@ControllerAdvice`, Spring Data) explicitly in the package conventions.
Spring Boot auto-configuration minimises boilerplate while allowing full control of
the N-layer structure required by the constitution.
**Alternatives considered**: Quarkus (cloud-native, faster startup, but less familiar
and no explicit mention in constitution), plain Spring MVC (more boilerplate, no benefit
for this scope).

---

### Build Tool

**Decision**: Maven 3.9+
**Rationale**: Standard in educational and enterprise Spring Boot projects. `pom.xml`
is widely understood, IDE support is universal, and dependency management is
deterministic via the Spring Boot BOM.
**Alternatives considered**: Gradle Kotlin DSL (more concise but higher learning curve
for teams unfamiliar with Kotlin DSL; no concrete advantage for this single-module service).

---

### Storage

**Decision**: In-memory `HashMap`-backed repository (no database)
**Rationale**: FR-004 explicitly states "no persistent storage required". Adding JPA +
H2 would violate YAGNI. The `InMemoryUserRepository` implementing a `UserRepository`
interface keeps the architecture compliant (repository layer abstraction honoured) while
using the simplest possible implementation.
**Alternatives considered**: H2 embedded DB with Spring Data JPA (over-engineered for
this spec; adds unnecessary dependencies and configuration).

---

### Validation Strategy

**Decision**: Bean Validation (`@NotBlank`, `@Pattern`, `@Email`) on the request DTO,
combined with a `@ControllerAdvice` that catches `MethodArgumentNotValidException` and
transforms it into the defined error envelope.
**Rationale**: Declarative validation keeps the DTO self-documenting; the global handler
(single place) produces the required structured response, honouring SRP (Single
Responsibility Principle). All field errors are accumulated by Bean Validation in one
pass, satisfying the "combine all errors in one response" requirement (FR-002).
**Alternatives considered**: Manual validation inside the service layer (violates SRP;
the service should not handle HTTP-level validation concerns; also bypasses the
Bean Validation accumulation of all errors).

---

### AOP Logging

**Decision**: Spring AOP with `@Aspect` + `@Around` advice on `controller` and
`service` packages.
**Rationale**: Constitution Principle IV mandates centralized logging. `@Around` advice
captures method entry, exit, arguments, and execution time in one pointcut per layer.
No `log.*` calls needed in business classes.
**Pattern**:
```
@Around("execution(* com.example.userservice.controller..*(..)) ||
         execution(* com.example.userservice.service..*(..))")
```
**Log format**: Logback with `logstash-logback-encoder` for structured JSON output,
including MDC-based correlation ID.
**Alternatives considered**: Manual logging in each method (violates Principle IV),
Spring `@Interceptor` (less powerful than AOP; cannot intercept service layer).

---

### API Documentation

**Decision**: `springdoc-openapi-starter-webmvc-ui` 2.x (generates OpenAPI 3.0 spec
and Swagger UI automatically from annotations).
**Rationale**: Constitution gate #5 requires API contract documented and in sync.
Springdoc generates the spec from code, eliminating drift between docs and
implementation.
**Alternatives considered**: Manually maintained YAML (error-prone, can drift from
implementation), Springfox (deprecated, incompatible with Spring Boot 3.x).

---

### Testing

**Decision**:
- **Unit tests**: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) —
  test service logic and validation rules with all dependencies mocked.
- **Functional tests**: `@SpringBootTest(webEnvironment = RANDOM_PORT)` +
  `TestRestTemplate` or `MockMvc` — exercises full HTTP stack end-to-end; in-memory
  store requires no external infrastructure.

**Rationale**: Constitution Principle III mandates both categories. In-memory storage
means functional tests need zero setup beyond starting the Spring context.
**Alternatives considered**: TestContainers (unnecessary — no real DB used),
WireMock (unnecessary — no external HTTP dependencies).

---

### Email Uniqueness Check

**Decision**: Normalise `correo` to lowercase before storage and lookup.
**Rationale**: Clarification Q2 confirmed case-insensitive comparison. Normalising at
write time (lowercase once on ingestion) is simpler and faster than case-insensitive
comparison on every lookup. Stored value retains the lowercase-normalised form.
**Alternatives considered**: Case-insensitive `HashMap` key (non-standard; requires
custom key wrapper — unnecessary complexity).
