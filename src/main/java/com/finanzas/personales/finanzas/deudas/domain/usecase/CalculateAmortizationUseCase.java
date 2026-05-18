package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.AmortizationResult;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para calcular la tabla de amortización de una deuda.
 * Implementa el sistema de amortización francés (cuota fija).
 *
 * Fórmula: Cuota = (P * r * (1+r)^n) / ((1+r)^n - 1)
 * Donde: P = principal, r = tasa por período, n = número de cuotas
 */
@RequiredArgsConstructor
public class CalculateAmortizationUseCase {

    private static final int SCALE = 10;
    private static final int OUTPUT_SCALE = 2;
    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    /**
     * Calcula la tabla de amortización para una deuda.
     *
     * @param principal Monto del préstamo
     * @param interestRate Tasa de interés (porcentaje, ej: 1.5 para 1.5%)
     * @param interestRateType Tipo de tasa: "monthly" o "annual"
     * @param installments Número de cuotas
     * @param frequency Frecuencia de pago: "mensual" o "quincenal"
     * @param startDate Fecha de inicio del préstamo
     * @return Resultado con la cuota fija y la tabla de amortización
     */
    public AmortizationResult execute(
            BigDecimal principal,
            BigDecimal interestRate,
            String interestRateType,
            int installments,
            String frequency,
            LocalDate startDate) {

        // Convertir la tasa al período correspondiente
        BigDecimal periodRate = calculatePeriodRate(interestRate, interestRateType, frequency);

        // Calcular la cuota fija usando la fórmula francesa
        BigDecimal installmentAmount = calculateInstallment(principal, periodRate, installments);

        // Generar la tabla de amortización
        List<DebtScheduleItem> schedule = generateSchedule(
                principal, periodRate, installmentAmount, installments, frequency, startDate);

        // Calcular totales
        BigDecimal totalInterest = schedule.stream()
                .map(DebtScheduleItem::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayment = principal.add(totalInterest);

        return AmortizationResult.builder()
                .installmentAmount(installmentAmount.setScale(OUTPUT_SCALE, RoundingMode.HALF_UP))
                .totalInterest(totalInterest.setScale(OUTPUT_SCALE, RoundingMode.HALF_UP))
                .totalPayment(totalPayment.setScale(OUTPUT_SCALE, RoundingMode.HALF_UP))
                .schedule(schedule)
                .build();
    }

    /**
     * Convierte la tasa de interés al período de pago correspondiente.
     *
     * @param interestRate Tasa de interés en porcentaje
     * @param interestRateType "monthly" o "annual"
     * @param frequency "mensual" o "quincenal"
     * @return Tasa por período como decimal (ej: 0.015 para 1.5%)
     */
    private BigDecimal calculatePeriodRate(BigDecimal interestRate, String interestRateType, String frequency) {
        // Convertir porcentaje a decimal (1.5% -> 0.015)
        BigDecimal rateDecimal = interestRate.divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);

        BigDecimal monthlyRate;

        if ("annual".equalsIgnoreCase(interestRateType)) {
            // Convertir tasa anual a mensual: (1 + r_anual)^(1/12) - 1
            double annualRateDouble = rateDecimal.doubleValue();
            double monthlyRateDouble = Math.pow(1 + annualRateDouble, 1.0 / 12.0) - 1;
            monthlyRate = BigDecimal.valueOf(monthlyRateDouble);
        } else {
            // Ya es tasa mensual
            monthlyRate = rateDecimal;
        }

        // Ajustar según la frecuencia de pago
        if ("quincenal".equalsIgnoreCase(frequency)) {
            // Convertir tasa mensual a quincenal: (1 + r_mensual)^(1/2) - 1
            double monthlyRateDouble = monthlyRate.doubleValue();
            double biweeklyRateDouble = Math.pow(1 + monthlyRateDouble, 0.5) - 1;
            return BigDecimal.valueOf(biweeklyRateDouble);
        }

        return monthlyRate;
    }

