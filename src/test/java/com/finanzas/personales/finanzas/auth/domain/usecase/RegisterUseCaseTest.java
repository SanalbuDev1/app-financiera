package com.finanzas.personales.finanzas.auth.domain.usecase;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import com.finanzas.personales.finanzas.auth.domain.model.UserRole;
import com.finanzas.personales.finanzas.auth.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link RegisterUseCase}.
 * Mockea el repositorio y el encoder para aislar la lógica del caso de uso.
 */
@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUseCase registerUseCase;

    /**
     * Verifica que el caso de uso crea un usuario con rol USER
     * cuando el email no está registrado previamente.
     */
    @Test
    void should_createUser_when_emailIsNew() {
        // Arrange
        when(userRepositoryPort.existsByEmail("nuevo@financiera.com")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$10$hashedPass");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act & Assert
        StepVerifier.create(registerUseCase.execute("nuevo@financiera.com", "pass123", "Nuevo Usuario"))
                .expectNextMatches(user ->
                        user.getEmail().equals("nuevo@financiera.com")
                        && user.getName().equals("Nuevo Usuario")
                        && user.getRole() == UserRole.USER
                        && user.getPassword().equals("$2a$10$hashedPass")
                )
                .verifyComplete();
    }

    /**
     * Verifica que se lanza {@link EmailAlreadyExistsException} cuando el email ya existe.
     */
    @Test
    void should_throwEmailAlreadyExistsException_when_emailAlreadyRegistered() {
        // Arrange — el email ya está en la base de datos
        when(userRepositoryPort.existsByEmail("admin@financiera.com")).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(registerUseCase.execute("admin@financiera.com", "pass123", "Admin"))
                .expectError(EmailAlreadyExistsException.class)
                .verify();
    }
}
