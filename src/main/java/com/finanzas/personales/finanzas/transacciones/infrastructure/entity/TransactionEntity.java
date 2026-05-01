package com.finanzas.personales.finanzas.transacciones.infrastructure.entity;

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
 * Entidad R2DBC que representa la tabla {@code transactions} en PostgreSQL.
 * Pertenece exclusivamente a la capa de infraestructura — no se expone fuera de ella.
 * Se mapea hacia/desde el modelo de dominio {@code TransactionsDto} en el adaptador de salida.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("transactions")
public class TransactionEntity {

    /** Identificador único de la transacción (UUID). */
    @Id
    private String id;

    /** FK al usuario propietario de la transacción. */
    @Column("user_id")
    private String userId;

    /** Descripción de la transacción. */
    private String description;

    /** Monto de la transacción. Siempre positivo. */
    private BigDecimal amount;

    /** FK a la tabla maestra de categorías. */
    @Column("category_id")
    private String categoryId;

    /** FK a la tabla maestra de tipos de transacción. */
    @Column("type_id")
    private String typeId;

    /** Fecha en la que se realizó la transacción. */
    @Column("transaction_date")
    private LocalDate transactionDate;

    /** Notas adicionales opcionales. */
    private String notes;

    /** Fecha y hora de creación del registro. */
    @Column("created_at")
    private LocalDateTime createdAt;
}