    /**
     * Calcula la cuota fija usando la fórmula de amortización francesa.
     *
     * Cuota = (P * r * (1+r)^n) / ((1+r)^n - 1)
     *
     * @param principal Monto del préstamo
     * @param periodRate Tasa por período como decimal
     * @param installments Número de cuotas
     * @return Cuota fija
     */
    private BigDecimal calculateInstallment(BigDecimal principal, BigDecimal periodRate, int installments) {
        if (periodRate.compareTo(BigDecimal.ZERO) == 0) {
            // Sin interés, cuota simple
            return principal.divide(BigDecimal.valueOf(installments), SCALE, RoundingMode.HALF_UP);
        }

        // (1 + r)^n
        BigDecimal onePlusRate = BigDecimal.ONE.add(periodRate);
        BigDecimal onePlusRatePowN = onePlusRate.pow(installments, MC);

        // P * r * (1+r)^n
        BigDecimal numerator = principal.multiply(periodRate).multiply(onePlusRatePowN);

        // (1+r)^n - 1
        BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Genera la tabla de amortización completa.
     *
     * @param principal Monto del préstamo
     * @param periodRate Tasa por período
     * @param installmentAmount Cuota fija
     * @param installments Número de cuotas
     * @param frequency Frecuencia de pago
     * @param startDate Fecha de inicio
     * @return Lista de ítems de la tabla de amortización
     */
    private List<DebtScheduleItem> generateSchedule(
            BigDecimal principal,
            BigDecimal periodRate,
            BigDecimal installmentAmount,
            int installments,
            String frequency,
            LocalDate startDate) {

        List<DebtScheduleItem> schedule = new ArrayList<>();
        BigDecimal balance = principal;
        LocalDate dueDate = startDate;

        for (int i = 1; i <= installments; i++) {
            // Calcular fecha de vencimiento
            dueDate = calculateNextDueDate(dueDate, frequency);

            // Calcular interés del período
            BigDecimal interestAmount = balance.multiply(periodRate).setScale(OUTPUT_SCALE, RoundingMode.HALF_UP);

            // Calcular capital (cuota - interés)
            BigDecimal principalAmount;
            BigDecimal totalAmount;

            if (i == installments) {
                // Última cuota: ajustar para que el saldo quede en 0
                principalAmount = balance;
                totalAmount = principalAmount.add(interestAmount);
            } else {
                principalAmount = installmentAmount.subtract(interestAmount);
                totalAmount = installmentAmount;
            }

            principalAmount = principalAmount.setScale(OUTPUT_SCALE, RoundingMode.HALF_UP);

            // Calcular nuevo saldo
            BigDecimal balanceAfter = balance.subtract(principalAmount).setScale(OUTPUT_SCALE, RoundingMode.HALF_UP);

            // Asegurar que el saldo no sea negativo
            if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
                balanceAfter = BigDecimal.ZERO;
            }

            DebtScheduleItem item = DebtScheduleItem.builder()
                    .id(UUID.randomUUID().toString())
                    .installmentNumber(i)
                    .dueDate(dueDate)
                    .principalAmount(principalAmount)
                    .interestAmount(interestAmount)
                    .totalAmount(totalAmount.setScale(OUTPUT_SCALE, RoundingMode.HALF_UP))
                    .balanceAfter(balanceAfter)
                    .status("pending")
                    .createdAt(LocalDateTime.now())
                    .build();

            schedule.add(item);
            balance = balanceAfter;
        }

        return schedule;
    }

    /**
     * Calcula la siguiente fecha de vencimiento según la frecuencia.
     *
     * @param currentDate Fecha actual
     * @param frequency "mensual" o "quincenal"
     * @return Siguiente fecha de vencimiento
     */
    private LocalDate calculateNextDueDate(LocalDate currentDate, String frequency) {
        if ("quincenal".equalsIgnoreCase(frequency)) {
            return currentDate.plusDays(15);
        }
        // Por defecto, mensual
        return currentDate.plusMonths(1);
    }
}
