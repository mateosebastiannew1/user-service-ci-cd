package com.cat.user.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "es requerido")
    private String nombre;

    @NotBlank(message = "es requerido")
    private String apellido;

    @NotBlank(message = "es requerido")
    private String direccion;

    @NotBlank(message = "es requerido")
    @Pattern(regexp = "09\\d{8}", message = "formato invalido")
    private String telefono;

    @NotBlank(message = "es requerido")
    @Email(message = "formato invalido")
    private String correo;
}
