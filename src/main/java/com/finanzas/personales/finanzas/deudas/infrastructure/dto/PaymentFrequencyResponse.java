package com.finanzas.personales.finanzas.deudas.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de salida que representa una frecuencia de pago de la tabla maestra.
 * Usado en respuestas de {@code GET /api/debt-types} y al consultar frecuencias.
 */
@Data
@Builder
public class PaymentFrequencyResponse {

    private String id;

    /** Nombre de la frecuencia: 'mensual' o 'quincenal'. */
    private String name;

    /** Días entre pagos: 30 (mensual) o 15 (quincenal). */
    private Integer daysBetweenPayments;
}
