package com.cat.user.service.functional;

import com.cat.user.service.repository.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserControllerFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.clearAll();
    }

    // ── US1: Successful registration ──────────────────────────────────────────

    @Test
    void postUser_withValidPayload_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Juan",
                                  "apellido": "Perez",
                                  "direccion": "Calle 1 #23-45",
                                  "telefono": "0912345678",
                                  "correo": "juan@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.apellido").value("Perez"))
                .andExpect(jsonPath("$.direccion").value("Calle 1 #23-45"))
                .andExpect(jsonPath("$.telefono").value("0912345678"))
                .andExpect(jsonPath("$.correo").value("juan@example.com"));
    }

    @Test
    void postUser_withValidPayload_idIsUuidFormat() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Pedro",
                                  "apellido": "Lopez",
                                  "direccion": "Calle 2",
                                  "telefono": "0987654321",
                                  "correo": "pedro@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",
                        matchesPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
    }

    // ── US2: Duplicate email rejected ─────────────────────────────────────────

    @Test
    void postUser_withDuplicateEmail_returns400() throws Exception {
        String validPayload = """
                {
                  "nombre": "Juan",
                  "apellido": "Perez",
                  "direccion": "Calle 1",
                  "telefono": "0912345678",
                  "correo": "juan@example.com"
                }
                """;
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("usuario existente"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    void postUser_withDuplicateEmailDifferentCase_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Juan",
                                  "apellido": "Perez",
                                  "direccion": "Calle 1",
                                  "telefono": "0912345678",
                                  "correo": "juan@example.com"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Juan",
                                  "apellido": "Perez",
                                  "direccion": "Calle 2",
                                  "telefono": "0987654321",
                                  "correo": "JUAN@EXAMPLE.COM"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("usuario existente"));
    }

    // ── US3: Missing required fields rejected ─────────────────────────────────

    @Test
    void postUser_withMissingNombre_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "apellido": "Perez",
                                  "direccion": "Calle 1",
                                  "telefono": "0912345678",
                                  "correo": "juan@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@.field == 'nombre')].message",
                        hasItem("es requerido")));
    }

    @Test
    void postUser_withMissingCorreo_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Juan",
                                  "apellido": "Perez",
                                  "direccion": "Calle 1",
                                  "telefono": "0912345678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@.field == 'correo')].message",
                        hasItem("es requerido")));
    }

    @Test
    void postUser_withInvalidTelefono_returns400WithFormatError() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Juan",
                                  "apellido": "Perez",
                                  "direccion": "Calle 1",
                                  "telefono": "12345",
                                  "correo": "juan@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@.field == 'telefono')].message",
                        hasItem("formato invalido")));
    }

    @Test
    void postUser_withMultipleErrors_returns400WithAllErrors() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "apellido": "Perez",
                                  "direccion": "Calle 1",
                                  "telefono": "12345",
                                  "correo": "juan@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@.field == 'nombre')].message",
                        hasItem("es requerido")))
                .andExpect(jsonPath("$.errors[?(@.field == 'telefono')].message",
                        hasItem("formato invalido")))
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void postUser_withBlankFields_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "  ",
                                  "apellido": "Perez",
                                  "direccion": "Calle 1",
                                  "telefono": "0912345678",
                                  "correo": "juan@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
