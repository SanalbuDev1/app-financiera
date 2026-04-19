package com.finanzas.personales.finanzas.health.infrastructure.adapter.in;

import com.finanzas.personales.finanzas.health.infrastructure.dto.HealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Controlador de health check.
 * Expone GET /api/health para verificar que la aplicación está activa.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /** Versión de la aplicación, leída desde build.gradle.kts vía application.properties. */
    @Value("${spring.application.name:finanzas}")
    private String appName;

    /**
     * Devuelve el estado de salud del servicio.
     *
     * @return Mono con el HealthResponse indicando status UP
     */
    @GetMapping
    public Mono<ResponseEntity<HealthResponse>> check() {
        return Mono.just(
                ResponseEntity.ok(
                        HealthResponse.builder()
                                .status("UP")
                                .version(appName)
                                .timestamp(Instant.now())
                                .build()
                )
        );
    }
}
