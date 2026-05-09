package com.cat.user.service.domain;

import java.util.UUID;

public class User {

    private final UUID id;
    private final String nombre;
    private final String apellido;
    private final String direccion;
    private final String telefono;
    private final String correo;

    public User(UUID id, String nombre, String apellido, String direccion,
                String telefono, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
}
