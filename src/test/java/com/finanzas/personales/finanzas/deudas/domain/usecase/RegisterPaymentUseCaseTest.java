package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.AmortizationResult;
import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.RegisterPaymentCommand;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtPayment;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtPaymentRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtScheduleRepositoryPort;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link RegisterPaymentUseCase}.
 * Verifica la lógica de pagos regulares y extraordinarios con sus estrategias.
 */
@ExtendWith(MockitoExtension.class)
class RegisterPaymentUseCaseTest {

    @Mock
    private DebtRepositoryPort debtRepositoryPort;

    @Mock
    private DebtPaymentRepositoryPort debtPaymentRepositoryPort;

    @Mock
    private DebtScheduleRepositoryPort debtScheduleRepositoryPort;

    @Mock
    private CalculateAmortizationUseCase calculateAmortizationUseCase;

    private RegisterPaymentUseCase registerPaymentUseCase;

    private static final String USER_ID = "user-123";
    private static final String DEBT_ID = "debt-abc";

    // Deuda de prueba: $10,000,000 al 1.5% mensual, 12 cuotas → cuota ~$917,351.85
    private static final BigDecimal ORIGINAL_AMOUNT = new BigDecimal("10000000");
    private static final BigDecimal INTEREST_RATE = new BigDecimal("1.5");
    private static final BigDecimal INSTALLMENT_AMOUNT = new BigDecimal("917351.85");

    @BeforeEach
    void setUp() {
        registerPaymentUseCase = new RegisterPaymentUseCase(
                debtRepositoryPort,
                debtPaymentRepositoryPort,
                debtScheduleRepositoryPort,
                calculateAmortizationUseCase
        );
    }

