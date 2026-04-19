package com.finanzas.personales.finanzas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de CORS para permitir las peticiones del frontend Angular.
 * El origen permitido se configura desde {@code application.properties}
 * para poder cambiarlo entre entornos sin recompilar.
 */
@Configuration
public class CorsConfig {

    /** Orígenes permitidos (separados por coma), leído desde application.properties. */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Define la política CORS para toda la aplicación WebFlux.
     * Permite los métodos HTTP estándar y todos los headers para los orígenes configurados.
     *
     * @return fuente de configuración CORS basada en URL
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Permitir cualquier origen (dev) — usar allowedOriginPatterns para compatibilidad con credentials
        config.setAllowedOriginPatterns(List.of("*"));

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Permitir todos los headers (Authorization, Content-Type, etc.)
        config.setAllowedHeaders(List.of("*"));

        // Permitir el envío de credenciales (necesario para el header Authorization)
        config.setAllowCredentials(true);

        // Aplicar esta configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
