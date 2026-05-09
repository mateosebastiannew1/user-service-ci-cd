---

description: "Task list for User Registration API"
---

# Tasks: User Registration API

**Input**: Design documents from `specs/001-user-registration-api/`
**Prerequisites**: plan.md ✅ | spec.md ✅ | data-model.md ✅ | contracts/ ✅ | research.md ✅

**Tests**: Unit tests and functional tests are MANDATORY per constitution v1.0.0.
Every feature MUST include both unit test tasks (service/domain layer, 80% coverage)
and functional test tasks (full HTTP stack). Do not omit these tasks.

**Organization**: Tasks are grouped by user story to enable independent implementation
and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions (N-Layer Architecture — constitution v1.0.0)

- **Main source**: `src/main/java/com/example/userservice/`
  - `controller/` — versioned REST controllers (`/api/v1/`)
  - `service/` — service interfaces + implementations
  - `domain/` — entities, value objects (no framework deps)
  - `repository/` — repository interface + InMemoryUserRepository
  - `dto/request/` — input DTOs
  - `dto/response/` — output DTOs
  - `exceptions/` — custom exceptions + global handler
  - `aspect/` — AOP `LoggingAspect`
- **Tests**: `src/test/java/com/example/userservice/`
  - `unit/` — unit tests (`@ExtendWith(MockitoExtension.class)`)
  - `functional/` — functional tests (`@SpringBootTest`)

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Create the Maven Spring Boot project scaffold and package structure.

