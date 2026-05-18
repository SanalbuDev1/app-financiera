package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.UpdateDebtCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link UpdateDebtUseCase}.
 * Verifica la actualización de campos editables de una deuda.
 */
@ExtendWith(MockitoExtension.class)
class UpdateDebtUseCaseTest {

    @Mock
    private DebtRepositoryPort debtRepositoryPort;

    private UpdateDebtUseCase updateDebtUseCase;

    private static final String USER_ID = "user-123";
    private static final String DEBT_ID = "debt-abc";

    @BeforeEach
    void setUp() {
        updateDebtUseCase = new UpdateDebtUseCase(debtRepositoryPort);
    }

    /**
     * Verifica que se actualiza el acreedor correctamente.
     */
    @Test
    void should_update_debt_creditor() {
        Debt existingDebt = buildDebt();
        Debt updatedDebt = buildDebt();
        updatedDebt.setCreditor("Nuevo Banco");

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(existingDebt));
        when(debtRepositoryPort.update(any(Debt.class))).thenReturn(Mono.just(updatedDebt));

        UpdateDebtCommand command = UpdateDebtCommand.builder()
                .debtId(DEBT_ID)
                .userId(USER_ID)
                .creditor("Nuevo Banco")
                .build();

        StepVerifier.create(updateDebtUseCase.execute(command))
                .assertNext(debt -> assertThat(debt.getCreditor()).isEqualTo("Nuevo Banco"))
                .verifyComplete();

        verify(debtRepositoryPort).update(any(Debt.class));
    }

    /**
     * Verifica que se actualiza la descripción correctamente.
     */
    @Test
    void should_update_debt_description() {
        Debt existingDebt = buildDebt();
        Debt updatedDebt = buildDebt();
        updatedDebt.setDescription("Nueva descripción del préstamo");

        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(existingDebt));
        when(debtRepositoryPort.update(any(Debt.class))).thenReturn(Mono.just(updatedDebt));

        UpdateDebtCommand command = UpdateDebtCommand.builder()
                .debtId(DEBT_ID)
                .userId(USER_ID)
                .description("Nueva descripción del préstamo")
                .build();

        StepVerifier.create(updateDebtUseCase.execute(command))
                .assertNext(debt -> assertThat(debt.getDescription()).isEqualTo("Nueva descripción del préstamo"))
                .verifyComplete();
    }

    /**
     * Verifica que se retorna error cuando la deuda no existe o no pertenece al usuario.
     */
    @Test
    void should_fail_when_debt_not_found() {
        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.empty());

        UpdateDebtCommand command = UpdateDebtCommand.builder()
                .debtId(DEBT_ID)
                .userId(USER_ID)
                .creditor("Cualquier banco")
                .build();

        StepVerifier.create(updateDebtUseCase.execute(command))
                .expectErrorMatches(ex -> ex instanceof IllegalArgumentException
                        && ex.getMessage().contains(DEBT_ID))
                .verify();
    }

    /**
     * Verifica que los campos null en el comando no sobreescriben los valores actuales.
     */
    @Test
    void should_not_overwrite_fields_with_null() {
        Debt existingDebt = buildDebt();
        // Solo se pasan notas, creditor y description quedan intactos
        when(debtRepositoryPort.findByIdAndUserId(DEBT_ID, USER_ID)).thenReturn(Mono.just(existingDebt));
        when(debtRepositoryPort.update(any(Debt.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        UpdateDebtCommand command = UpdateDebtCommand.builder()
                .debtId(DEBT_ID)
                .userId(USER_ID)
                .notes("Notas actualizadas")
                // creditor y description no se envían (null)
                .build();

        StepVerifier.create(updateDebtUseCase.execute(command))
                .assertNext(debt -> {
                    // El creditor original se mantiene
                    assertThat(debt.getCreditor()).isEqualTo("Banco Test");
                    assertThat(debt.getNotes()).isEqualTo("Notas actualizadas");
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
                .description("Préstamo personal")
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
                .notes("Notas originales")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
