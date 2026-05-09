package com.cat.user.service.service;

import com.cat.user.service.domain.User;
import com.cat.user.service.dto.request.CreateUserRequest;
import com.cat.user.service.dto.response.UserResponse;
import com.cat.user.service.exceptions.UserAlreadyExistsException;
import com.cat.user.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse registerUser(CreateUserRequest request) {
        String correoNormalizado = request.getCorreo().toLowerCase();

        if (userRepository.existsByCorreo(correoNormalizado)) {
            throw new UserAlreadyExistsException(correoNormalizado);
        }

        User user = new User(
                UUID.randomUUID(),
                request.getNombre(),
                request.getApellido(),
                request.getDireccion(),
                request.getTelefono(),
                correoNormalizado
        );

        userRepository.save(user);

        return new UserResponse(
                user.getId().toString(),
                user.getNombre(),
                user.getApellido(),
                user.getDireccion(),
                user.getTelefono(),
                user.getCorreo()
        );
    }
}