- [x] T001 Initialize Maven Spring Boot project in repository root: create `pom.xml` with dependencies spring-boot-starter-web, spring-boot-starter-validation, spring-boot-starter-aop, springdoc-openapi-starter-webmvc-ui 2.x, spring-boot-starter-test, and JaCoCo Maven plugin
- [x] T002 Create `src/main/java/com/example/userservice/UserServiceApplication.java` — Spring Boot main class with `@SpringBootApplication`
- [x] T003 [P] Create package directories: `controller/`, `service/`, `domain/`, `repository/`, `dto/request/`, `dto/response/`, `exceptions/`, `aspect/` under `src/main/java/com/example/userservice/`
- [x] T004 [P] Create `src/main/resources/application.properties` with `server.port=8080` and `spring.application.name=user-service`
- [x] T005 [P] Create `src/main/resources/logback-spring.xml` configuring structured JSON output via logstash-logback-encoder with MDC correlation ID support
- [x] T006 [P] Create test package directories `unit/` and `functional/` under `src/test/java/com/example/userservice/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared infrastructure classes that ALL user stories depend on. No user
story implementation can begin until this phase is complete.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T007 Create `src/main/java/com/example/userservice/domain/User.java` — plain Java class (no Spring/JPA annotations) with fields: `UUID id`, `String nombre`, `String apellido`, `String direccion`, `String telefono`, `String correo`; include all-args constructor and getters (no setters — immutable after creation)
- [x] T008 [P] Create `src/main/java/com/example/userservice/dto/request/CreateUserRequest.java` — Java record or class with fields `nombre`, `apellido`, `direccion`, `telefono`, `correo` (no validation annotations yet — added in US3 phase)
- [x] T009 [P] Create `src/main/java/com/example/userservice/dto/response/UserResponse.java` — Java record or class with fields `id` (String), `nombre`, `apellido`, `direccion`, `telefono`, `correo`
- [x] T010 [P] Create `src/main/java/com/example/userservice/dto/response/ErrorResponse.java` with fields `String message` and `List<FieldError> errors`; create nested (or separate) `src/main/java/com/example/userservice/dto/response/FieldError.java` with fields `String field` and `String message`
- [x] T011 [P] Create `src/main/java/com/example/userservice/repository/UserRepository.java` — interface with methods: `User save(User user)`, `Optional<User> findByCorreo(String correoLowercase)`, `boolean existsByCorreo(String correoLowercase)`
- [x] T012 Create `src/main/java/com/example/userservice/repository/InMemoryUserRepository.java` — implements `UserRepository` using `ConcurrentHashMap<String, User>` keyed on lowercased `correo`; annotated with `@Repository`; depends on T011
- [x] T013 [P] Create `src/main/java/com/example/userservice/exceptions/UserAlreadyExistsException.java` — extends `RuntimeException` with constructor accepting the duplicate email value
- [x] T014 [P] Create `src/main/java/com/example/userservice/aspect/LoggingAspect.java` — `@Aspect @Component` class with `@Around` pointcut targeting `com.example.userservice.controller..*` and `com.example.userservice.service..*`; logs method entry (args), exit (result), and execution time in ms at DEBUG level; MUST NOT use `log.*` in any other class

**Checkpoint**: Foundation ready — all shared classes exist. User story implementation can now begin.

---

## Phase 3: User Story 1 — Successful User Registration (Priority: P1) 🎯 MVP

**Goal**: `POST /api/v1/users` with a complete, valid payload returns `201 Created`
with all five user fields plus an auto-generated UUID `id`.

**Independent Test**: Start the service and `POST` a valid payload — verify HTTP `201`
and response body contains the five fields plus a UUID `id`. No errors or external
services required.

### Tests for User Story 1 (MANDATORY — constitution v1.0.0) ✅

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation (T017, T018)**

- [x] T015 [P] [US1] Create `src/test/java/com/example/userservice/unit/UserServiceImplTest.java` with `@ExtendWith(MockitoExtension.class)`; add test `registerUser_withValidData_returnsUserResponse` — mock `UserRepository`, call `registerUser(request)`, assert returned `UserResponse` has non-null UUID and all five fields; verify `repository.save()` is called once
- [x] T016 [P] [US1] Create `src/test/java/com/example/userservice/functional/UserControllerFunctionalTest.java` with `@SpringBootTest(webEnvironment = RANDOM_PORT)` and `MockMvc`; add test `postUser_withValidPayload_returns201` — POST valid JSON to `/api/v1/users`, assert status `201` and response body contains `id`, `nombre`, `apellido`, `direccion`, `telefono`, `correo`

### Implementation for User Story 1

- [x] T017 [US1] Create `src/main/java/com/example/userservice/service/UserService.java` — interface with single method `UserResponse registerUser(CreateUserRequest request)`
- [x] T018 [US1] Create `src/main/java/com/example/userservice/service/UserServiceImpl.java` — `@Service` implementing `UserService`; constructor-injects `UserRepository`; generates `UUID.randomUUID()`, normalises `correo` to lowercase, constructs `User`, calls `repository.save()`, maps to `UserResponse`; NO `log.*` calls (logging handled by `LoggingAspect`); depends on T007–T012, T017
- [x] T019 [US1] Create `src/main/java/com/example/userservice/controller/UserController.java` — `@RestController @RequestMapping("/api/v1/users")`; constructor-injects `UserService`; `@PostMapping` method `registerUser(@RequestBody CreateUserRequest request)` calls `userService.registerUser(request)` and returns `ResponseEntity.status(201).body(response)`; NO `log.*` calls; depends on T017, T018
- [x] T020 [US1] Run unit test T015 and functional test T016 — both MUST now pass; verify `201` response and UUID format in response body

**Checkpoint**: User Story 1 is fully functional and independently testable. `POST /api/v1/users` with valid data returns `201` with UUID.

---

## Phase 4: User Story 2 — Duplicate Email Rejected (Priority: P2)

**Goal**: `POST /api/v1/users` with an already-registered email (case-insensitive)
returns `400` with `{"message":"usuario existente","errors":[]}`.

**Independent Test**: Register a user, then POST again with the same email (try also
with different casing) — verify HTTP `400` and the exact envelope body.

### Tests for User Story 2 (MANDATORY — constitution v1.0.0) ✅

- [x] T021 [P] [US2] In `UserServiceImplTest.java`: add test `registerUser_withDuplicateEmail_throwsUserAlreadyExistsException` — configure mock `repository.existsByCorreo()` to return `true`, call `registerUser(request)`, assert `UserAlreadyExistsException` is thrown
- [x] T022 [P] [US2] In `UserControllerFunctionalTest.java`: add test `postUser_withDuplicateEmail_returns400` — register a user first, then POST same email; assert status `400`, body has `message:"usuario existente"` and `errors:[]`; add test `postUser_withDuplicateEmailDifferentCase_returns400` — POST same email in UPPERCASE; assert same `400` response

### Implementation for User Story 2

- [x] T023 [US2] Update `UserServiceImpl.registerUser()`: before `repository.save()`, call `repository.existsByCorreo(correoLowercase)`; if true, throw `UserAlreadyExistsException`; depends on T013, T018
- [x] T024 [US2] Create `src/main/java/com/example/userservice/exceptions/GlobalExceptionHandler.java` — `@RestControllerAdvice`; add `@ExceptionHandler(UserAlreadyExistsException.class)` method that returns `ResponseEntity` with status `400` and body `ErrorResponse("usuario existente", emptyList())`
- [x] T025 [US2] Run tests T021 and T022 — both MUST pass; verify case-insensitive scenario (`JUAN@EXAMPLE.COM` rejected when `juan@example.com` is registered)

**Checkpoint**: User Stories 1 AND 2 work independently. Duplicate email returns `400 {"message":"usuario existente","errors":[]}`.

---

## Phase 5: User Story 3 — Missing Required Fields Rejected (Priority: P3)

**Goal**: `POST /api/v1/users` with missing or invalid fields returns `400` with all
field errors combined in a single structured envelope.

**Independent Test**: POST with `nombre` omitted → `400` with `errors:[{field:"nombre",message:"es requerido"}]`.
POST with invalid `telefono` format → `400` with `errors:[{field:"telefono",message:"formato invalido"}]`.
POST with both issues → `400` with both errors in one response.

### Tests for User Story 3 (MANDATORY — constitution v1.0.0) ✅

- [x] T026 [P] [US3] In `UserControllerFunctionalTest.java`: add test `postUser_withMissingNombre_returns400WithFieldError` — POST without `nombre`, assert `400`, body has `message:"Validation failed"` and `errors` contains `{field:"nombre",message:"es requerido"}`
- [x] T027 [P] [US3] In `UserControllerFunctionalTest.java`: add test `postUser_withInvalidTelefono_returns400WithFormatError` — POST with `telefono:"12345"`, assert `400`, body has `errors` containing `{field:"telefono",message:"formato invalido"}`
- [x] T028 [P] [US3] In `UserControllerFunctionalTest.java`: add test `postUser_withMultipleErrors_returns400WithAllErrors` — POST with `nombre` missing AND invalid `telefono`; assert `400`, body has both error entries in `errors` array in one single response

### Implementation for User Story 3

- [x] T029 [US3] Add Bean Validation annotations to `CreateUserRequest.java`: `@NotBlank(message="es requerido")` on all five fields; `@Pattern(regexp="09\\d{8}", message="formato invalido")` on `telefono`; `@Email(message="formato invalido")` on `correo`; depends on T008
- [x] T030 [US3] Update `UserController.registerUser()`: add `@Valid` annotation to the `@RequestBody CreateUserRequest` parameter to activate Bean Validation; depends on T019, T029
- [x] T031 [US3] Extend `GlobalExceptionHandler`: add `@ExceptionHandler(MethodArgumentNotValidException.class)` method that collects ALL `FieldError` entries from the `BindingResult`, maps each to `FieldError(fieldName, defaultMessage)`, and returns `ResponseEntity` `400` with body `ErrorResponse("Validation failed", fieldErrors)`; depends on T024
- [x] T032 [US3] Run tests T026, T027, T028 — all MUST pass; verify combined-error scenario returns all errors in a single `400` response

**Checkpoint**: All three user stories are fully functional and independently testable. Full validation pipeline operational.

---

## Final Phase: Polish & Cross-Cutting Concerns

**Purpose**: Quality gates, documentation, and verification that cross-cutting concerns work correctly.

- [x] T033 [P] Add `springdoc-openapi` annotations to `UserController.java`: `@Tag(name="Users")`, `@Operation(summary="Register a new user")`, `@ApiResponse` for `201` and `400`; verify Swagger UI at `http://localhost:8080/swagger-ui.html` matches `contracts/openapi.yml`
- [x] T034 [P] Configure JaCoCo Maven plugin in `pom.xml`: add `<rule>` enforcing 80% line coverage on `com.example.userservice.service.*` and `com.example.userservice.domain.*`; build MUST fail if gate not met
- [x] T035 [P] Verify `LoggingAspect` is working: run service, make a POST request, and confirm structured JSON log entries appear for both controller and service method entry/exit with execution time; confirm ZERO `log.*` calls exist in `service/`, `domain/`, `repository/` packages (grep check)
- [x] T036 Run full test suite: `mvn verify`; assert all unit and functional tests pass, JaCoCo coverage gate met; fix any failures before proceeding
- [x] T037 [P] Validate all quickstart.md scenarios manually against the running service (scenarios 1–5); document any discrepancies

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — **BLOCKS all user stories**
- **User Story 1 (Phase 3)**: Depends on Phase 2 completion
- **User Story 2 (Phase 4)**: Depends on Phase 3 completion (extends UserServiceImpl and GlobalExceptionHandler)
- **User Story 3 (Phase 5)**: Depends on Phase 4 completion (extends GlobalExceptionHandler for validation)
- **Polish (Final Phase)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational — no story dependencies
- **US2 (P2)**: Depends on US1 completion — adds duplicate check to the same service + handler
- **US3 (P3)**: Depends on US2 completion — extends the same handler with Bean Validation mapping

