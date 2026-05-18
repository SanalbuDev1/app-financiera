package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.DebtSummary;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link GetDebtSummaryUseCase}.
 * Verifica la obtención del resumen financiero de deudas del usuario.
 */
@ExtendWith(MockitoExtension.class)
class GetDebtSummaryUseCaseTest {

    @Mock
    private DebtRepositoryPort debtRepositoryPort;

    private GetDebtSummaryUseCase getDebtSummaryUseCase;

    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        getDebtSummaryUseCase = new GetDebtSummaryUseCase(debtRepositoryPort);
    }

    /**
     * Verifica que se retorna el resumen con los totales correctos cuando el usuario tiene deudas.
     */
    @Test
    void should_return_summary_with_correct_totals() {
        DebtSummary expectedSummary = DebtSummary.builder()
                .totalDebts(3)
                .totalBalance(new BigDecimal("25000000"))
                .totalOriginalAmount(new BigDecimal("35000000"))
                .totalMonthlyPayment(new BigDecimal("1500000"))
                .totalPendingInterest(new BigDecimal("3000000"))
                .averageProgress(new BigDecimal("28.57"))
                .build();

        when(debtRepositoryPort.getSummaryByUserId(USER_ID)).thenReturn(Mono.just(expectedSummary));

        StepVerifier.create(getDebtSummaryUseCase.execute(USER_ID))
                .assertNext(summary -> {
                    assertThat(summary.getTotalDebts()).isEqualTo(3);
                    assertThat(summary.getTotalBalance()).isEqualByComparingTo(new BigDecimal("25000000"));
                    assertThat(summary.getTotalMonthlyPayment()).isEqualByComparingTo(new BigDecimal("1500000"));
                })
                .verifyComplete();

        verify(debtRepositoryPort).getSummaryByUserId(USER_ID);
    }

    /**
     * Verifica que se retorna un resumen con ceros cuando el usuario no tiene deudas activas.
     */
    @Test
    void should_return_zero_summary_when_no_debts() {
        DebtSummary emptySummary = DebtSummary.builder()
                .totalDebts(0)
                .totalBalance(BigDecimal.ZERO)
                .totalOriginalAmount(BigDecimal.ZERO)
                .totalMonthlyPayment(BigDecimal.ZERO)
                .totalPendingInterest(BigDecimal.ZERO)
                .averageProgress(BigDecimal.ZERO)
                .build();

        when(debtRepositoryPort.getSummaryByUserId(USER_ID)).thenReturn(Mono.just(emptySummary));

        StepVerifier.create(getDebtSummaryUseCase.execute(USER_ID))
                .assertNext(summary -> {
                    assertThat(summary.getTotalDebts()).isEqualTo(0);
                    assertThat(summary.getTotalBalance()).isEqualByComparingTo(BigDecimal.ZERO);
                })
                .verifyComplete();
    }

    /**
     * Verifica que el caso de uso delega correctamente al puerto con el userId correcto.
     */
    @Test
    void should_delegate_to_repository_port_with_correct_user_id() {
        String specificUserId = "specific-user-456";

        DebtSummary summary = DebtSummary.builder()
                .totalDebts(1)
                .totalBalance(new BigDecimal("5000000"))
                .totalOriginalAmount(new BigDecimal("5000000"))
                .totalMonthlyPayment(new BigDecimal("500000"))
                .totalPendingInterest(new BigDecimal("250000"))
                .averageProgress(BigDecimal.ZERO)
                .build();

        when(debtRepositoryPort.getSummaryByUserId(specificUserId)).thenReturn(Mono.just(summary));

        StepVerifier.create(getDebtSummaryUseCase.execute(specificUserId))
                .expectNextCount(1)
                .verifyComplete();

        verify(debtRepositoryPort).getSummaryByUserId(specificUserId);
    }
}
