package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.AmortizationResult;
import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtPayment;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import com.finanzas.personales.finanzas.deudas.domain.model.RegisterPaymentCommand;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtPaymentRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtScheduleRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para registrar un pago (regular o extraordinario) a una deuda.
 *
 * <p><b>Pago regular:</b> usa los montos de la próxima cuota pendiente del cronograma.
 * Actualiza el saldo, las cuotas restantes y la próxima fecha de pago.</p>
 *
 * <p><b>Pago extraordinario:</b> aplica el monto íntegro al capital. Según la estrategia:
 * <ul>
 *   <li>{@code reduce_installment} — recalcula la cuota con el nuevo saldo, manteniendo
 *       el número de cuotas restantes.</li>
 *   <li>{@code reduce_term} — recalcula las cuotas restantes con la cuota actual,
 *       reduciendo el plazo.</li>
 * </ul>
 * En ambos casos regenera la tabla de amortización desde la próxima fecha de pago.</p>
 */
@RequiredArgsConstructor
public class RegisterPaymentUseCase {

    private static final int SCALE = 10;
    private static final int OUTPUT_SCALE = 2;
    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    private final DebtRepositoryPort debtRepositoryPort;
    private final DebtPaymentRepositoryPort debtPaymentRepositoryPort;
    private final DebtScheduleRepositoryPort debtScheduleRepositoryPort;
    private final CalculateAmortizationUseCase calculateAmortizationUseCase;

