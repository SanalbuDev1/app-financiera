package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link ListDebtsUseCase}.
 * Verifica la obtención de deudas con y sin filtro de estado.
 */
@ExtendWith(MockitoExtension.class)
class ListDebtsUseCaseTest {

    @Mock
    private DebtRepositoryPort debtRepositoryPort;

    private ListDebtsUseCase listDebtsUseCase;

    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        listDebtsUseCase = new ListDebtsUseCase(debtRepositoryPort);
    }

    /**
     * Verifica que se retornan todas las deudas del usuario cuando no se aplica filtro de estado.
     */
    @Test
    void should_list_all_user_debts_when_no_status_filter() {
        Debt debt1 = buildDebt("debt-1", "active");
        Debt debt2 = buildDebt("debt-2", "paid_off");

        when(debtRepositoryPort.findAllByUserId(USER_ID)).thenReturn(Flux.just(debt1, debt2));

        StepVerifier.create(listDebtsUseCase.execute(USER_ID, null))
                .expectNext(debt1)
                .expectNext(debt2)
                .verifyComplete();

        verify(debtRepositoryPort).findAllByUserId(USER_ID);
    }

    /**
     * Verifica que se filtran las deudas por estado cuando se especifica uno.
     */
    @Test
    void should_filter_by_status_when_status_provided() {
        Debt activeDebt = buildDebt("debt-1", "active");

        when(debtRepositoryPort.findByUserIdAndStatus(USER_ID, "active"))
                .thenReturn(Flux.just(activeDebt));

        StepVerifier.create(listDebtsUseCase.execute(USER_ID, "active"))
                .expectNext(activeDebt)
                .verifyComplete();

        verify(debtRepositoryPort).findByUserIdAndStatus(USER_ID, "active");
    }

    /**
     * Verifica que se usa el filtro por estado también para "paid_off".
     */
    @Test
    void should_filter_by_paid_off_status() {
        Debt paidDebt = buildDebt("debt-2", "paid_off");

        when(debtRepositoryPort.findByUserIdAndStatus(USER_ID, "paid_off"))
                .thenReturn(Flux.just(paidDebt));

        StepVerifier.create(listDebtsUseCase.execute(USER_ID, "paid_off"))
                .expectNext(paidDebt)
                .verifyComplete();

        verify(debtRepositoryPort).findByUserIdAndStatus(USER_ID, "paid_off");
    }

    /**
     * Verifica que se retorna Flux vacío cuando el usuario no tiene deudas.
     */
    @Test
    void should_return_empty_when_no_debts() {
        when(debtRepositoryPort.findAllByUserId(USER_ID)).thenReturn(Flux.empty());

        StepVerifier.create(listDebtsUseCase.execute(USER_ID, null))
                .verifyComplete();
    }

    /**
     * Verifica que un estado en blanco se trata como sin filtro.
     */
    @Test
    void should_list_all_when_status_is_blank() {
        Debt debt1 = buildDebt("debt-1", "active");

        when(debtRepositoryPort.findAllByUserId(USER_ID)).thenReturn(Flux.just(debt1));

        StepVerifier.create(listDebtsUseCase.execute(USER_ID, "  "))
                .expectNext(debt1)
                .verifyComplete();

        verify(debtRepositoryPort).findAllByUserId(USER_ID);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Debt buildDebt(String id, String status) {
        return Debt.builder()
                .id(id)
                .userId(USER_ID)
                .debtTypeId("debt-type-bank-loan")
                .debtTypeName("prestamo_bancario")
                .frequencyId("freq-monthly")
                .frequencyName("mensual")
                .creditor("Banco Test")
                .originalAmount(new BigDecimal("1000000"))
                .currentBalance(new BigDecimal("800000"))
                .interestRate(new BigDecimal("1.5"))
                .interestRateType("monthly")
                .totalInstallments(12)
                .remainingInstallments(9)
                .installmentAmount(new BigDecimal("91667"))
                .startDate(LocalDate.of(2026, 1, 1))
                .nextPaymentDate(LocalDate.of(2026, 5, 1))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
