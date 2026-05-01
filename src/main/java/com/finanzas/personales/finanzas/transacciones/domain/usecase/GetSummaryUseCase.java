package com.finanzas.personales.finanzas.transacciones.domain.usecase;

import com.finanzas.personales.finanzas.transacciones.domain.model.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Caso de uso para obtener el resumen financiero del usuario.
 * Calcula balance total, ingresos/gastos mensuales y ahorro.
 * No tiene anotaciones de Spring — se registra como @Bean en ApplicationConfig.
 */
@RequiredArgsConstructor
public class GetSummaryUseCase {

    /** Puerto de salida para consultas de agregación. */
    private final TransactionRepositoryPort transactionRepositoryPort;

    /** Meta de ahorro por defecto. */
    private static final BigDecimal DEFAULT_SAVINGS_GOAL = new BigDecimal("3000.00");

    /**
     * Ejecuta el cálculo del resumen financiero.
     *
     * @param userId identificador del usuario (del JWT)
     * @param month  mes a consultar (1-12)
     * @param year   año a consultar
     * @return {@code Mono<SummaryResult>} con todos los valores calculados
     */
    public Mono<SummaryResult> execute(String userId, int month, int year) {
        // Ejecutar las 3 consultas de agregación en paralelo
        return Mono.zip(
                transactionRepositoryPort.getTotalBalance(userId),
                transactionRepositoryPort.getMonthlyIncome(userId, month, year),
                transactionRepositoryPort.getMonthlyExpenses(userId, month, year)
        ).map(tuple -> {
            BigDecimal totalBalance = tuple.getT1();
            BigDecimal monthlyIncome = tuple.getT2();
            BigDecimal monthlyExpenses = tuple.getT3();
            BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpenses);

            return new SummaryResult(totalBalance, monthlyIncome, monthlyExpenses, monthlySavings, DEFAULT_SAVINGS_GOAL);
        });
    }

    /**
     * Resultado del resumen financiero.
     *
     * @param totalBalance    balance histórico total
     * @param monthlyIncome   ingresos del mes
     * @param monthlyExpenses gastos del mes
     * @param monthlySavings  ahorro del mes
     * @param savingsGoal     meta de ahorro
     */
    public record SummaryResult(
            BigDecimal totalBalance,
            BigDecimal monthlyIncome,
            BigDecimal monthlyExpenses,
            BigDecimal monthlySavings,
            BigDecimal savingsGoal
    ) {}
}
