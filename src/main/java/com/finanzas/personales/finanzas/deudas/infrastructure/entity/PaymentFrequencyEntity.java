package com.finanzas.personales.finanzas.deudas.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad R2DBC que representa la tabla maestra {@code payment_frequencies} en PostgreSQL.
 * Define las frecuencias de pago disponibles: mensual (30 días) y quincenal (15 días).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("payment_frequencies")
public class PaymentFrequencyEntity {

    @Id
    private String id;

    private String name;

    @Column("days_between_payments")
    private Integer daysBetweenPayments;
}
