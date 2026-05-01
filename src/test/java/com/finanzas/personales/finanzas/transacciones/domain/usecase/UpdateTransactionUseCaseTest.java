package com.finanzas.personales.finanzas.transacciones.domain.usecase;

import com.finanzas.personales.finanzas.transacciones.domain.model.TransactionsDto;
import com.finanzas.personales.finanzas.transacciones.domain.model.port.TransactionRepositoryPort;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link UpdateTransactionUseCase}.
 * Mockea {@link TransactionRepositoryPort} para aislar la lógica del caso de uso.
 */
@ExtendWith(MockitoExtension.class)
class UpdateTransactionUseCaseTest {

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    private UpdateTransactionUseCase updateTransactionUseCase;

    private TransactionsDto existingTransaction;

    @BeforeEach
    void setUp() {
        updateTransactionUseCase = new UpdateTransactionUseCase(transactionRepositoryPort);

        existingTransaction = TransactionsDto.builder()
                .id("tx-uuid-1")
                .userId("user-uuid-1")
                .description("Almuerzo actualizado")
                .amount(new BigDecimal("200.00"))
                .category("food")
                .type("expense")
                .transactionDate(LocalDate.of(2026, 4, 21))
                .notes("Nota actualizada")
                .createdAt(LocalDateTime.of(2026, 4, 21, 10, 0, 0))
                .build();
    }

    /**
     * Verifica que una transacción se actualiza correctamente cuando existe
     * y pertenece al usuario autenticado.
     */
    @Test
    void should_returnUpdatedTransaction_when_transactionExistsAndBelongsToUser() {
        when(transactionRepositoryPort.update(any(TransactionsDto.class))).thenReturn(Mono.just(1L));
        when(transactionRepositoryPort.findById("tx-uuid-1")).thenReturn(Mono.just(existingTransaction));

        StepVerifier.create(updateTransactionUseCase.execute(
                        "tx-uuid-1", "user-uuid-1", "Almuerzo actualizado",
                        new BigDecimal("200.00"), "food", "expense",
                        LocalDate.of(2026, 4, 21), "Nota actualizada"))
                .expectNextMatches(tx -> tx.getId().equals("tx-uuid-1")
                        && tx.getDescription().equals("Almuerzo actualizado")
                        && tx.getAmount().compareTo(new BigDecimal("200.00")) == 0)
                .verifyComplete();
    }

    /**
     * Verifica que se retorna vacío cuando la transacción no existe
     * o no pertenece al usuario (0 filas actualizadas).
     */
    @Test
    void should_returnEmpty_when_transactionNotFoundOrNotOwned() {
        when(transactionRepositoryPort.update(any(TransactionsDto.class))).thenReturn(Mono.just(0L));

        StepVerifier.create(updateTransactionUseCase.execute(
                        "tx-inexistente", "user-uuid-1", "Descripcion",
                        new BigDecimal("100.00"), "food", "expense",
                        LocalDate.of(2026, 4, 21), null))
                .verifyComplete();
    }

}