### Within Each User Story

- Tests (T015/T016, T021/T022, T026/T027/T028) MUST be written BEFORE implementation tasks
- Tests MUST FAIL before implementation begins (Red phase)
- Implement until tests pass (Green phase)
- Each story MUST be independently demonstrable before moving to the next

### Parallel Opportunities

**Phase 1 — can run in parallel**: T003, T004, T005, T006
**Phase 2 — can run in parallel**: T008, T009, T010, T011, T013, T014 (T012 needs T011)
**US1 tests — can run in parallel**: T015, T016
**US2 tests — can run in parallel**: T021, T022
**US3 tests — can run in parallel**: T026, T027, T028
**Final phase — can run in parallel**: T033, T034, T035, T037

---

## Parallel Execution Examples

### Phase 2 — Foundational (launch together after T007)

```
Task: "Create CreateUserRequest DTO in src/main/java/.../dto/request/CreateUserRequest.java"          → T008
Task: "Create UserResponse DTO in src/main/java/.../dto/response/UserResponse.java"                   → T009
Task: "Create ErrorResponse + FieldError DTOs in src/main/java/.../dto/response/"                     → T010
Task: "Create UserRepository interface in src/main/java/.../repository/UserRepository.java"           → T011
Task: "Create UserAlreadyExistsException in src/main/java/.../exceptions/"                            → T013
Task: "Create LoggingAspect in src/main/java/.../aspect/LoggingAspect.java"                           → T014
```

