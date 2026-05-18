package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.AmortizationResult;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para {@link CalculateAmortizationUseCase}.
 * Verifica el cálculo correcto de amortización francesa (cuota fija).
 *
 * Test case base: Préstamo $10,000,000 al 1.5% mensual, 12 cuotas
 * Fórmula: Cuota = (P * r * (1+r)^n) / ((1+r)^n - 1)
 * - Cuota esperada: ~916,799.93
 * - Total intereses: ~1,001,599
 * - Total a pagar: ~11,001,599
 */
class CalculateAmortizationUseCaseTest {

    private CalculateAmortizationUseCase calculateAmortizationUseCase;

    @BeforeEach
    void setUp() {
        calculateAmortizationUseCase = new CalculateAmortizationUseCase();
    }

    /**
     * Verifica el cálculo de amortización francesa con tasa mensual.
     * Préstamo: $10,000,000 al 1.5% mensual, 12 cuotas.
     */
    @Test
    void should_calculate_french_amortization_monthly() {
        // Arrange
        BigDecimal principal = new BigDecimal("10000000");
        BigDecimal interestRate = new BigDecimal("1.5"); // 1.5% mensual
        String interestRateType = "monthly";
        int installments = 12;
        String frequency = "mensual";
        LocalDate startDate = LocalDate.of(2026, 5, 1);

        // Act
        AmortizationResult result = calculateAmortizationUseCase.execute(
                principal, interestRate, interestRateType, installments, frequency, startDate);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getInstallmentAmount());

