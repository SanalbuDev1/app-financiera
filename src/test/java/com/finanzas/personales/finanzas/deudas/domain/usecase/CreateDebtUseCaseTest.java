package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.AmortizationResult;
import com.finanzas.personales.finanzas.deudas.domain.model.CreateDebtCommand;
import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtType;
import com.finanzas.personales.finanzas.deudas.domain.model.PaymentFrequency;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtScheduleRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtTypeRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.PaymentFrequencyRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link CreateDebtUseCase}.
 * Verifica la creación de deudas con su tabla de amortización.
 *
 * Se utiliza TDD: primero se escriben los tests, luego la implementación.
 */
@ExtendWith(MockitoExtension.class)
class CreateDebtUseCaseTest {

    @Mock
    private DebtRepositoryPort debtRepositoryPort;

    @Mock
    private DebtScheduleRepositoryPort debtScheduleRepositoryPort;

    @Mock
    private DebtTypeRepositoryPort debtTypeRepositoryPort;

    @Mock
    private PaymentFrequencyRepositoryPort paymentFrequencyRepositoryPort;

    @Mock
    private CalculateAmortizationUseCase calculateAmortizationUseCase;

    private CreateDebtUseCase createDebtUseCase;

    // Test fixtures
    private static final String USER_ID = "user-123";
    private static final String DEBT_TYPE_ID = "debt-type-bank-loan";
    private static final String FREQUENCY_ID = "freq-monthly";

    private DebtType testDebtType;
    private PaymentFrequency testFrequency;
    private CreateDebtCommand testCommand;

    @BeforeEach
    void setUp() {
        createDebtUseCase = new CreateDebtUseCase(
                debtRepositoryPort,
                debtScheduleRepositoryPort,
                debtTypeRepositoryPort,
                paymentFrequencyRepositoryPort,
                calculateAmortizationUseCase
        );

        // Setup test fixtures
        testDebtType = DebtType.builder()
                .id(DEBT_TYPE_ID)
                .name("prestamo_bancario")
                .description("Préstamo bancario")
                .icon("account_balance")
                .active(true)
                .build();

        testFrequency = PaymentFrequency.builder()
                .id(FREQUENCY_ID)
                .name("mensual")
                .daysBetweenPayments(30)
                .build();

        testCommand = CreateDebtCommand.builder()
                .userId(USER_ID)
                .debtTypeId(DEBT_TYPE_ID)
                .frequencyId(FREQUENCY_ID)
                .creditor("Banco Nacional")
                .description("Préstamo para vehículo")
                .originalAmount(new BigDecimal("10000000"))
                .interestRate(new BigDecimal("1.5"))
                .interestRateType("monthly")
                .totalInstallments(12)
                .startDate(LocalDate.of(2026, 5, 1))
                .notes("Crédito vehicular")
                .build();
    }

