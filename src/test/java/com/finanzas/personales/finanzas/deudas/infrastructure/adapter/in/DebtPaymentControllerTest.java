package com.finanzas.personales.finanzas.deudas.infrastructure.adapter.in;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtDetail;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtPayment;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import com.finanzas.personales.finanzas.deudas.domain.usecase.GetDebtDetailUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.RegisterPaymentUseCase;
import com.finanzas.personales.finanzas.security.domain.port.TokenServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link DebtPaymentController} usando WebTestClient.
 * Mockea los casos de uso del dominio para aislar el controlador de la infraestructura.
 */
@WebFluxTest(DebtPaymentController.class)
class DebtPaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RegisterPaymentUseCase registerPaymentUseCase;

    @MockitoBean
    private GetDebtDetailUseCase getDebtDetailUseCase;

    @MockitoBean
    private TokenServicePort tokenServicePort;

    /** Pago de dominio reutilizable en los tests. */
    private final DebtPayment mockPayment = DebtPayment.builder()
            .id("payment-001")
            .debtId("debt-001")
            .paymentDate(LocalDate.of(2026, 5, 1))
            .totalAmount(new BigDecimal("917351.85"))
            .principalAmount(new BigDecimal("767351.85"))
            .interestAmount(new BigDecimal("150000.00"))
            .paymentType("regular")
            .extraPaymentStrategy(null)
            .notes("")
            .createdAt(LocalDateTime.of(2026, 5, 1, 10, 0))
            .build();

    /** Ítem de cronograma reutilizable en los tests. */
    private final DebtScheduleItem mockScheduleItem = DebtScheduleItem.builder()
            .id("schedule-001")
            .debtId("debt-001")
            .installmentNumber(1)
            .dueDate(LocalDate.of(2026, 2, 1))
            .principalAmount(new BigDecimal("767351.85"))
            .interestAmount(new BigDecimal("150000.00"))
            .totalAmount(new BigDecimal("917351.85"))
            .balanceAfter(new BigDecimal("9232648.15"))
            .status("paid")
            .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
            .build();

    /** Deuda de dominio mínima para tests de schedule. */
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
            .currentBalance(new BigDecimal("9232648.15"))
            .interestRate(new BigDecimal("1.5"))
            .interestRateType("monthly")
            .totalInstallments(12)
            .remainingInstallments(11)
            .installmentAmount(new BigDecimal("917351.85"))
            .startDate(LocalDate.of(2026, 1, 1))
            .nextPaymentDate(LocalDate.of(2026, 3, 1))
            .status("active")
            .notes("")
            .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
            .build();

    /**
     * Verifica que POST /api/debts/{id}/payments retorna 201 Created con el pago registrado
     * cuando el request es válido y la deuda existe.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return201_when_registerPaymentIsSuccessful() {
        when(registerPaymentUseCase.execute(any())).thenReturn(Mono.just(mockPayment));

        webTestClient.post()
                .uri("/api/debts/debt-001/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "paymentDate": "2026-05-01",
                          "totalAmount": 917351.85,
                          "paymentType": "regular"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("payment-001")
                .jsonPath("$.debtId").isEqualTo("debt-001")
                .jsonPath("$.paymentType").isEqualTo("regular")
                .jsonPath("$.principalAmount").isEqualTo(767351.85)
                .jsonPath("$.interestAmount").isEqualTo(150000.00);
    }

    /**
     * Verifica que POST /api/debts/{id}/payments retorna 201 Created
     * cuando se registra un pago extraordinario con estrategia reduce_installment.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return201_when_extraPaymentIsRegistered() {
        DebtPayment extraPayment = DebtPayment.builder()
                .id("payment-002")
                .debtId("debt-001")
                .paymentDate(LocalDate.of(2026, 5, 15))
                .totalAmount(new BigDecimal("2000000.00"))
                .principalAmount(new BigDecimal("2000000.00"))
                .interestAmount(BigDecimal.ZERO)
                .paymentType("extra")
                .extraPaymentStrategy("reduce_installment")
                .notes("Abono extraordinario")
                .createdAt(LocalDateTime.now())
                .build();

        when(registerPaymentUseCase.execute(any())).thenReturn(Mono.just(extraPayment));

        webTestClient.post()
                .uri("/api/debts/debt-001/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "paymentDate": "2026-05-15",
                          "totalAmount": 2000000.00,
                          "paymentType": "extra",
                          "extraPaymentStrategy": "reduce_installment",
                          "notes": "Abono extraordinario"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.paymentType").isEqualTo("extra")
                .jsonPath("$.extraPaymentStrategy").isEqualTo("reduce_installment");
    }

    /**
     * Verifica que POST /api/debts/{id}/payments retorna 404 Not Found
     * cuando la deuda no existe o no pertenece al usuario.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return404_when_debtNotFoundOnPayment() {
        when(registerPaymentUseCase.execute(any()))
                .thenReturn(Mono.error(new IllegalArgumentException("Deuda no encontrada")));

        webTestClient.post()
                .uri("/api/debts/debt-999/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "paymentDate": "2026-05-01",
                          "totalAmount": 917351.85,
                          "paymentType": "regular"
                        }
                        """)
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * Verifica que POST /api/debts/{id}/payments retorna 400 Bad Request
     * cuando el totalAmount no se proporciona en el request.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return400_when_paymentRequestIsInvalid() {
        webTestClient.post()
                .uri("/api/debts/debt-001/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "paymentType": "regular"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * Verifica que GET /api/debts/{id}/schedule retorna 200 OK con el cronograma
     * cuando la deuda existe y pertenece al usuario.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return200WithSchedule_when_debtExists() {
        DebtDetail detail = new DebtDetail(mockDebt, List.of(mockScheduleItem));
        when(getDebtDetailUseCase.execute(eq("debt-001"), any())).thenReturn(Mono.just(detail));

        webTestClient.get()
                .uri("/api/debts/debt-001/schedule")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("schedule-001")
                .jsonPath("$[0].installmentNumber").isEqualTo(1)
                .jsonPath("$[0].status").isEqualTo("paid")
                .jsonPath("$[0].totalAmount").isEqualTo(917351.85);
    }

    /**
     * Verifica que GET /api/debts/{id}/schedule retorna 200 OK con lista vacía
     * cuando la deuda no tiene ítems de cronograma aún.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return200WithEmptySchedule_when_noScheduleItems() {
        DebtDetail detail = new DebtDetail(mockDebt, List.of());
        when(getDebtDetailUseCase.execute(eq("debt-001"), any())).thenReturn(Mono.just(detail));

        webTestClient.get()
                .uri("/api/debts/debt-001/schedule")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    /**
     * Verifica que GET /api/debts/{id}/schedule retorna 404 Not Found
     * cuando la deuda no existe o no pertenece al usuario.
     */
    @Test
    @WithMockUser(username = "user-001")
    void should_return404_when_debtNotFoundOnSchedule() {
        when(getDebtDetailUseCase.execute(eq("debt-999"), any()))
                .thenReturn(Mono.error(new IllegalArgumentException("Deuda no encontrada")));

        webTestClient.get()
                .uri("/api/debts/debt-999/schedule")
                .exchange()
                .expectStatus().isNotFound();
    }

}
