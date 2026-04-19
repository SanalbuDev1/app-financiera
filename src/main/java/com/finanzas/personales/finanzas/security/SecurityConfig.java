package com.finanzas.personales.finanzas.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuración de seguridad de Spring Security para WebFlux.
 * Define qué rutas son públicas, cuáles requieren autenticación,
 * y registra el filtro JWT en la cadena de seguridad reactiva.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** Filtro JWT que valida el token en cada solicitud. */
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Define la cadena de filtros de seguridad para WebFlux.
     * Las rutas de autenticación son públicas; el resto requiere JWT válido.
     *
     * @param http objeto de configuración de seguridad de WebFlux
     * @return la cadena de filtros configurada
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Deshabilitar CSRF — API stateless con JWT no lo necesita
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // Deshabilitar autenticación básica HTTP
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                // Deshabilitar formulario de login
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Swagger UI y especificación OpenAPI — acceso público
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        // Rutas de autenticación públicas (login y registro)
                        .pathMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        // Health check público
                        .pathMatchers(HttpMethod.GET, "/api/health").permitAll()
                        // El resto de rutas requieren autenticación
                        .anyExchange().authenticated()
                )
                // Registrar el filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * Bean del encoder de contraseñas usando BCrypt.
     * Se inyecta en los use cases del dominio para hashear y verificar contraseñas.
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