    /**
     * Verifica que se crea una deuda correctamente con todos sus datos.
     */
    @Test
    void should_create_debt_with_valid_data() {
        // Arrange
        AmortizationResult amortizationResult = createMockAmortizationResult();

        when(debtTypeRepositoryPort.findById(DEBT_TYPE_ID))
                .thenReturn(Mono.just(testDebtType));
        when(paymentFrequencyRepositoryPort.findById(FREQUENCY_ID))
                .thenReturn(Mono.just(testFrequency));
        when(calculateAmortizationUseCase.execute(any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(amortizationResult);
        when(debtRepositoryPort.save(any(Debt.class)))
                .thenAnswer(invocation -> {
                    Debt debt = invocation.getArgument(0);
                    debt.setId("debt-generated-id");
                    return Mono.just(debt);
                });
        when(debtScheduleRepositoryPort.saveAll(anyList()))
                .thenReturn(Flux.fromIterable(amortizationResult.getSchedule()));

        // Act & Assert
        StepVerifier.create(createDebtUseCase.execute(testCommand))
                .assertNext(debt -> {
                    assertNotNull(debt);
                    assertNotNull(debt.getId());
                    assertEquals(USER_ID, debt.getUserId());
                    assertEquals(DEBT_TYPE_ID, debt.getDebtTypeId());
                    assertEquals("Banco Nacional", debt.getCreditor());
                    assertEquals(new BigDecimal("10000000"), debt.getOriginalAmount());
                    assertEquals(new BigDecimal("10000000"), debt.getCurrentBalance());
                    assertEquals("active", debt.getStatus());
                    assertEquals(12, debt.getTotalInstallments());
                    assertEquals(12, debt.getRemainingInstallments());
                })
                .verifyComplete();

        // Verify interactions
        verify(debtRepositoryPort).save(any(Debt.class));
        verify(debtScheduleRepositoryPort).saveAll(anyList());
    }

    /**
     * Verifica que se calcula y asigna la cuota correctamente.
     */
    @Test
    void should_calculate_installment_amount() {
        // Arrange
        BigDecimal expectedInstallment = new BigDecimal("916799.93");
        AmortizationResult amortizationResult = AmortizationResult.builder()
                .installmentAmount(expectedInstallment)
                .totalInterest(new BigDecimal("1001599.16"))
                .totalPayment(new BigDecimal("11001599.16"))
                .schedule(List.of())
                .build();

        when(debtTypeRepositoryPort.findById(DEBT_TYPE_ID))
                .thenReturn(Mono.just(testDebtType));
        when(paymentFrequencyRepositoryPort.findById(FREQUENCY_ID))
                .thenReturn(Mono.just(testFrequency));
        when(calculateAmortizationUseCase.execute(any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(amortizationResult);
        when(debtRepositoryPort.save(any(Debt.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(debtScheduleRepositoryPort.saveAll(anyList()))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(createDebtUseCase.execute(testCommand))
                .assertNext(debt -> {
                    assertEquals(expectedInstallment, debt.getInstallmentAmount());
                })
                .verifyComplete();
    }

    /**
     * Verifica que se guarda la tabla de amortización completa.
     */
    @Test
    void should_save_amortization_schedule() {
        // Arrange
        AmortizationResult amortizationResult = createMockAmortizationResult();

        when(debtTypeRepositoryPort.findById(DEBT_TYPE_ID))
                .thenReturn(Mono.just(testDebtType));
        when(paymentFrequencyRepositoryPort.findById(FREQUENCY_ID))
                .thenReturn(Mono.just(testFrequency));
        when(calculateAmortizationUseCase.execute(any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(amortizationResult);
        when(debtRepositoryPort.save(any(Debt.class)))
                .thenAnswer(invocation -> {
                    Debt debt = invocation.getArgument(0);
                    debt.setId("debt-123");
                    return Mono.just(debt);
                });
        when(debtScheduleRepositoryPort.saveAll(anyList()))
                .thenReturn(Flux.fromIterable(amortizationResult.getSchedule()));

        // Act
        StepVerifier.create(createDebtUseCase.execute(testCommand))
                .expectNextCount(1)
                .verifyComplete();

        // Assert - Verify schedule items were saved with debt ID
        ArgumentCaptor<List<DebtScheduleItem>> scheduleCaptor = ArgumentCaptor.forClass(List.class);
        verify(debtScheduleRepositoryPort).saveAll(scheduleCaptor.capture());

        List<DebtScheduleItem> savedSchedule = scheduleCaptor.getValue();
        assertFalse(savedSchedule.isEmpty());
        assertTrue(savedSchedule.stream().allMatch(item -> "debt-123".equals(item.getDebtId())));
    }

    /**
     * Verifica que falla si el tipo de deuda no existe.
     */
    @Test
    void should_fail_when_debt_type_not_found() {
        // Arrange
        when(debtTypeRepositoryPort.findById(DEBT_TYPE_ID))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(createDebtUseCase.execute(testCommand))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Tipo de deuda no encontrado"))
                .verify();

        // Verify no debt was saved
        verify(debtRepositoryPort, never()).save(any());
    }

    /**
     * Verifica que falla si la frecuencia de pago no existe.
     */
    @Test
    void should_fail_when_frequency_not_found() {
        // Arrange
        when(debtTypeRepositoryPort.findById(DEBT_TYPE_ID))
                .thenReturn(Mono.just(testDebtType));
        when(paymentFrequencyRepositoryPort.findById(FREQUENCY_ID))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(createDebtUseCase.execute(testCommand))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Frecuencia de pago no encontrada"))
                .verify();

        // Verify no debt was saved
        verify(debtRepositoryPort, never()).save(any());
    }

    /**
     * Verifica que falla si el monto es negativo o cero.
     */
    @Test
    void should_fail_when_amount_is_invalid() {
        // Arrange
        CreateDebtCommand invalidCommand = CreateDebtCommand.builder()
                .userId(USER_ID)
                .debtTypeId(DEBT_TYPE_ID)
                .frequencyId(FREQUENCY_ID)
                .creditor("Banco Nacional")
                .originalAmount(BigDecimal.ZERO) // Invalid
                .interestRate(new BigDecimal("1.5"))
                .interestRateType("monthly")
                .totalInstallments(12)
                .startDate(LocalDate.of(2026, 5, 1))
                .build();

        // Act & Assert
        StepVerifier.create(createDebtUseCase.execute(invalidCommand))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("monto"))
                .verify();

        // Verify no debt was saved
        verify(debtRepositoryPort, never()).save(any());
    }

    /**
     * Verifica que falla si el número de cuotas es inválido.
     */
    @Test
    void should_fail_when_installments_is_invalid() {
        // Arrange
        CreateDebtCommand invalidCommand = CreateDebtCommand.builder()
                .userId(USER_ID)
                .debtTypeId(DEBT_TYPE_ID)
                .frequencyId(FREQUENCY_ID)
                .creditor("Banco Nacional")
                .originalAmount(new BigDecimal("10000000"))
                .interestRate(new BigDecimal("1.5"))
                .interestRateType("monthly")
                .totalInstallments(0) // Invalid
                .startDate(LocalDate.of(2026, 5, 1))
                .build();

        // Act & Assert
        StepVerifier.create(createDebtUseCase.execute(invalidCommand))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("cuotas"))
                .verify();

        // Verify no debt was saved
        verify(debtRepositoryPort, never()).save(any());
    }

    /**
     * Verifica que se calcula la próxima fecha de pago correctamente.
     */
    @Test
    void should_calculate_next_payment_date() {
        // Arrange
        AmortizationResult amortizationResult = createMockAmortizationResult();

        when(debtTypeRepositoryPort.findById(DEBT_TYPE_ID))
                .thenReturn(Mono.just(testDebtType));
        when(paymentFrequencyRepositoryPort.findById(FREQUENCY_ID))
                .thenReturn(Mono.just(testFrequency));
        when(calculateAmortizationUseCase.execute(any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(amortizationResult);
        when(debtRepositoryPort.save(any(Debt.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(debtScheduleRepositoryPort.saveAll(anyList()))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(createDebtUseCase.execute(testCommand))
                .assertNext(debt -> {
                    // La próxima fecha de pago debe ser un mes después del inicio
                    assertEquals(LocalDate.of(2026, 6, 1), debt.getNextPaymentDate());
                })
                .verifyComplete();
    }

    /**
     * Verifica que se asigna el nombre del tipo de deuda.
     */
    @Test
    void should_include_debt_type_name() {
        // Arrange
        AmortizationResult amortizationResult = createMockAmortizationResult();

        when(debtTypeRepositoryPort.findById(DEBT_TYPE_ID))
                .thenReturn(Mono.just(testDebtType));
        when(paymentFrequencyRepositoryPort.findById(FREQUENCY_ID))
                .thenReturn(Mono.just(testFrequency));
        when(calculateAmortizationUseCase.execute(any(), any(), any(), anyInt(), any(), any()))
                .thenReturn(amortizationResult);
        when(debtRepositoryPort.save(any(Debt.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(debtScheduleRepositoryPort.saveAll(anyList()))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(createDebtUseCase.execute(testCommand))
                .assertNext(debt -> {
                    assertEquals("prestamo_bancario", debt.getDebtTypeName());
                    assertEquals("mensual", debt.getFrequencyName());
                })
                .verifyComplete();
    }

    // Helper method to create mock amortization result
    private AmortizationResult createMockAmortizationResult() {
        DebtScheduleItem item1 = DebtScheduleItem.builder()
                .id("schedule-1")
                .installmentNumber(1)
                .dueDate(LocalDate.of(2026, 6, 1))
                .principalAmount(new BigDecimal("766799.93"))
                .interestAmount(new BigDecimal("150000.00"))
                .totalAmount(new BigDecimal("916799.93"))
                .balanceAfter(new BigDecimal("9233200.07"))
                .status("pending")
                .build();

        DebtScheduleItem item2 = DebtScheduleItem.builder()
                .id("schedule-2")
                .installmentNumber(2)
                .dueDate(LocalDate.of(2026, 7, 1))
                .principalAmount(new BigDecimal("778301.93"))
                .interestAmount(new BigDecimal("138498.00"))
                .totalAmount(new BigDecimal("916799.93"))
                .balanceAfter(new BigDecimal("8454898.14"))
                .status("pending")
                .build();

        return AmortizationResult.builder()
                .installmentAmount(new BigDecimal("916799.93"))
                .totalInterest(new BigDecimal("1001599.16"))
                .totalPayment(new BigDecimal("11001599.16"))
                .schedule(List.of(item1, item2))
                .build();
    }
}
