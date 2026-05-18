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
 * Entidad R2DBC que representa la tabla {@code debt_payments} en PostgreSQL.
 * Registra tanto pagos regulares como pagos extraordinarios (abonos a capital).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("debt_payments")
public class DebtPaymentEntity {

    @Id
    private String id;

    @Column("debt_id")
    private String debtId;

    @Column("payment_date")
    private LocalDate paymentDate;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("principal_amount")
    private BigDecimal principalAmount;

    @Column("interest_amount")
    private BigDecimal interestAmount;

    @Column("payment_type")
    private String paymentType;

    @Column("extra_payment_strategy")
    private String extraPaymentStrategy;

    private String notes;

    @Column("created_at")
    private LocalDateTime createdAt;
}
