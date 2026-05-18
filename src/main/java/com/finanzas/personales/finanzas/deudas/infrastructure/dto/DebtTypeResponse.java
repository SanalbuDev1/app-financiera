package com.finanzas.personales.finanzas.deudas.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de salida que representa un tipo de deuda de la tabla maestra.
 * Usado en respuestas de {@code GET /api/debt-types}.
 */
@Data
@Builder
public class DebtTypeResponse {

    private String id;
    private String name;
    private String description;
    private String icon;
    private Boolean active;
}
