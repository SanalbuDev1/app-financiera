package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comando de entrada para el caso de uso {@code RegisterPaymentUseCase}.
 * Soporta pagos regulares y pagos extraordinarios (abonos a capital).
 */
@Data
@Builder
public class RegisterPaymentCommand {
    /** Identificador de la deuda sobre la que se aplica el pago. */
    private String debtId;
    /** Identificador del usuario propietario de la deuda. */
    private String userId;
    /** Fecha en que se realiza el pago. */
    private LocalDate paymentDate;
    /** Monto total pagado. */
    private BigDecimal totalAmount;
    /** Tipo de pago: 'regular' o 'extra'. */
    private String paymentType;
    /**
     * Estrategia para pagos extraordinarios: 'reduce_installment' o 'reduce_term'.
     * Solo aplica cuando {@code paymentType = 'extra'}.
     */
    private String extraPaymentStrategy;
    /** Notas adicionales (opcional). */
    private String notes;
}