        // Cuota esperada: ~916,799.93 (fórmula francesa correcta)
        // Cuota = (P * r * (1+r)^n) / ((1+r)^n - 1) = 916,799.93
        BigDecimal expectedInstallment = new BigDecimal("916799.93");
        assertTrue(
                result.getInstallmentAmount().subtract(expectedInstallment).abs()
                        .compareTo(new BigDecimal("5")) <= 0,  // Tolerancia de $5 por redondeos
                "Cuota esperada ~916,799.93, obtenida: " + result.getInstallmentAmount()
        );
    }

    /**
     * Verifica el cálculo de amortización con frecuencia quincenal.
     * La tasa mensual se ajusta a quincenal.
     */
    @Test
    void should_calculate_french_amortization_biweekly() {
        // Arrange
        BigDecimal principal = new BigDecimal("10000000");
        BigDecimal interestRate = new BigDecimal("1.5"); // 1.5% mensual
        String interestRateType = "monthly";
        int installments = 24; // 24 quincenas = 12 meses
        String frequency = "quincenal";
        LocalDate startDate = LocalDate.of(2026, 5, 1);

        // Act
        AmortizationResult result = calculateAmortizationUseCase.execute(
                principal, interestRate, interestRateType, installments, frequency, startDate);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getInstallmentAmount());

        // La cuota quincenal debe ser aproximadamente la mitad de la mensual
        // pero no exactamente por el efecto del interés compuesto
        BigDecimal monthlyInstallment = new BigDecimal("917351.85");
        BigDecimal halfMonthly = monthlyInstallment.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        // La cuota quincenal debe estar entre 40% y 60% de la mensual
        assertTrue(
                result.getInstallmentAmount().compareTo(halfMonthly.multiply(new BigDecimal("0.8"))) > 0 &&
                result.getInstallmentAmount().compareTo(halfMonthly.multiply(new BigDecimal("1.2"))) < 0,
                "Cuota quincenal debe ser aproximadamente la mitad de la mensual"
        );
    }

    /**
     * Verifica la conversión de tasa anual a mensual.
     * Tasa anual 18% ≈ 1.39% mensual (tasa efectiva).
     */
    @Test
    void should_convert_annual_rate_to_monthly() {
        // Arrange
        BigDecimal principal = new BigDecimal("10000000");
        BigDecimal interestRate = new BigDecimal("18"); // 18% anual
        String interestRateType = "annual";
        int installments = 12;
        String frequency = "mensual";
        LocalDate startDate = LocalDate.of(2026, 5, 1);

        // Act
        AmortizationResult result = calculateAmortizationUseCase.execute(
                principal, interestRate, interestRateType, installments, frequency, startDate);

        // Assert
        assertNotNull(result);

        // Con 18% anual efectivo, la tasa mensual es (1.18)^(1/12) - 1 ≈ 1.39%
        // La cuota debe ser menor que con 1.5% mensual (~917,351)
        BigDecimal installmentWith1_5Percent = new BigDecimal("917351.85");
        assertTrue(
                result.getInstallmentAmount().compareTo(installmentWith1_5Percent) < 0,
                "Con 18% anual, la cuota debe ser menor que con 1.5% mensual"
        );
    }

    /**
     * Verifica que se genera la tabla de amortización completa.
     */
    @Test
    void should_generate_complete_schedule() {
        // Arrange
        BigDecimal principal = new BigDecimal("10000000");
        BigDecimal interestRate = new BigDecimal("1.5");
        String interestRateType = "monthly";
        int installments = 12;
        String frequency = "mensual";
        LocalDate startDate = LocalDate.of(2026, 5, 1);

        // Act
        AmortizationResult result = calculateAmortizationUseCase.execute(
                principal, interestRate, interestRateType, installments, frequency, startDate);

        // Assert
        assertNotNull(result.getSchedule());
        assertEquals(12, result.getSchedule().size(), "Debe generar 12 cuotas");

        // Verificar primera cuota
        DebtScheduleItem firstItem = result.getSchedule().get(0);
        assertEquals(1, firstItem.getInstallmentNumber());
        assertEquals(LocalDate.of(2026, 6, 1), firstItem.getDueDate()); // Un mes después

        // Interés primera cuota: 10,000,000 * 1.5% = 150,000
        BigDecimal expectedFirstInterest = new BigDecimal("150000.00");
        assertTrue(
                firstItem.getInterestAmount().subtract(expectedFirstInterest).abs()
                        .compareTo(BigDecimal.ONE) <= 0,
                "Interés primera cuota debe ser ~150,000"
        );

        // Verificar última cuota
        DebtScheduleItem lastItem = result.getSchedule().get(11);
        assertEquals(12, lastItem.getInstallmentNumber());
        assertEquals(LocalDate.of(2027, 5, 1), lastItem.getDueDate());

        // El saldo después de la última cuota debe ser 0 (o muy cercano)
        assertTrue(
                lastItem.getBalanceAfter().abs().compareTo(BigDecimal.ONE) <= 0,
                "Saldo final debe ser 0, obtenido: " + lastItem.getBalanceAfter()
        );

        // Verificar que todas las cuotas tienen estado 'pending'
        assertTrue(
                result.getSchedule().stream().allMatch(item -> "pending".equals(item.getStatus())),
                "Todas las cuotas deben tener estado 'pending'"
        );
    }

    /**
     * Verifica que la suma de todas las cuotas es igual al total a pagar.
     */
    @Test
    void should_sum_installments_equal_total_with_interest() {
        // Arrange
        BigDecimal principal = new BigDecimal("10000000");
        BigDecimal interestRate = new BigDecimal("1.5");
        String interestRateType = "monthly";
        int installments = 12;
        String frequency = "mensual";
        LocalDate startDate = LocalDate.of(2026, 5, 1);

        // Act
        AmortizationResult result = calculateAmortizationUseCase.execute(
                principal, interestRate, interestRateType, installments, frequency, startDate);

        // Assert
        List<DebtScheduleItem> schedule = result.getSchedule();

        // Suma de todas las cuotas
        BigDecimal sumOfInstallments = schedule.stream()
                .map(DebtScheduleItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Suma de capital
        BigDecimal sumOfPrincipal = schedule.stream()
                .map(DebtScheduleItem::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Suma de intereses
        BigDecimal sumOfInterest = schedule.stream()
                .map(DebtScheduleItem::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Verificaciones
        // 1. Suma de capital debe ser igual al principal original
        assertTrue(
                sumOfPrincipal.subtract(principal).abs().compareTo(BigDecimal.ONE) <= 0,
                "Suma de capital debe ser igual al principal: " + sumOfPrincipal
        );

        // 2. Suma de intereses debe ser igual al total de intereses reportado
        assertTrue(
                sumOfInterest.subtract(result.getTotalInterest()).abs().compareTo(BigDecimal.ONE) <= 0,
                "Suma de intereses debe coincidir con totalInterest"
        );

        // 3. Suma de cuotas debe ser igual al total a pagar
        assertTrue(
                sumOfInstallments.subtract(result.getTotalPayment()).abs().compareTo(BigDecimal.ONE) <= 0,
                "Suma de cuotas debe coincidir con totalPayment"
        );

        // 4. Total a pagar = principal + intereses
        BigDecimal expectedTotal = principal.add(result.getTotalInterest());
        assertTrue(
                result.getTotalPayment().subtract(expectedTotal).abs().compareTo(BigDecimal.ONE) <= 0,
                "totalPayment debe ser principal + totalInterest"
        );

        // Total intereses esperado: ~1,001,599 (calculado con fórmula francesa correcta)
        // Total pagado ≈ 916,799.93 * 12 = 11,001,599, menos ajuste última cuota
        BigDecimal expectedTotalInterest = new BigDecimal("1001599");
        assertTrue(
                result.getTotalInterest().subtract(expectedTotalInterest).abs()
                        .compareTo(new BigDecimal("100")) <= 0, // Tolerancia de $100 por ajuste última cuota
                "Total intereses esperado ~1,001,599, obtenido: " + result.getTotalInterest()
        );
    }
}
