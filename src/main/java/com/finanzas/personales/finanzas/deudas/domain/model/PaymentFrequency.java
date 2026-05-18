package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * Modelo de dominio que representa una frecuencia de pago.
 * Define cada cuántos días se realiza un pago (mensual, quincenal).
 */
@Data
@Builder
public class PaymentFrequency {

    /** Identificador único de la frecuencia (UUID como String). */
    private String id;

    /** Nombre de la frecuencia (mensual, quincenal). */
    private String name;

    /** Número de días entre pagos. */
    private Integer daysBetweenPayments;
}
