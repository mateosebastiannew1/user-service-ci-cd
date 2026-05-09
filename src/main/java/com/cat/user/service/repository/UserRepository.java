package com.cat.user.service.repository;

import com.cat.user.service.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByCorreo(String correoLowercase);

    boolean existsByCorreo(String correoLowercase);
}
