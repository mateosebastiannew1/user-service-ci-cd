<!--
SYNC IMPACT REPORT
==================
Version change: (unversioned template) → 1.0.0
Added sections:
  - I. N-Layer Architecture (NON-NEGOTIABLE)
  - II. API Versioning Best Practices (NON-NEGOTIABLE)
  - III. Testing Strategy — Unit & Functional (NON-NEGOTIABLE)
  - IV. Centralized Logging via AOP (NON-NEGOTIABLE)
  - V. Clean Code Principles — SOLID, DRY, YAGNI (NON-NEGOTIABLE)
  - Layer Structure & Package Conventions
  - Development Workflow & Quality Gates
  - Governance
Removed sections: None (initial population of template)
Modified principles: N/A (first version)
Templates updated:
  - .specify/templates/plan-template.md ✅ updated
  - .specify/templates/tasks-template.md ✅ updated
  - .specify/templates/spec-template.md — no structural change required ✅
Deferred TODOs: None
-->

# User Service Constitution

## Core Principles

### I. N-Layer Architecture (NON-NEGOTIABLE)

Every feature MUST be implemented across exactly the following layers, each in its own
package, with no cross-layer leakage of responsibilities:

- **controller** — HTTP entry point only. Validates HTTP input, delegates to service,
  maps results to HTTP responses. MUST NOT contain business logic.
- **service** — Orchestrates business use cases. MUST NOT access repositories directly
  from controllers, and MUST NOT contain persistence or HTTP concerns.
- **domain** — Pure business entities and value objects. MUST NOT depend on any
  framework, persistence, or transport layer. No annotations that tie it to JPA/Spring.
- **repository** — Persistence abstraction. Interfaces defined in domain or service;
  implementations live here. MUST NOT contain business logic.
- **dto** — Data Transfer Objects for request/response boundaries. MUST NOT be reused
  as domain entities. Separate request and response DTOs are required.
- **exceptions** — Custom typed exceptions for each error category (e.g.,
  `ResourceNotFoundException`, `BusinessRuleException`). MUST be caught and handled by
  a global exception handler — never swallowed silently.

**Rationale**: Separation of concerns enables independent testability of each layer,
prevents coupling between transport and persistence, and allows the team to evolve
layers without cascading changes.

### II. API Versioning Best Practices (NON-NEGOTIABLE)

All REST endpoints MUST follow URI-path versioning:

- Base path pattern: `/api/v{N}/...` (e.g., `/api/v1/users`)
- Version MUST be a positive integer; no minor/patch segments in the URI.
- A new version MUST be introduced whenever a breaking change is made (field removal,
  type change, semantic change). Non-breaking additions (new optional fields) MAY stay
  in the existing version.
- Deprecated versions MUST remain operational for at least one release cycle and MUST
  return a `Deprecation` response header.
- API contracts (request/response schemas) MUST be documented (e.g., OpenAPI/Swagger)
  and kept in sync with implementation.

**Rationale**: Explicit versioning protects consumers from breaking changes and makes
the evolution of the service predictable and auditable.

### III. Testing Strategy — Unit & Functional (NON-NEGOTIABLE)

Two mandatory test categories MUST be present for every feature:

**Unit Tests**
- MUST cover all service-layer logic, domain entities, and utility classes.
- MUST mock all external dependencies (repositories, external services).
- Naming convention: `[ClassName]Test` in `src/test/.../unit/`.
- Coverage gate: 80 % line coverage minimum on service and domain layers.

**Functional Tests** (end-to-end / integration)
- MUST exercise the full HTTP stack from controller through repository.
- MUST use an embedded/in-memory data store or TestContainers — no shared databases.
- Naming convention: `[ClassName]FunctionalTest` in `src/test/.../functional/`.
- MUST test happy path, validation errors, and not-found scenarios for every endpoint.

Tests MUST be written before or alongside implementation (no retroactive test skipping).
CI pipeline MUST fail if any test fails or coverage gate is not met.

**Rationale**: Unit tests validate isolated logic quickly; functional tests validate
the assembled system against real HTTP contracts, catching integration issues early.

### IV. Centralized Logging via AOP (NON-NEGOTIABLE)

All operational logging MUST be performed through Aspect-Oriented Programming (AOP):

- A dedicated logging aspect (e.g., `LoggingAspect`) MUST intercept service and
  controller method boundaries to log entry, exit, arguments, and execution time.
