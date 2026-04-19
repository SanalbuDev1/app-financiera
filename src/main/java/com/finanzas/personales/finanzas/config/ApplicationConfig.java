package com.finanzas.personales.finanzas.config;

import com.finanzas.personales.finanzas.auth.domain.port.UserRepositoryPort;
import com.finanzas.personales.finanzas.auth.domain.usecase.LoginUseCase;
import com.finanzas.personales.finanzas.auth.domain.usecase.RegisterUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de la capa de aplicación.
 * Declara los casos de uso del dominio como beans de Spring para que puedan
 * ser inyectados en los adaptadores de entrada, sin necesidad de anotar
 * las clases del dominio con anotaciones de Spring (@Component, @Service, etc.).
 * Esto mantiene el dominio completamente libre de dependencias de frameworks.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Registra {@link LoginUseCase} como bean de Spring.
     * Recibe sus dependencias (port y encoder) inyectadas por el contenedor.
     *
     * @param userRepositoryPort puerto de salida para consultar usuarios
     * @param passwordEncoder    encoder BCrypt para verificar contraseñas
     * @return instancia del caso de uso de login
     */
    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepositoryPort,
                                     PasswordEncoder passwordEncoder) {
        return new LoginUseCase(userRepositoryPort, passwordEncoder);
    }

    /**
     * Registra {@link RegisterUseCase} como bean de Spring.
     * Recibe sus dependencias (port y encoder) inyectadas por el contenedor.
     *
     * @param userRepositoryPort puerto de salida para persistir nuevos usuarios
     * @param passwordEncoder    encoder BCrypt para hashear contraseñas
     * @return instancia del caso de uso de registro
     */
    @Bean
    public RegisterUseCase registerUseCase(UserRepositoryPort userRepositoryPort,
                                           PasswordEncoder passwordEncoder) {
        return new RegisterUseCase(userRepositoryPort, passwordEncoder);
    }
}
