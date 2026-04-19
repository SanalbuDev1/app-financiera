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

import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link LoginUseCase}.
 * Mockea el repositorio y el encoder para aislar la lógica del caso de uso.
 */
@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginUseCase loginUseCase;

    /**
     * Verifica que el caso de uso retorna el usuario cuando las credenciales son válidas.
     */
    @Test
    void should_returnUser_when_credentialsAreValid() {
        // Arrange
        User mockUser = User.builder()
                .id("uuid-1")
                .email("user@financiera.com")
                .name("Usuario")
                .password("$2a$10$hashedPassword")
                .role(UserRole.USER)
                .build();

        when(userRepositoryPort.findByEmail("user@financiera.com")).thenReturn(Mono.just(mockUser));
        when(passwordEncoder.matches("user123", "$2a$10$hashedPassword")).thenReturn(true);

        // Act & Assert
        StepVerifier.create(loginUseCase.execute("user@financiera.com", "user123"))
                .expectNextMatches(user -> user.getEmail().equals("user@financiera.com")
                        && user.getRole() == UserRole.USER)
                .verifyComplete();
    }

    /**
     * Verifica que se lanza {@link UnauthorizedException} cuando la contraseña es incorrecta.
     */
    @Test
    void should_throwUnauthorizedException_when_passwordIsWrong() {
        // Arrange
        User mockUser = User.builder()
                .id("uuid-1")
                .email("user@financiera.com")
                .name("Usuario")
                .password("$2a$10$hashedPassword")
                .role(UserRole.USER)
                .build();

        when(userRepositoryPort.findByEmail("user@financiera.com")).thenReturn(Mono.just(mockUser));
        // El encoder devuelve false — contraseña incorrecta
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        // Act & Assert
        StepVerifier.create(loginUseCase.execute("user@financiera.com", "wrongPassword"))
                .expectError(UnauthorizedException.class)
                .verify();
    }

    /**
     * Verifica que se lanza {@link UnauthorizedException} cuando el email no existe.
     */
    @Test
    void should_throwUnauthorizedException_when_emailNotFound() {
        // Arrange — el repositorio no encuentra al usuario
        when(userRepositoryPort.findByEmail("noexiste@financiera.com")).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loginUseCase.execute("noexiste@financiera.com", "cualquier"))
                .expectError(UnauthorizedException.class)
                .verify();
    }
}
