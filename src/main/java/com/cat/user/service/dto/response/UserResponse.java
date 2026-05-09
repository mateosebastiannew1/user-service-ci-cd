package com.cat.user.service.dto.response;

public record UserResponse(
        String id,
        String nombre,
        String apellido,
        String direccion,
        String telefono,
        String correo
) {}
