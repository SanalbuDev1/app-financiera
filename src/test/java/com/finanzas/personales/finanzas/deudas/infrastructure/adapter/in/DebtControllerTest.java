package com.finanzas.personales.finanzas.deudas.infrastructure.adapter.in;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtDetail;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtSummary;
import com.finanzas.personales.finanzas.deudas.domain.usecase.CreateDebtUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.DeleteDebtUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.GetDebtDetailUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.GetDebtSummaryUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.ListDebtsUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.UpdateDebtUseCase;
import com.finanzas.personales.finanzas.security.domain.port.TokenServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link DebtController} usando WebTestClient.
 * Mockea los casos de uso del dominio para aislar el controlador de la infraestructura.
 * El userId se simula mediante {@code @WithMockUser} y el principal del SecurityContext.
 */
@WebFluxTest(DebtController.class)
class DebtControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateDebtUseCase createDebtUseCase;

    @MockitoBean
    private ListDebtsUseCase listDebtsUseCase;

    @MockitoBean
    private GetDebtDetailUseCase getDebtDetailUseCase;

    @MockitoBean
    private UpdateDebtUseCase updateDebtUseCase;

    @MockitoBean
    private DeleteDebtUseCase deleteDebtUseCase;

    @MockitoBean
    private GetDebtSummaryUseCase getDebtSummaryUseCase;

    @MockitoBean
    private TokenServicePort tokenServicePort;

    /** Deuda de dominio reutilizable en los tests. */
    private final Debt mockDebt = Debt.builder()
            .id("debt-001")
            .userId("user-001")
            .debtTypeId("debt-type-bank-loan")
            .debtTypeName("prestamo_bancario")
            .frequencyId("freq-monthly")
            .frequencyName("mensual")
            .creditor("Bancolombia")
            .description("Crédito personal")
            .originalAmount(new BigDecimal("10000000.00"))
            .currentBalance(new BigDecimal("9000000.00"))
            .interestRate(new BigDecimal("1.5"))
            .interestRateType("monthly")
            .totalInstallments(12)
            .remainingInstallments(10)
            .installmentAmount(new BigDecimal("917351.85"))
            .startDate(LocalDate.of(2026, 1, 1))
            .nextPaymentDate(LocalDate.of(2026, 6, 1))
            .status("active")
            .notes("")
            .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
            .build();

    /**
     * Verifica que GET /api/debts retorna 200 OK con la lista de deudas del usuario
     * cuando no se especifica filtro de estado.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return200WithDebtList_when_listDebtsIsSuccessful() {
        when(listDebtsUseCase.execute(any(), any())).thenReturn(Flux.just(mockDebt));

        webTestClient.get()
                .uri("/api/debts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("debt-001")
                .jsonPath("$[0].creditor").isEqualTo("Bancolombia")
                .jsonPath("$[0].status").isEqualTo("active")
                .jsonPath("$[0].progressPercentage").exists();
    }

    /**
     * Verifica que GET /api/debts?status=active filtra por estado correctamente
     * y retorna 200 OK con la lista filtrada.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return200WithFilteredList_when_statusFilterIsProvided() {
        when(listDebtsUseCase.execute(any(), eq("active"))).thenReturn(Flux.just(mockDebt));

        webTestClient.get()
                .uri("/api/debts?status=active")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].status").isEqualTo("active");
    }

    /**
     * Verifica que GET /api/debts retorna 200 OK con lista vacía
     * cuando el usuario no tiene deudas registradas.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return200WithEmptyList_when_noDebtsExist() {
        when(listDebtsUseCase.execute(any(), any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/debts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    /**
     * Verifica que GET /api/debts/{id} retorna 200 OK con la deuda y su cronograma
     * cuando la deuda existe y pertenece al usuario.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return200WithDebtDetail_when_debtExists() {
        DebtDetail detail = new DebtDetail(mockDebt, List.of());
        when(getDebtDetailUseCase.execute(eq("debt-001"), any())).thenReturn(Mono.just(detail));

        webTestClient.get()
                .uri("/api/debts/debt-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.debt.id").isEqualTo("debt-001")
                .jsonPath("$.debt.creditor").isEqualTo("Bancolombia")
                .jsonPath("$.schedule").isArray();
    }

    /**
     * Verifica que GET /api/debts/{id} retorna 404 Not Found
     * cuando la deuda no existe o no pertenece al usuario.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return404_when_debtNotFound() {
        when(getDebtDetailUseCase.execute(eq("debt-999"), any()))
                .thenReturn(Mono.error(new IllegalArgumentException("Deuda no encontrada")));

        webTestClient.get()
                .uri("/api/debts/debt-999")
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * Verifica que POST /api/debts retorna 201 Created con la deuda creada
     * cuando el request es válido.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return201_when_createDebtIsSuccessful() {
        when(createDebtUseCase.execute(any())).thenReturn(Mono.just(mockDebt));

        webTestClient.post()
                .uri("/api/debts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "creditor": "Bancolombia",
                          "description": "Crédito personal",
                          "debtTypeId": "debt-type-bank-loan",
                          "frequencyId": "freq-monthly",
                          "originalAmount": 10000000.00,
                          "interestRate": 1.5,
                          "interestRateType": "monthly",
                          "totalInstallments": 12,
                          "startDate": "2026-01-01"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("debt-001")
                .jsonPath("$.creditor").isEqualTo("Bancolombia")
                .jsonPath("$.status").isEqualTo("active");
    }

    /**
     * Verifica que POST /api/debts retorna 400 Bad Request
     * cuando el request tiene campos obligatorios ausentes.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return400_when_createDebtRequestIsInvalid() {
        webTestClient.post()
                .uri("/api/debts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "description": "Crédito sin acreedor"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * Verifica que PUT /api/debts/{id} retorna 200 OK con la deuda actualizada
     * cuando el request es válido y la deuda pertenece al usuario.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return200_when_updateDebtIsSuccessful() {
        Debt updatedDebt = Debt.builder()
                .id("debt-001")
                .userId("user-001")
                .debtTypeId("debt-type-bank-loan")
                .debtTypeName("prestamo_bancario")
                .frequencyId("freq-monthly")
                .frequencyName("mensual")
                .creditor("Bancolombia Actualizado")
                .description("Crédito personal actualizado")
                .originalAmount(new BigDecimal("10000000.00"))
                .currentBalance(new BigDecimal("9000000.00"))
                .interestRate(new BigDecimal("1.5"))
                .interestRateType("monthly")
                .totalInstallments(12)
                .remainingInstallments(10)
                .installmentAmount(new BigDecimal("917351.85"))
                .startDate(LocalDate.of(2026, 1, 1))
                .nextPaymentDate(LocalDate.of(2026, 6, 1))
                .status("active")
                .notes("Nota actualizada")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();

        when(updateDebtUseCase.execute(any())).thenReturn(Mono.just(updatedDebt));

        webTestClient.put()
                .uri("/api/debts/debt-001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "creditor": "Bancolombia Actualizado",
                          "description": "Crédito personal actualizado",
                          "debtTypeId": "debt-type-bank-loan",
                          "frequencyId": "freq-monthly",
                          "originalAmount": 10000000.00,
                          "interestRate": 1.5,
                          "interestRateType": "monthly",
                          "totalInstallments": 12,
                          "startDate": "2026-01-01",
                          "notes": "Nota actualizada"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.creditor").isEqualTo("Bancolombia Actualizado")
                .jsonPath("$.notes").isEqualTo("Nota actualizada");
    }

    /**
     * Verifica que PUT /api/debts/{id} retorna 404 Not Found
     * cuando la deuda no existe o no pertenece al usuario.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return404_when_updateDebtNotFound() {
        when(updateDebtUseCase.execute(any()))
                .thenReturn(Mono.error(new IllegalArgumentException("Deuda no encontrada")));

        webTestClient.put()
                .uri("/api/debts/debt-999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "creditor": "Acreedor",
                          "description": "Descripción",
                          "debtTypeId": "debt-type-bank-loan",
                          "frequencyId": "freq-monthly",
                          "originalAmount": 1000.00,
                          "interestRate": 1.5,
                          "interestRateType": "monthly",
                          "totalInstallments": 6,
                          "startDate": "2026-01-01"
                        }
                        """)
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * Verifica que DELETE /api/debts/{id} retorna 204 No Content
     * cuando la deuda existe y pertenece al usuario.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return204_when_deleteDebtIsSuccessful() {
        when(deleteDebtUseCase.execute(eq("debt-001"), any())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/debts/debt-001")
                .exchange()
                .expectStatus().isNoContent();
    }

    /**
     * Verifica que DELETE /api/debts/{id} retorna 404 Not Found
     * cuando la deuda no existe o no pertenece al usuario.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return404_when_deleteDebtNotFound() {
        when(deleteDebtUseCase.execute(eq("debt-999"), any()))
                .thenReturn(Mono.error(new IllegalArgumentException("Deuda no encontrada")));

        webTestClient.delete()
                .uri("/api/debts/debt-999")
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * Verifica que GET /api/debts/summary retorna 200 OK con el resumen financiero
     * cuando el usuario tiene deudas activas.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return200WithSummary_when_summaryIsSuccessful() {
        DebtSummary mockSummary = DebtSummary.builder()
                .totalDebts(2)
                .totalBalance(new BigDecimal("12500000.00"))
                .totalOriginalAmount(new BigDecimal("15000000.00"))
                .totalMonthlyPayment(new BigDecimal("1200000.00"))
                .totalPendingInterest(new BigDecimal("500000.00"))
                .averageProgress(new BigDecimal("16.67"))
                .build();

        when(getDebtSummaryUseCase.execute(any())).thenReturn(Mono.just(mockSummary));

        webTestClient.get()
                .uri("/api/debts/summary")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalDebts").isEqualTo(2)
                .jsonPath("$.totalBalance").isEqualTo(12500000.00)
                .jsonPath("$.totalMonthlyPayment").isEqualTo(1200000.00);
    }

}
