package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link DeleteDebtUseCase}.
 * Verifica la eliminación de deudas con verificación de propiedad.
 */
@ExtendWith(MockitoExtension.class)
class DeleteDebtUseCaseTest {

    @Mock
    private DebtRepositoryPort debtRepositoryPort;

    private DeleteDebtUseCase deleteDebtUseCase;

    private static final String USER_ID = "user-123";
    private static final String DEBT_ID = "debt-abc";

    @BeforeEach
    void setUp() {
        deleteDebtUseCase = new DeleteDebtUseCase(debtRepositoryPort);
    }

    /**
     * Verifica que la deuda se elimina correctamente cuando pertenece al usuario.
     */
    @Test
    void should_delete_debt_when_belongs_to_user() {
        Debt debt = buildDebt();

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(debt));
        when(debtRepositoryPort.deleteById(DEBT_ID)).thenReturn(Mono.empty());

        StepVerifier.create(deleteDebtUseCase.execute(DEBT_ID, USER_ID))
                .verifyComplete();

        verify(debtRepositoryPort).deleteById(DEBT_ID);
    }

    /**
     * Verifica que se retorna error y no se elimina cuando la deuda no existe.
     */
    @Test
    void should_fail_when_debt_not_found() {
        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(deleteDebtUseCase.execute(DEBT_ID, USER_ID))
                .expectErrorMatches(ex -> ex instanceof IllegalArgumentException
                        && ex.getMessage().contains(DEBT_ID))
                .verify();

        // No se debe intentar eliminar si no se encontró la deuda
        verify(debtRepositoryPort, never()).deleteById(DEBT_ID);
    }

    /**
     * Verifica que no se puede eliminar una deuda de otro usuario
     * (findByIdAndUserId retorna vacío cuando el userId no coincide).
     */
    @Test
    void should_fail_when_debt_belongs_to_other_user() {
        String otherUserId = "other-user-999";

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, otherUserId)).thenReturn(Mono.empty());

        StepVerifier.create(deleteDebtUseCase.execute(DEBT_ID, otherUserId))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(debtRepositoryPort, never()).deleteById(DEBT_ID);
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
}
