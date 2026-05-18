package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * Modelo de dominio que representa un tipo de deuda.
 * Catálogo de tipos como tarjeta de crédito, préstamo bancario, hipoteca, etc.
 */
@Data
@Builder
public class DebtType {

    /** Identificador único del tipo de deuda (UUID como String). */
    private String id;

    /** Nombre único del tipo (tarjeta_credito, prestamo_bancario, etc.). */
    private String name;

    /** Descripción legible para mostrar en UI. */
    private String description;

    /** Nombre del ícono para UI (Material Icons). */
    private String icon;

    /** Indica si el tipo está activo para selección. */
    private Boolean active;
}
