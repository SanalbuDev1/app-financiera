package com.finanzas.personales.finanzas.health.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * DTO de respuesta del endpoint de salud de la aplicación.
 * Devuelve el estado actual del servicio y metadatos básicos.
 */
@Data
@Builder
public class HealthResponse {

    /** Estado del servicio: UP o DOWN. */
    private String status;

    /** Versión de la aplicación. */
    private String version;

    /** Marca de tiempo del momento en que se consultó el health. */
    private Instant timestamp;
}