    /**
     * Registra un pago sobre una deuda.
     * Verifica que la deuda exista y pertenezca al usuario.
     *
     * @param command datos del pago a registrar
     * @return {@code Mono<DebtPayment>} con el pago registrado
     */
    public Mono<DebtPayment> execute(RegisterPaymentCommand command) {
        return validateCommand(command)
                .then(debtRepositoryPort.findByIdAndUserId(command.getDebtId(), command.getUserId()))
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("Deuda no encontrada: " + command.getDebtId())))
                .flatMap(debt -> {
                    if ("extra".equalsIgnoreCase(command.getPaymentType())) {
                        return processExtraPayment(debt, command);
                    }
                    return processRegularPayment(debt, command);
                });
    }

    // ── Pago regular ─────────────────────────────────────────────────────────

    /**
     * Procesa un pago regular usando los montos de la próxima cuota pendiente del cronograma.
     */
    private Mono<DebtPayment> processRegularPayment(Debt debt, RegisterPaymentCommand command) {
        return debtScheduleRepositoryPort.findNextPendingByDebtId(debt.getId())
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("No hay cuotas pendientes para la deuda: " + debt.getId())))
                .flatMap(scheduleItem -> {
                    // Construir el pago con los montos precomputados del cronograma
                    DebtPayment payment = DebtPayment.builder()
                            .id(UUID.randomUUID().toString())
                            .debtId(debt.getId())
                            .paymentDate(command.getPaymentDate())
                            .totalAmount(scheduleItem.getTotalAmount())
                            .principalAmount(scheduleItem.getPrincipalAmount())
                            .interestAmount(scheduleItem.getInterestAmount())
                            .paymentType("regular")
                            .notes(command.getNotes())
                            .createdAt(LocalDateTime.now())
                            .build();

                    // Marcar la cuota como pagada, guardar el pago y actualizar la deuda
                    return debtScheduleRepositoryPort.updateStatus(scheduleItem.getId(), "paid")
                            .then(debtPaymentRepositoryPort.save(payment))
                            .flatMap(savedPayment ->
                                    updateDebtAfterRegularPayment(debt, scheduleItem)
                                            .thenReturn(savedPayment));
                });
    }

    /**
     * Actualiza el estado de la deuda después de un pago regular:
     * reduce el saldo, decrementa cuotas restantes y avanza la fecha del próximo pago.
     * Si el saldo llega a 0, marca la deuda como saldada.
     */
    private Mono<Void> updateDebtAfterRegularPayment(Debt debt, DebtScheduleItem paidItem) {
        BigDecimal newBalance = debt.getCurrentBalance()
                .subtract(paidItem.getPrincipalAmount())
                .setScale(OUTPUT_SCALE, RoundingMode.HALF_UP);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }

        final BigDecimal finalNewBalance = newBalance;
        final int newRemaining = Math.max(0, debt.getRemainingInstallments() - 1);

        debt.setCurrentBalance(finalNewBalance);

        debt.setRemainingInstallments(newRemaining);

        // Si el saldo es 0 o no quedan cuotas, la deuda queda saldada
        if (finalNewBalance.compareTo(BigDecimal.ZERO) == 0 || newRemaining <= 0) {
            debt.setStatus("paid_off");
            debt.setNextPaymentDate(null);
            return debtRepositoryPort.update(debt).then();
        }

        // Obtener la siguiente cuota pendiente para actualizar nextPaymentDate
        return debtScheduleRepositoryPort.findNextPendingByDebtId(debt.getId())
                .flatMap(nextItem -> {
                    debt.setNextPaymentDate(nextItem.getDueDate());
                    return debtRepositoryPort.update(debt).then();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // No quedan más cuotas pendientes → deuda saldada
                    debt.setStatus("paid_off");
                    debt.setNextPaymentDate(null);
                    return debtRepositoryPort.update(debt).then();
                }));
    }

    // ── Pago extraordinario ───────────────────────────────────────────────────

    /**
     * Procesa un pago extraordinario aplicando el monto íntegro al capital.
     * Recalcula y regenera el cronograma según la estrategia elegida.
     */
    private Mono<DebtPayment> processExtraPayment(Debt debt, RegisterPaymentCommand command) {
        BigDecimal newBalance = debt.getCurrentBalance()
                .subtract(command.getTotalAmount())
                .setScale(OUTPUT_SCALE, RoundingMode.HALF_UP);

        final boolean paidOff = newBalance.compareTo(BigDecimal.ZERO) <= 0;
        final BigDecimal finalNewBalance = paidOff ? BigDecimal.ZERO : newBalance;

        // El pago extraordinario aplica íntegro al capital (sin intereses)
        DebtPayment payment = DebtPayment.builder()
                .id(UUID.randomUUID().toString())
                .debtId(debt.getId())
                .paymentDate(command.getPaymentDate())
                .totalAmount(command.getTotalAmount())
                .principalAmount(command.getTotalAmount())
                .interestAmount(BigDecimal.ZERO)
                .paymentType("extra")
                .extraPaymentStrategy(command.getExtraPaymentStrategy())
                .notes(command.getNotes())
                .createdAt(LocalDateTime.now())
                .build();

        if (paidOff) {
            // La deuda queda saldada con este abono extraordinario
            debt.setCurrentBalance(BigDecimal.ZERO);
            debt.setRemainingInstallments(0);
            debt.setStatus("paid_off");
            debt.setNextPaymentDate(null);
            return debtPaymentRepositoryPort.save(payment)
                    .flatMap(savedPayment -> debtRepositoryPort.update(debt).thenReturn(savedPayment));
        }

        // Actualizar saldo en la deuda
        debt.setCurrentBalance(finalNewBalance);

        // Calcular tasa del período para recalcular cuota/plazo
        BigDecimal periodRate = computePeriodRate(debt);

        if ("reduce_installment".equalsIgnoreCase(command.getExtraPaymentStrategy())) {
            // Mantener plazo, reducir cuota
            BigDecimal newInstallment = computeInstallment(finalNewBalance, periodRate, debt.getRemainingInstallments());
            debt.setInstallmentAmount(newInstallment);
        } else if ("reduce_term".equalsIgnoreCase(command.getExtraPaymentStrategy())) {
            // Mantener cuota, reducir plazo
            int newRemaining = computeRemainingInstallments(finalNewBalance, periodRate, debt.getInstallmentAmount());
            debt.setRemainingInstallments(newRemaining);
        }

        // Regenerar el cronograma desde la próxima fecha de pago
        LocalDate scheduleStartDate = computeScheduleStartDate(debt);
        AmortizationResult newAmortization = calculateAmortizationUseCase.execute(
                finalNewBalance,
                debt.getInterestRate(),
                debt.getInterestRateType(),
                debt.getRemainingInstallments(),
                debt.getFrequencyName(),
                scheduleStartDate
        );

        // Sincronizar installmentAmount con el valor preciso de la amortización
        debt.setInstallmentAmount(newAmortization.getInstallmentAmount());

        // Asignar debtId a los nuevos ítems del cronograma
        List<DebtScheduleItem> newSchedule = newAmortization.getSchedule().stream()
                .map(item -> DebtScheduleItem.builder()
                        .id(item.getId())
                        .debtId(debt.getId())
                        .installmentNumber(item.getInstallmentNumber())
                        .dueDate(item.getDueDate())
                        .principalAmount(item.getPrincipalAmount())
                        .interestAmount(item.getInterestAmount())
                        .totalAmount(item.getTotalAmount())
                        .balanceAfter(item.getBalanceAfter())
                        .status(item.getStatus())
                        .createdAt(item.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return debtPaymentRepositoryPort.save(payment)
                .flatMap(savedPayment -> debtRepositoryPort.update(debt)
                        // regenerateSchedule retorna Flux → convertir a Mono<Void> con .then()
                        .then(debtScheduleRepositoryPort.regenerateSchedule(debt.getId(), newSchedule).then())
                        .thenReturn(savedPayment));
    }

    // ── Utilidades de cálculo ────────────────────────────────────────────────

    /**
     * Calcula la tasa de interés por período según el tipo de tasa y la frecuencia de pago.
     *
     * @param debt deuda con interestRate, interestRateType y frequencyName
     * @return tasa por período como decimal (ej: 0.015 para 1.5% mensual)
     */
    private BigDecimal computePeriodRate(Debt debt) {
        BigDecimal rateDecimal = debt.getInterestRate()
                .divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);

        BigDecimal monthlyRate;
        if ("annual".equalsIgnoreCase(debt.getInterestRateType())) {
            // Convertir tasa anual a mensual: (1 + r_anual)^(1/12) - 1
            double monthlyRateDouble = Math.pow(1 + rateDecimal.doubleValue(), 1.0 / 12.0) - 1;
            monthlyRate = BigDecimal.valueOf(monthlyRateDouble);
        } else {
            monthlyRate = rateDecimal;
        }

        if ("quincenal".equalsIgnoreCase(debt.getFrequencyName())) {
            // Convertir tasa mensual a quincenal: (1 + r_mensual)^(1/2) - 1
            double biweeklyRateDouble = Math.pow(1 + monthlyRate.doubleValue(), 0.5) - 1;
            return BigDecimal.valueOf(biweeklyRateDouble);
        }

        return monthlyRate;
    }

    /**
     * Calcula la cuota fija usando la fórmula de amortización francesa.
     * Cuota = (P * r * (1+r)^n) / ((1+r)^n - 1)
     *
     * @param principal   saldo actual de la deuda
     * @param periodRate  tasa por período como decimal
     * @param installments número de cuotas restantes
     * @return cuota fija calculada
     */
    private BigDecimal computeInstallment(BigDecimal principal, BigDecimal periodRate, int installments) {
        if (periodRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(installments), OUTPUT_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusRate = BigDecimal.ONE.add(periodRate);
        BigDecimal onePlusRatePowN = onePlusRate.pow(installments, MC);
        BigDecimal numerator = principal.multiply(periodRate).multiply(onePlusRatePowN);
        BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, OUTPUT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el número de cuotas restantes dado un saldo, tasa e importe de cuota.
     * n = -ln(1 - balance * r / payment) / ln(1 + r)
     * El resultado se redondea hacia arriba (cuotas completas).
     *
     * @param balance     saldo actual
     * @param periodRate  tasa por período como decimal
     * @param installment importe de cuota actual
     * @return número de cuotas restantes (redondeado hacia arriba)
     */
    private int computeRemainingInstallments(BigDecimal balance, BigDecimal periodRate, BigDecimal installment) {
        if (periodRate.compareTo(BigDecimal.ZERO) == 0) {
            return (int) Math.ceil(balance.divide(installment, SCALE, RoundingMode.HALF_UP).doubleValue());
        }
        double r = periodRate.doubleValue();
        double p = balance.doubleValue();
        double c = installment.doubleValue();
        double ratio = p * r / c;
        if (ratio >= 1.0) {
            // La cuota no cubre los intereses: situación inválida
            throw new IllegalArgumentException(
                    "La cuota actual no cubre los intereses del saldo restante. " +
                    "Use la estrategia 'reduce_installment' o abone más capital.");
        }
        double n = -Math.log(1 - ratio) / Math.log(1 + r);
        return (int) Math.ceil(n);
    }

    /**
     * Calcula la fecha de inicio para generar el nuevo cronograma,
     * de modo que la primera cuota venza en {@code debt.nextPaymentDate}.
     *
     * @param debt deuda con nextPaymentDate y frequencyName
     * @return fecha de inicio a pasar a {@link CalculateAmortizationUseCase}
     */
    private LocalDate computeScheduleStartDate(Debt debt) {
        LocalDate nextPaymentDate = debt.getNextPaymentDate();
        if ("quincenal".equalsIgnoreCase(debt.getFrequencyName())) {
            return nextPaymentDate.minusDays(15);
        }
        return nextPaymentDate.minusMonths(1);
    }

    /**
     * Valida los datos básicos del comando antes de procesar el pago.
     */
    private Mono<Void> validateCommand(RegisterPaymentCommand command) {
        if (command.getTotalAmount() == null || command.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("El monto del pago debe ser mayor a 0"));
        }
        if (command.getDebtId() == null || command.getDebtId().isBlank()) {
            return Mono.error(new IllegalArgumentException("El ID de la deuda es requerido"));
        }
        if (command.getUserId() == null || command.getUserId().isBlank()) {
            return Mono.error(new IllegalArgumentException("El ID del usuario es requerido"));
        }
        if (command.getPaymentDate() == null) {
            return Mono.error(new IllegalArgumentException("La fecha de pago es requerida"));
        }
        return Mono.empty();
    }

}