    /**
     * Verifica que un pago regular se registra con los montos de la cuota del cronograma.
     */
    @Test
    void should_register_regular_payment() {
        Debt debt = buildDebt(new BigDecimal("10000000"), 12);
        DebtScheduleItem nextItem = buildScheduleItem(1,
                new BigDecimal("767351.85"), new BigDecimal("150000.00"),
                LocalDate.of(2026, 2, 1));
        DebtScheduleItem secondItem = buildScheduleItem(2,
                new BigDecimal("778862.13"), new BigDecimal("138489.72"),
                LocalDate.of(2026, 3, 1));

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtScheduleRepositoryPort.findNextPendingByDebtId(DEBT_ID))
                .thenReturn(Mono.just(nextItem))          // primera llamada: cuota actual
                .thenReturn(Mono.just(secondItem));        // segunda llamada: próxima cuota
        when(debtScheduleRepositoryPort.updateStatus(anyString(), eq("paid"))).thenReturn(Mono.just(nextItem));
        when(debtPaymentRepositoryPort.save(any(DebtPayment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(debtRepositoryPort.update(any(Debt.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        RegisterPaymentCommand command = buildRegularCommand();

        StepVerifier.create(registerPaymentUseCase.execute(command))
                .assertNext(payment -> {
                    assertThat(payment.getPaymentType()).isEqualTo("regular");
                    assertThat(payment.getPrincipalAmount()).isEqualByComparingTo(new BigDecimal("767351.85"));
                    assertThat(payment.getInterestAmount()).isEqualByComparingTo(new BigDecimal("150000.00"));
                })
                .verifyComplete();
    }

    /**
     * Verifica que el saldo de la deuda se reduce correctamente tras un pago regular.
     */
    @Test
    void should_reduce_balance_after_regular_payment() {
        Debt debt = buildDebt(new BigDecimal("10000000"), 12);
        DebtScheduleItem nextItem = buildScheduleItem(1,
                new BigDecimal("767351.85"), new BigDecimal("150000.00"),
                LocalDate.of(2026, 2, 1));
        DebtScheduleItem secondItem = buildScheduleItem(2,
                new BigDecimal("778862.13"), new BigDecimal("138489.72"),
                LocalDate.of(2026, 3, 1));

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtScheduleRepositoryPort.findNextPendingByDebtId(DEBT_ID))
                .thenReturn(Mono.just(nextItem))
                .thenReturn(Mono.just(secondItem));
        when(debtScheduleRepositoryPort.updateStatus(anyString(), anyString())).thenReturn(Mono.just(nextItem));
        when(debtPaymentRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
        when(debtRepositoryPort.update(debtCaptor.capture())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(registerPaymentUseCase.execute(buildRegularCommand()))
                .expectNextCount(1)
                .verifyComplete();

        Debt updatedDebt = debtCaptor.getValue();
        // Saldo reducido: 10,000,000 - 767,351.85 = 9,232,648.15
        assertThat(updatedDebt.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("9232648.15"));
    }

    /**
     * Verifica que las cuotas restantes se decrementan en 1 tras un pago regular.
     */
    @Test
    void should_update_remaining_installments_after_regular_payment() {
        Debt debt = buildDebt(new BigDecimal("10000000"), 12);
        DebtScheduleItem nextItem = buildScheduleItem(1,
                new BigDecimal("767351.85"), new BigDecimal("150000.00"),
                LocalDate.of(2026, 2, 1));
        DebtScheduleItem secondItem = buildScheduleItem(2,
                new BigDecimal("778862.13"), new BigDecimal("138489.72"),
                LocalDate.of(2026, 3, 1));

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtScheduleRepositoryPort.findNextPendingByDebtId(DEBT_ID))
                .thenReturn(Mono.just(nextItem))
                .thenReturn(Mono.just(secondItem));
        when(debtScheduleRepositoryPort.updateStatus(anyString(), anyString())).thenReturn(Mono.just(nextItem));
        when(debtPaymentRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
        when(debtRepositoryPort.update(debtCaptor.capture())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(registerPaymentUseCase.execute(buildRegularCommand()))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(debtCaptor.getValue().getRemainingInstallments()).isEqualTo(11);
    }

    /**
     * Verifica que la deuda se marca como pagada cuando el pago regular cancela el saldo restante.
     */
    @Test
    void should_mark_debt_as_paid_off_when_balance_zero() {
        // Última cuota: saldo = cuota, pago cancela todo
        BigDecimal lastBalance = new BigDecimal("917351.85");
        Debt debt = buildDebt(lastBalance, 1);
        DebtScheduleItem lastItem = buildScheduleItem(12,
                lastBalance, BigDecimal.ZERO, LocalDate.of(2026, 2, 1));

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtScheduleRepositoryPort.findNextPendingByDebtId(DEBT_ID)).thenReturn(Mono.just(lastItem));
        when(debtScheduleRepositoryPort.updateStatus(anyString(), anyString())).thenReturn(Mono.just(lastItem));
        when(debtPaymentRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
        when(debtRepositoryPort.update(debtCaptor.capture())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(registerPaymentUseCase.execute(buildRegularCommand()))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(debtCaptor.getValue().getStatus()).isEqualTo("paid_off");
        assertThat(debtCaptor.getValue().getCurrentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * Verifica que un abono extraordinario con estrategia reduce_installment
     * recalcula la cuota y regenera el cronograma.
     */
    @Test
    void should_recalculate_installment_on_extra_payment_reduce_installment() {
        // Saldo actual: 7,663,241.06 (después de 3 cuotas), abono extra: 2,000,000
        BigDecimal currentBalance = new BigDecimal("7663241.06");
        BigDecimal extraPayment = new BigDecimal("2000000");
        BigDecimal expectedNewBalance = new BigDecimal("5663241.06");

        Debt debt = buildDebt(currentBalance, 9);

        AmortizationResult fakeResult = buildFakeAmortizationResult(expectedNewBalance, 9);

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtPaymentRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(debtRepositoryPort.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(calculateAmortizationUseCase.execute(
                any(), any(), anyString(), any(Integer.class), anyString(), any()
        )).thenReturn(fakeResult);
        when(debtScheduleRepositoryPort.regenerateSchedule(anyString(), anyList()))
                .thenReturn(Flux.fromIterable(fakeResult.getSchedule()));

        RegisterPaymentCommand command = RegisterPaymentCommand.builder()
                .debtId(DEBT_ID)
                .userId(USER_ID)
                .paymentDate(LocalDate.of(2026, 5, 1))
                .totalAmount(extraPayment)
                .paymentType("extra")
                .extraPaymentStrategy("reduce_installment")
                .build();

        StepVerifier.create(registerPaymentUseCase.execute(command))
                .assertNext(payment -> {
                    assertThat(payment.getPaymentType()).isEqualTo("extra");
                    assertThat(payment.getExtraPaymentStrategy()).isEqualTo("reduce_installment");
                    assertThat(payment.getPrincipalAmount()).isEqualByComparingTo(extraPayment);
                    assertThat(payment.getInterestAmount()).isEqualByComparingTo(BigDecimal.ZERO);
                })
                .verifyComplete();

        // Verificar que se regeneró el cronograma
        verify(debtScheduleRepositoryPort).regenerateSchedule(eq(DEBT_ID), anyList());
    }

    /**
     * Verifica que un abono extraordinario con estrategia reduce_term
     * recalcula el número de cuotas y regenera el cronograma.
     */
    @Test
    void should_recalculate_term_on_extra_payment_reduce_term() {
        BigDecimal currentBalance = new BigDecimal("7663241.06");
        BigDecimal extraPayment = new BigDecimal("2000000");
        BigDecimal expectedNewBalance = new BigDecimal("5663241.06");

        Debt debt = buildDebt(currentBalance, 9);
        AmortizationResult fakeResult = buildFakeAmortizationResult(expectedNewBalance, 7);

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtPaymentRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(debtRepositoryPort.update(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(calculateAmortizationUseCase.execute(
                any(), any(), anyString(), any(Integer.class), anyString(), any()
        )).thenReturn(fakeResult);
        when(debtScheduleRepositoryPort.regenerateSchedule(anyString(), anyList()))
                .thenReturn(Flux.fromIterable(fakeResult.getSchedule()));

        RegisterPaymentCommand command = RegisterPaymentCommand.builder()
                .debtId(DEBT_ID)
                .userId(USER_ID)
                .paymentDate(LocalDate.of(2026, 5, 1))
                .totalAmount(extraPayment)
                .paymentType("extra")
                .extraPaymentStrategy("reduce_term")
                .build();

        StepVerifier.create(registerPaymentUseCase.execute(command))
                .assertNext(payment -> {
                    assertThat(payment.getPaymentType()).isEqualTo("extra");
                    assertThat(payment.getExtraPaymentStrategy()).isEqualTo("reduce_term");
                })
                .verifyComplete();

        verify(debtScheduleRepositoryPort).regenerateSchedule(eq(DEBT_ID), anyList());
    }

    /**
     * Verifica que la deuda se marca como pagada cuando el abono extraordinario supera el saldo.
     */
    @Test
    void should_mark_debt_as_paid_off_when_extra_payment_exceeds_balance() {
        BigDecimal currentBalance = new BigDecimal("500000");
        BigDecimal extraPayment = new BigDecimal("600000"); // supera el saldo

        Debt debt = buildDebt(currentBalance, 1);

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtPaymentRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
        when(debtRepositoryPort.update(debtCaptor.capture())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        RegisterPaymentCommand command = RegisterPaymentCommand.builder()
                .debtId(DEBT_ID)
                .userId(USER_ID)
                .paymentDate(LocalDate.of(2026, 5, 1))
                .totalAmount(extraPayment)
                .paymentType("extra")
                .extraPaymentStrategy("reduce_installment")
                .build();

        StepVerifier.create(registerPaymentUseCase.execute(command))
                .expectNextCount(1)
                .verifyComplete();

        Debt updatedDebt = debtCaptor.getValue();
        assertThat(updatedDebt.getStatus()).isEqualTo("paid_off");
        assertThat(updatedDebt.getCurrentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Debt buildDebt(BigDecimal balance, int remaining) {
        return Debt.builder()
                .id(DEBT_ID)
                .userId(USER_ID)
                .debtTypeId("debt-type-bank-loan")
                .debtTypeName("prestamo_bancario")
                .frequencyId("freq-monthly")
                .frequencyName("mensual")
                .creditor("Banco Test")
                .originalAmount(ORIGINAL_AMOUNT)
                .currentBalance(balance)
                .interestRate(INTEREST_RATE)
                .interestRateType("monthly")
                .totalInstallments(12)
                .remainingInstallments(remaining)
                .installmentAmount(INSTALLMENT_AMOUNT)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextPaymentDate(LocalDate.of(2026, 2, 1))
                .status("active")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private DebtScheduleItem buildScheduleItem(int number, BigDecimal principal,
                                               BigDecimal interest, LocalDate dueDate) {
        return DebtScheduleItem.builder()
                .id("item-" + number)
                .debtId(DEBT_ID)
                .installmentNumber(number)
                .dueDate(dueDate)
                .principalAmount(principal)
                .interestAmount(interest)
                .totalAmount(principal.add(interest))
                .balanceAfter(ORIGINAL_AMOUNT.subtract(principal))
                .status("pending")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AmortizationResult buildFakeAmortizationResult(BigDecimal balance, int installments) {
        DebtScheduleItem item = DebtScheduleItem.builder()
                .id("new-item-1")
                .debtId(DEBT_ID)
                .installmentNumber(1)
                .dueDate(LocalDate.of(2026, 6, 1))
                .principalAmount(new BigDecimal("600000"))
                .interestAmount(new BigDecimal("84948.62"))
                .totalAmount(new BigDecimal("684948.62"))
                .balanceAfter(balance.subtract(new BigDecimal("600000")))
                .status("pending")
                .createdAt(LocalDateTime.now())
                .build();

        return AmortizationResult.builder()
                .installmentAmount(new BigDecimal("684948.62"))
                .totalInterest(new BigDecimal("600000"))
                .totalPayment(balance.add(new BigDecimal("600000")))
                .schedule(List.of(item))
                .build();
    }

    private RegisterPaymentCommand buildRegularCommand() {
        return RegisterPaymentCommand.builder()
                .debtId(DEBT_ID)
                .userId(USER_ID)
                .paymentDate(LocalDate.of(2026, 2, 1))
                .totalAmount(INSTALLMENT_AMOUNT)
                .paymentType("regular")
                .build();
    }
}
