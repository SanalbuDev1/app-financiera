package com.finanzas.personales.finanzas.deudas.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad R2DBC que representa la tabla {@code debts} en PostgreSQL.
 * Pertenece exclusivamente a la capa de infraestructura — no se expone fuera de ella.
 * El adaptador de salida mapea esta entidad hacia/desde el modelo de dominio {@code Debt}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("debts")
public class DebtEntity {

    @Id
    private String id;

    @Column("user_id")
    private String userId;

    @Column("debt_type_id")
    private String debtTypeId;

    @Column("frequency_id")
    private String frequencyId;

    private String creditor;
    private String description;

    @Column("original_amount")
    private BigDecimal originalAmount;

    @Column("current_balance")
    private BigDecimal currentBalance;

    @Column("interest_rate")
    private BigDecimal interestRate;

    @Column("interest_rate_type")
    private String interestRateType;

    @Column("total_installments")
    private Integer totalInstallments;

    @Column("remaining_installments")
    private Integer remainingInstallments;

    @Column("installment_amount")
    private BigDecimal installmentAmount;

    @Column("start_date")
    private LocalDate startDate;

    @Column("next_payment_date")
    private LocalDate nextPaymentDate;

    private String status;
    private String notes;

    @Column("created_at")
    private LocalDateTime createdAt;
}
