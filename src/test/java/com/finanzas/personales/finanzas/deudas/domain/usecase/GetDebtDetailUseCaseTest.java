package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtScheduleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link GetDebtDetailUseCase}.
 * Verifica la obtención del detalle de una deuda con su tabla de amortización.
 */
@ExtendWith(MockitoExtension.class)
class GetDebtDetailUseCaseTest {

    @Mock
    private DebtRepositoryPort debtRepositoryPort;

    @Mock
    private DebtScheduleRepositoryPort debtScheduleRepositoryPort;

    private GetDebtDetailUseCase getDebtDetailUseCase;

    private static final String USER_ID = "user-123";
    private static final String DEBT_ID = "debt-abc";

    @BeforeEach
    void setUp() {
        getDebtDetailUseCase = new GetDebtDetailUseCase(debtRepositoryPort, debtScheduleRepositoryPort);
    }

    /**
     * Verifica que se retorna la deuda junto con su cronograma completo.
     */
    @Test
    void should_return_debt_with_schedule() {
        Debt debt = buildDebt();
        DebtScheduleItem item1 = buildScheduleItem(1);
        DebtScheduleItem item2 = buildScheduleItem(2);

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtScheduleRepositoryPort.findByDebtId(DEBT_ID)).thenReturn(Flux.just(item1, item2));

        StepVerifier.create(getDebtDetailUseCase.execute(DEBT_ID, USER_ID))
                .assertNext(detail -> {
                    assertThat(detail.debt()).isEqualTo(debt);
                    assertThat(detail.schedule()).hasSize(2);
                    assertThat(detail.schedule().get(0).getInstallmentNumber()).isEqualTo(1);
                    assertThat(detail.schedule().get(1).getInstallmentNumber()).isEqualTo(2);
                })
                .verifyComplete();
    }

    /**
     * Verifica que se retorna error cuando la deuda no existe para el usuario.
     */
    @Test
    void should_fail_when_debt_not_found() {
        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(getDebtDetailUseCase.execute(DEBT_ID, USER_ID))
                .expectErrorMatches(ex -> ex instanceof IllegalArgumentException
                        && ex.getMessage().contains(DEBT_ID))
                .verify();
    }

    /**
     * Verifica que se retorna error cuando la deuda pertenece a otro usuario
     * (findByIdAndUserId devuelve vacío en ese caso).
     */
    @Test
    void should_fail_when_debt_belongs_to_other_user() {
        String otherUserId = "other-user-999";
        // El port retorna vacío cuando el userId no coincide
        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, otherUserId)).thenReturn(Mono.empty());

        StepVerifier.create(getDebtDetailUseCase.execute(DEBT_ID, otherUserId))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    /**
     * Verifica que se retorna la deuda con schedule vacío cuando aún no tiene cuotas.
     */
    @Test
    void should_return_debt_with_empty_schedule() {
        Debt debt = buildDebt();

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtScheduleRepositoryPort.findByDebtId(DEBT_ID)).thenReturn(Flux.empty());

        StepVerifier.create(getDebtDetailUseCase.execute(DEBT_ID, USER_ID))
                .assertNext(detail -> {
                    assertThat(detail.debt()).isEqualTo(debt);
                    assertThat(detail.schedule()).isEmpty();
                })
                .verifyComplete();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Debt buildDebt() {
        return Debt.builder()
                .id(DEBT_ID)
                .userId(USER_ID)
                .debtTypeId("debt-type-bank-loan")
                .debtTypeName("prestamo_bancario")
                .frequencyId("freq-monthly")
                .frequencyName("mensual")
                .creditor("Banco Test")
                .originalAmount(new BigDecimal("10000000"))
                .currentBalance(new BigDecimal("8000000"))
                .interestRate(new BigDecimal("1.5"))
                .interestRateType("monthly")
                .totalInstallments(12)
                .remainingInstallments(9)
                .installmentAmount(new BigDecimal("917351.85"))
                .startDate(LocalDate.of(2026, 1, 1))
                .nextPaymentDate(LocalDate.of(2026, 5, 1))
                .status("active")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private DebtScheduleItem buildScheduleItem(int number) {
        return DebtScheduleItem.builder()
                .id("item-" + number)
                .debtId(DEBT_ID)
                .installmentNumber(number)
                .dueDate(LocalDate.of(2026, number, 1))
                .principalAmount(new BigDecimal("767351.85"))
                .interestAmount(new BigDecimal("150000"))
                .totalAmount(new BigDecimal("917351.85"))
                .balanceAfter(new BigDecimal("9232648.15"))
                .status("pending")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
