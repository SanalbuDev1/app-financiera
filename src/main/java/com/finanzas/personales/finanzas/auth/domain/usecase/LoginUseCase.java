package com.finanzas.personales.finanzas.auth.domain.usecase;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import com.finanzas.personales.finanzas.auth.domain.port.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

/**
 * Caso de uso que encapsula la lógica de autenticación de un usuario.
 * Pertenece al núcleo del dominio: solo depende de puertos e interfaces,
 * sin conocimiento de la infraestructura (HTTP, base de datos, JWT).
 * Es instanciado como bean por {@code ApplicationConfig} — sin anotaciones de Spring.
 * Lombok genera el constructor a partir de los campos {@code final}.
 */
@Slf4j
@RequiredArgsConstructor
public class LoginUseCase {

    /** Puerto de salida para consultar usuarios en el almacenamiento. */
    private final UserRepositoryPort userRepositoryPort;

    /** Encoder para verificar la contraseña ingresada contra el hash almacenado. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Autentica a un usuario verificando email y contraseña.
     *
     * @param email    correo electrónico del usuario
     * @param password contraseña en texto plano ingresada por el usuario
     * @return {@code Mono<User>} con el usuario autenticado,
     *         o error con {@code UnauthorizedException} si las credenciales son inválidas
     */
    public Mono<User> execute(String email, String password) {
        log.info("[LOGIN-UC] Intentando autenticar email: {}", email);
        return userRepositoryPort.findByEmail(email)
                .doOnNext(user -> log.debug("[LOGIN-UC] Usuario encontrado en BD: {}", email))
                // Verificar que la contraseña coincide con el hash almacenado
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                // Si el filter descarta el elemento, emitir error 401
                .switchIfEmpty(Mono.error(new UnauthorizedException("Credenciales inválidas")))
                .doOnSuccess(user -> log.info("[LOGIN-UC] Autenticación exitosa para email: {}", email))
                .doOnError(ex -> log.warn("[LOGIN-UC] Autenticación fallida para email: {} - {}", email, ex.getMessage()));
    }
}
