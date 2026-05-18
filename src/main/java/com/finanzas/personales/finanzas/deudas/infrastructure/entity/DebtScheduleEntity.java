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
 * Entidad R2DBC que representa la tabla {@code debt_schedule} en PostgreSQL.
 * Cada fila es una cuota proyectada de la tabla de amortización francesa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("debt_schedule")
public class DebtScheduleEntity {

    @Id
    private String id;

    @Column("debt_id")
    private String debtId;

    @Column("installment_number")
    private Integer installmentNumber;

    @Column("due_date")
    private LocalDate dueDate;

    @Column("principal_amount")
    private BigDecimal principalAmount;

    @Column("interest_amount")
    private BigDecimal interestAmount;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("balance_after")
    private BigDecimal balanceAfter;

    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
