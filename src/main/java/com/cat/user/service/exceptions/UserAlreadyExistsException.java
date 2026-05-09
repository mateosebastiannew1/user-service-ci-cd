package com.cat.user.service.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String correo) {
        super("User already exists with email: " + correo);
    }
}
