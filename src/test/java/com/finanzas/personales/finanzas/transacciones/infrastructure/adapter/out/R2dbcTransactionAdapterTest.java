package com.finanzas.personales.finanzas.transacciones.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.transacciones.domain.model.TransactionsDto;
import com.finanzas.personales.finanzas.config.SqlQueryLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link R2dbcTransactionAdapter}.
 * Mockea {@link DatabaseClient} y {@link SqlQueryLoader} para aislar la lógica del adaptador.
 */
@ExtendWith(MockitoExtension.class)
class R2dbcTransactionAdapterTest {

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private SqlQueryLoader sqlQueryLoader;

    @Mock
    private GenericExecuteSpec executeSpec;

    @Mock
    private FetchSpec<java.util.Map<String, Object>> fetchSpec;

    private R2dbcTransactionAdapter adapter;

    private TransactionsDto sampleTransaction;

    @BeforeEach
    void setUp() {
        adapter = new R2dbcTransactionAdapter(databaseClient, sqlQueryLoader);

        sampleTransaction = TransactionsDto.builder()
                .id("tx-uuid-1")
                .userId("user-uuid-1")
                .description("Compra supermercado")
                .amount(new BigDecimal("150.50"))
                .category("food")
                .type("expense")
                .transactionDate(LocalDate.of(2026, 4, 21))
                .notes("Compra semanal")
                .createdAt(LocalDateTime.of(2026, 4, 21, 10, 0, 0))
                .build();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_returnTransaction_when_findByIdExists() {
        when(sqlQueryLoader.load("transactions/obtener_transaccion_por_id")).thenReturn("SELECT ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("id", "tx-uuid-1")).thenReturn(executeSpec);
        RowsFetchSpec<TransactionsDto> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.just(sampleTransaction));

        StepVerifier.create(adapter.findById("tx-uuid-1"))
                .expectNextMatches(tx -> tx.getId().equals("tx-uuid-1")
                        && tx.getCategory().equals("food"))
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_returnEmpty_when_findByIdNotFound() {
        when(sqlQueryLoader.load("transactions/obtener_transaccion_por_id")).thenReturn("SELECT ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("id", "tx-inexistente")).thenReturn(executeSpec);
        RowsFetchSpec<TransactionsDto> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("tx-inexistente")).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_returnPaginatedTransactions_when_filtersApplied() {
        when(sqlQueryLoader.load("transactions/listar_transacciones_paginadas")).thenReturn("SELECT ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.bindNull(anyString(), any(Class.class))).thenReturn(executeSpec);
        RowsFetchSpec<TransactionsDto> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.all()).thenReturn(Flux.just(sampleTransaction));

        StepVerifier.create(adapter.findByUserIdPaginated("user-uuid-1", null, null, null, null, 0, 15))
                .expectNextMatches(tx -> tx.getId().equals("tx-uuid-1"))
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_returnCount_when_countFiltered() {
        when(sqlQueryLoader.load("transactions/contar_transacciones_filtradas")).thenReturn("SELECT COUNT...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.bindNull(anyString(), any(Class.class))).thenReturn(executeSpec);
        RowsFetchSpec<Long> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.just(25L));

        StepVerifier.create(adapter.countByUserIdFiltered("user-uuid-1", null, null, null, null))
                .expectNext(25L).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_returnBalance_when_getTotalBalance() {
        when(sqlQueryLoader.load("transactions/obtener_balance_total")).thenReturn("SELECT ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("userId", "user-uuid-1")).thenReturn(executeSpec);
        RowsFetchSpec<BigDecimal> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.just(new BigDecimal("12500.00")));

        StepVerifier.create(adapter.getTotalBalance("user-uuid-1"))
                .expectNext(new BigDecimal("12500.00")).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_returnIncome_when_getMonthlyIncome() {
        when(sqlQueryLoader.load("transactions/obtener_ingresos_mensuales")).thenReturn("SELECT ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        RowsFetchSpec<BigDecimal> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.just(new BigDecimal("4500.00")));

        StepVerifier.create(adapter.getMonthlyIncome("user-uuid-1", 4, 2026))
                .expectNext(new BigDecimal("4500.00")).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_returnExpenses_when_getMonthlyExpenses() {
        when(sqlQueryLoader.load("transactions/obtener_gastos_mensuales")).thenReturn("SELECT ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        RowsFetchSpec<BigDecimal> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.just(new BigDecimal("2800.00")));

        StepVerifier.create(adapter.getMonthlyExpenses("user-uuid-1", 4, 2026))
                .expectNext(new BigDecimal("2800.00")).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_saveAndReturnTransaction_when_saveIsSuccessful() {
        when(sqlQueryLoader.load("transactions/registrar_transaccion")).thenReturn("INSERT INTO ...");
        when(databaseClient.sql("INSERT INTO ...")).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        when(sqlQueryLoader.load("transactions/obtener_transaccion_por_id")).thenReturn("SELECT ...");
        when(databaseClient.sql("SELECT ...")).thenReturn(executeSpec);
        when(executeSpec.bind("id", "tx-uuid-1")).thenReturn(executeSpec);
        RowsFetchSpec<TransactionsDto> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.just(sampleTransaction));

        StepVerifier.create(adapter.save(sampleTransaction))
                .expectNextMatches(tx -> tx.getId().equals("tx-uuid-1"))
                .verifyComplete();
    }

    @Test
    void should_return1_when_deleteByIdAndUserIdSuccessful() {
        when(sqlQueryLoader.load("transactions/eliminar_transaccion_por_id_y_usuario")).thenReturn("DELETE ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("id", "tx-uuid-1")).thenReturn(executeSpec);
        when(executeSpec.bind("userId", "user-uuid-1")).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(adapter.deleteByIdAndUserId("tx-uuid-1", "user-uuid-1"))
                .expectNext(1L).verifyComplete();
    }

    @Test
    void should_return0_when_deleteByIdAndUserIdNotFound() {
        when(sqlQueryLoader.load("transactions/eliminar_transaccion_por_id_y_usuario")).thenReturn("DELETE ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("id", "tx-inexistente")).thenReturn(executeSpec);
        when(executeSpec.bind("userId", "user-uuid-1")).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(0L));

        StepVerifier.create(adapter.deleteByIdAndUserId("tx-inexistente", "user-uuid-1"))
                .expectNext(0L).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_returnTrue_when_transactionExists() {
        when(sqlQueryLoader.load("transactions/verificar_existencia_transaccion")).thenReturn("SELECT COUNT ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("id", "tx-uuid-1")).thenReturn(executeSpec);
        RowsFetchSpec<Boolean> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsById("tx-uuid-1")).expectNext(true).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_returnFalse_when_transactionDoesNotExist() {
        when(sqlQueryLoader.load("transactions/verificar_existencia_transaccion")).thenReturn("SELECT COUNT ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("id", "tx-inexistente")).thenReturn(executeSpec);
        RowsFetchSpec<Boolean> typedFetchSpec = mock(RowsFetchSpec.class);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(typedFetchSpec);
        when(typedFetchSpec.one()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.existsById("tx-inexistente")).expectNext(false).verifyComplete();
    }

    /**
     * Verifica que update retorna 1 cuando la transacción existe y pertenece al usuario.
     */
    @Test
    void should_return1_when_updateSuccessful() {
        when(sqlQueryLoader.load("transactions/actualizar_transaccion")).thenReturn("UPDATE ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(adapter.update(sampleTransaction))
                .expectNext(1L).verifyComplete();
    }

    /**
     * Verifica que update retorna 0 cuando la transacción no existe o no pertenece al usuario.
     */
    @Test
    void should_return0_when_updateNotFoundOrNotOwned() {
        when(sqlQueryLoader.load("transactions/actualizar_transaccion")).thenReturn("UPDATE ...");
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(0L));

        TransactionsDto nonExistent = TransactionsDto.builder()
                .id("tx-inexistente")
                .userId("user-uuid-1")
                .description("No existe")
                .amount(new BigDecimal("100.00"))
                .category("food")
                .type("expense")
                .transactionDate(LocalDate.of(2026, 4, 21))
                .notes("")
                .build();

        StepVerifier.create(adapter.update(nonExistent))
                .expectNext(0L).verifyComplete();
    }
}
