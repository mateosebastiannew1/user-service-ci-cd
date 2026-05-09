package com.cat.user.service.repository;

import com.cat.user.service.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> storage = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        storage.put(user.getCorreo(), user);
        return user;
    }

    @Override
    public Optional<User> findByCorreo(String correoLowercase) {
        return Optional.ofNullable(storage.get(correoLowercase));
    }

    @Override
    public boolean existsByCorreo(String correoLowercase) {
        return storage.containsKey(correoLowercase);
    }

    public void clearAll() {
        storage.clear();
    }
}
