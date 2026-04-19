package com.finanzas.personales.finanzas.auth.domain.usecase;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import com.finanzas.personales.finanzas.auth.domain.model.UserRole;
import com.finanzas.personales.finanzas.auth.domain.port.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Caso de uso que encapsula la lógica de registro de un nuevo usuario.
 * Pertenece al núcleo del dominio: valida reglas de negocio (email único),
 * hashea la contraseña y delega la persistencia al puerto de salida.
 * Es instanciado como bean por {@code ApplicationConfig} — sin anotaciones de Spring.
 * Lombok genera el constructor a partir de los campos {@code final}.
 */
@Slf4j
@RequiredArgsConstructor
public class RegisterUseCase {

    /** Puerto de salida para verificar existencia y persistir nuevos usuarios. */
    private final UserRepositoryPort userRepositoryPort;

    /** Encoder para hashear la contraseña antes de persistirla. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario en el sistema con rol USER por defecto.
     *
     * @param email    correo electrónico del nuevo usuario
     * @param password contraseña en texto plano (será hasheada con BCrypt)
     * @param name     nombre completo del usuario
     * @return {@code Mono<User>} con el usuario creado e ID generado,
     *         o error con {@code EmailAlreadyExistsException} si el email ya está registrado
     */
    public Mono<User> execute(String email, String password, String name) {
        log.info("[REGISTER-UC] Iniciando registro para email: {}", email);
        return userRepositoryPort.existsByEmail(email)
                // Si el email ya existe, emitir error 409
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.warn("[REGISTER-UC] Email ya existe en BD: {}", email);
                        return Mono.error(new EmailAlreadyExistsException("El email ya está registrado: " + email));
                    }
                    // Construir el nuevo usuario con rol USER y contraseña hasheada
                    User newUser = User.builder()
                            .id(UUID.randomUUID().toString())
                            .email(email)
                            .name(name)
                            .password(passwordEncoder.encode(password))
                            .role(UserRole.USER)
                            .build();
                    log.debug("[REGISTER-UC] Usuario construido con id: {}, guardando en BD", newUser.getId());
                    return userRepositoryPort.save(newUser);
                })
                .doOnSuccess(user -> log.info("[REGISTER-UC] Usuario registrado exitosamente: {} (id: {})", user.getEmail(), user.getId()))
                .doOnError(ex -> log.error("[REGISTER-UC] Error al registrar usuario {}: {}", email, ex.getMessage()));
    }
}
