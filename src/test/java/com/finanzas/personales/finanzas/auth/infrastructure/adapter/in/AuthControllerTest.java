package com.finanzas.personales.finanzas.auth.infrastructure.adapter.in;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import com.finanzas.personales.finanzas.auth.domain.model.UserRole;
import com.finanzas.personales.finanzas.auth.domain.usecase.EmailAlreadyExistsException;
import com.finanzas.personales.finanzas.auth.domain.usecase.LoginUseCase;
import com.finanzas.personales.finanzas.auth.domain.usecase.RegisterUseCase;
import com.finanzas.personales.finanzas.auth.domain.usecase.UnauthorizedException;
import com.finanzas.personales.finanzas.security.domain.port.TokenServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link AuthController} usando WebTestClient.
 * Mockea los casos de uso y el tokenServicePort para aislar el controlador.
 */
@WebFluxTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private RegisterUseCase registerUseCase;

    @MockitoBean
    private TokenServicePort tokenServicePort;

    /** Usuario de dominio reutilizable en los tests. */
    private final User mockUser = User.builder()
            .id("uuid-1")
            .email("user@financiera.com")
            .name("Usuario")
            .password("$2a$10$hashed")
            .role(UserRole.USER)
            .build();

    /**
     * Verifica que POST /api/auth/login retorna 200 OK con el token
     * cuando las credenciales son válidas.
     */
    @Test
    @WithMockUser
    void should_return200WithToken_when_loginIsSuccessful() {
        when(loginUseCase.execute("user@financiera.com", "user123")).thenReturn(Mono.just(mockUser));
        when(tokenServicePort.generateToken(mockUser)).thenReturn("jwt.token.generado");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        { "email": "user@financiera.com", "password": "user123" }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("user@financiera.com")
                .jsonPath("$.role").isEqualTo("USER")
                .jsonPath("$.token").isEqualTo("jwt.token.generado");
    }

    /**
     * Verifica que POST /api/auth/login retorna 401 Unauthorized
     * cuando las credenciales son incorrectas.
     */
    @Test
    @WithMockUser
    void should_return401_when_credentialsAreInvalid() {
        when(loginUseCase.execute("user@financiera.com", "wrong"))
                .thenReturn(Mono.error(new UnauthorizedException("Credenciales inválidas")));

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        { "email": "user@financiera.com", "password": "wrong" }
                        """)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Verifica que POST /api/auth/register retorna 201 Created con el token
     * cuando el email no está registrado.
     */
    @Test
    @WithMockUser
    void should_return201WithToken_when_registerIsSuccessful() {
        when(registerUseCase.execute("nuevo@financiera.com", "pass123", "Nuevo")).thenReturn(Mono.just(mockUser));
        when(tokenServicePort.generateToken(mockUser)).thenReturn("jwt.token.generado");

        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        { "email": "nuevo@financiera.com", "password": "pass123", "name": "Nuevo" }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.token").isEqualTo("jwt.token.generado");
    }

    /**
     * Verifica que POST /api/auth/register retorna 409 Conflict
     * cuando el email ya está registrado en el sistema.
     */
    @Test
    @WithMockUser
    void should_return409_when_emailAlreadyExists() {
        when(registerUseCase.execute("admin@financiera.com", "pass123", "Admin"))
                .thenReturn(Mono.error(new EmailAlreadyExistsException("Email ya registrado")));

        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        { "email": "admin@financiera.com", "password": "pass123", "name": "Admin" }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409);
    }
}
