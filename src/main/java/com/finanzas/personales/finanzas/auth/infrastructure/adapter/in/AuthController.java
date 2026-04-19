package com.finanzas.personales.finanzas.auth.infrastructure.adapter.in;

import com.finanzas.personales.finanzas.auth.domain.usecase.EmailAlreadyExistsException;
import com.finanzas.personales.finanzas.auth.domain.usecase.LoginUseCase;
import com.finanzas.personales.finanzas.auth.domain.usecase.RegisterUseCase;
import com.finanzas.personales.finanzas.auth.domain.usecase.UnauthorizedException;
import com.finanzas.personales.finanzas.auth.infrastructure.dto.LoginRequest;
import com.finanzas.personales.finanzas.auth.infrastructure.dto.RegisterRequest;
import com.finanzas.personales.finanzas.auth.infrastructure.dto.UserResponse;
import com.finanzas.personales.finanzas.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Adaptador de entrada (input adapter) que expone los endpoints REST de autenticación.
 * Recibe las solicitudes HTTP del frontend Angular, delega la lógica a los casos de uso
 * del dominio, y mapea los resultados a DTOs de respuesta.
 *
 * <p>Rutas expuestas:
 * <ul>
 *   <li>POST /api/auth/login</li>
 *   <li>POST /api/auth/register</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** Caso de uso del dominio para autenticar usuarios. */
    private final LoginUseCase loginUseCase;

    /** Caso de uso del dominio para registrar nuevos usuarios. */
    private final RegisterUseCase registerUseCase;

    /** Servicio de infraestructura para generar el token JWT tras la autenticación. */
    private final JwtService jwtService;

    /**
     * Autentica a un usuario existente y devuelve sus datos junto con un JWT.
     *
     * @param request DTO con email y contraseña del usuario
     * @return 200 OK con {@link UserResponse} (incluye JWT),
     *         o 401 Unauthorized si las credenciales son inválidas
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("[LOGIN] Solicitud de login recibida para email: {}", request.getEmail());
        return loginUseCase.execute(request.getEmail(), request.getPassword())
                .map(user -> {
                    // Generar JWT para el usuario autenticado
                    String token = jwtService.generateToken(user);
                    log.info("[LOGIN] Login exitoso para email: {}, userId: {}", user.getEmail(), user.getId());
                    return ResponseEntity.ok(UserResponse.from(user, token));
                })
                // Mapear excepción de dominio al código HTTP 401
                .onErrorResume(UnauthorizedException.class, ex -> {
                    log.warn("[LOGIN] Credenciales inválidas para email: {}", request.getEmail());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }

    /**
     * Registra un nuevo usuario en el sistema y devuelve sus datos junto con un JWT.
     *
     * @param request DTO con email, contraseña y nombre del nuevo usuario
     * @return 201 Created con {@link UserResponse} (incluye JWT),
     *         o 409 Conflict si el email ya está registrado
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("[REGISTER] Solicitud de registro recibida para email: {}, nombre: {}", request.getEmail(), request.getName());
        return registerUseCase.execute(request.getEmail(), request.getPassword(), request.getName())
                .map(user -> {
                    // Generar JWT para el usuario recién registrado
                    String token = jwtService.generateToken(user);
                    log.info("[REGISTER] Registro exitoso para email: {}, userId: {}", user.getEmail(), user.getId());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .<UserResponse>body(UserResponse.from(user, token));
                })
                // Mapear excepción de dominio al código HTTP 409
                .onErrorResume(EmailAlreadyExistsException.class, ex -> {
                    log.warn("[REGISTER] Email ya registrado: {}", request.getEmail());
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                });
    }
}