### User Story 1 — Tests (launch together before implementation)

```
Task: "Write UserServiceImplTest.java — happy path unit test"      → T015
Task: "Write UserControllerFunctionalTest.java — 201 scenario"     → T016
```

### User Story 3 — Tests (launch together before implementation)

```
Task: "Add missing-field test to UserControllerFunctionalTest"     → T026
Task: "Add invalid-telefono test to UserControllerFunctionalTest"  → T027
Task: "Add combined-errors test to UserControllerFunctionalTest"   → T028
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (**CRITICAL** — blocks all stories)
3. Write US1 tests (T015, T016) — verify they FAIL
4. Complete Phase 3: User Story 1
5. **STOP and VALIDATE**: Run `mvn test`, verify US1 tests pass, POST valid payload returns `201`
6. Demo/review if needed

### Incremental Delivery

1. Setup + Foundational → scaffold ready
2. US1 → `201` happy path works → validate → demo
3. US2 → `400` duplicate email works → validate → demo
4. US3 → `400` validation errors work → validate → demo
5. Polish → coverage gate + Swagger + AOP verification → merge ready

### Parallel Team Strategy

With the shared endpoint, parallelism is at the **test-writing** level within each story:

- While one developer writes service implementation, another writes functional tests
- Foundational tasks T008–T014 can be split across developers (all independent files)

---

## Notes

- `[P]` tasks operate on different files — no merge conflicts expected
- `[Story]` label maps each task to its user story for traceability
- Tests for each story use the **same two test files** (`UserServiceImplTest` and
  `UserControllerFunctionalTest`) — add new test methods, do not create new files
- LoggingAspect covers controller + service automatically — verify with a single
  integration smoke test, not per-story
- `correo` is normalised to lowercase at `UserServiceImpl` level before any storage
  or comparison — this is the single place where normalisation happens (DRY)
- Never add `log.*` calls in `service/`, `domain/`, `repository/` — constitution gate #4