- Business logic classes (services, domain, repositories) MUST NOT contain any explicit
  `log.info`, `log.debug`, or `log.error` calls except for truly exceptional error
  scenarios not catchable by the aspect (which MUST be documented in a code comment).
- Log format MUST be structured (JSON) and include: timestamp, level, correlation/trace
  ID, class, method, and relevant payload summary (no PII in logs).
- Log levels: DEBUG for method traces, INFO for significant business events (via aspect),
  WARN for recoverable issues, ERROR for unhandled exceptions.

**Rationale**: Centralizing logging in aspects keeps business code readable, ensures
consistent log formatting, and allows cross-cutting log policy changes without touching
business logic.

### V. Clean Code Principles — SOLID, DRY, YAGNI (NON-NEGOTIABLE)

All implementation MUST comply with the following:

**SOLID**
- **S** — Each class has exactly one reason to change.
- **O** — Extend behavior via new classes/interfaces, not by modifying existing ones.
- **L** — Subtypes MUST be substitutable for their base types without altering correctness.
- **I** — Interfaces MUST be narrow and client-specific; no fat interfaces.
- **D** — High-level modules MUST depend on abstractions, not on concrete implementations.
  All dependencies MUST be injected (constructor injection preferred).

**DRY** — Duplication of logic is forbidden. Common behavior MUST be extracted into
shared utilities, base classes, or aspects before a second use.

**YAGNI** — Features, abstractions, or configurations MUST NOT be added speculatively.
Every line of code MUST be traceable to a current, concrete requirement.

**Rationale**: These principles reduce accidental complexity, lower maintenance cost,
and produce a codebase where each component can be understood, tested, and changed in
isolation.

## Layer Structure & Package Conventions

The canonical package structure for this service is:

```
com.example.userservice
├── controller/        # @RestController classes, versioned under /api/v{N}/
├── service/           # @Service interfaces + implementations
├── domain/            # Entities, value objects, domain exceptions
├── repository/        # Spring Data interfaces / custom repository impls
├── dto/
│   ├── request/       # Input DTOs (e.g., CreateUserRequest)
│   └── response/      # Output DTOs (e.g., UserResponse)
├── exceptions/        # Custom exception classes + @ControllerAdvice handler
└── aspect/            # AOP logging aspects (@Aspect)
```

Test mirror structure:

```
src/test/
├── unit/              # Unit tests — mocked dependencies
└── functional/        # Functional/integration tests — full stack
```

**Rules**:
- Controllers MUST NOT return domain entities — only DTOs.
- Domain entities MUST NOT implement `Serializable` unless strictly required for caching.
- Repository interfaces MUST be defined in the `repository` package and MUST NOT be
  autowired directly into controllers.

## Development Workflow & Quality Gates

The following gates are NON-NEGOTIABLE before merging any feature branch:

1. **Architecture compliance** — Verify no layer bypasses (e.g., controller calling
   repository directly). Use ArchUnit or equivalent static check.
2. **All tests green** — Both unit and functional test suites MUST pass.
3. **Coverage gate met** — 80 % minimum on service + domain layers.
4. **No business-layer log statements** — Code review MUST catch any `log.*` calls
   outside `aspect/` or documented exceptional cases.
5. **API contract documented** — OpenAPI spec updated before merge.
6. **No unversioned endpoints** — Every new endpoint MUST start under `/api/v{N}/`.
7. **SOLID/DRY/YAGNI review** — PR description MUST include a one-line justification
   for any new abstraction introduced.

## Governance

This constitution supersedes all other team conventions, README guidance, and informal
agreements. Any conflict MUST be resolved in favor of the constitution.

**Amendment procedure**:
1. Propose change via pull request to `.specify/memory/constitution.md`.
2. State the principle being amended, the rationale, and any migration plan for
   existing code.
3. Requires explicit approval from project lead before merge.
4. After merge, update `LAST_AMENDED_DATE` and increment `CONSTITUTION_VERSION`
   following semantic versioning (MAJOR for removals/redefinitions, MINOR for
   additions, PATCH for clarifications).

**Compliance review**: Every sprint retrospective MUST include a brief check of
constitution adherence. Violations discovered in review MUST be filed as tech-debt
tasks and resolved within the next sprint.

**Versioning policy**: `CONSTITUTION_VERSION` follows semver. The version line below
is the single source of truth.

**Version**: 1.0.0 | **Ratified**: 2026-05-05 | **Last Amended**: 2026-05-05
