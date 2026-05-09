package com.cat.user.service.unit;

import com.cat.user.service.dto.request.CreateUserRequest;
import com.cat.user.service.dto.response.UserResponse;
import com.cat.user.service.domain.User;
import com.cat.user.service.exceptions.UserAlreadyExistsException;
import com.cat.user.service.repository.UserRepository;
import com.cat.user.service.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // ── US1: Successful registration ──────────────────────────────────────────

    @Test
    void registerUser_withValidData_returnsUserResponse() {
        CreateUserRequest request = new CreateUserRequest(
                "Juan", "Perez", "Calle 1", "0912345678", "juan@example.com");
        when(userRepository.existsByCorreo("juan@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.registerUser(request);

        assertNotNull(response.id());
        assertFalse(response.id().isBlank());
        assertEquals("Juan", response.nombre());
        assertEquals("Perez", response.apellido());
        assertEquals("Calle 1", response.direccion());
        assertEquals("0912345678", response.telefono());
        assertEquals("juan@example.com", response.correo());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_normalizesEmailToLowercase() {
        CreateUserRequest request = new CreateUserRequest(
                "Juan", "Perez", "Calle 1", "0912345678", "JUAN@EXAMPLE.COM");
        when(userRepository.existsByCorreo("juan@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.registerUser(request);

        assertEquals("juan@example.com", response.correo());
        verify(userRepository).existsByCorreo("juan@example.com");
    }

    // ── US2: Duplicate email rejected ─────────────────────────────────────────

    @Test
    void registerUser_withDuplicateEmail_throwsUserAlreadyExistsException() {
        CreateUserRequest request = new CreateUserRequest(
                "Juan", "Perez", "Calle 1", "0912345678", "juan@example.com");
        when(userRepository.existsByCorreo("juan@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_withDuplicateEmailDifferentCase_throwsUserAlreadyExistsException() {
        CreateUserRequest request = new CreateUserRequest(
                "Juan", "Perez", "Calle 1", "0912345678", "JUAN@EXAMPLE.COM");
        when(userRepository.existsByCorreo("juan@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }
}
