package com.finanzas.personales.finanzas.deudas.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.config.SqlQueryLoader;
import com.finanzas.personales.finanzas.deudas.domain.model.PaymentFrequency;
import com.finanzas.personales.finanzas.deudas.domain.port.PaymentFrequencyRepositoryPort;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida que implementa {@link PaymentFrequencyRepositoryPort} usando R2DBC.
 * Gestiona la tabla maestra de frecuencias de pago (mensual, quincenal).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2dbcPaymentFrequencyAdapter implements PaymentFrequencyRepositoryPort {

    private final DatabaseClient databaseClient;
    private final SqlQueryLoader sqlQueryLoader;

    /**
     * Obtiene todas las frecuencias de pago disponibles.
     *
     * @return {@code Flux<PaymentFrequency>} con las frecuencias
     */
    @Override
    public Flux<PaymentFrequency> findAll() {
        log.debug("[R2DBC] Listando frecuencias de pago");
        String sql = sqlQueryLoader.load("deudas/listar_frecuencias_pago");
        return databaseClient.sql(sql)
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Busca una frecuencia de pago por su ID.
     *
     * @param id identificador de la frecuencia
     * @return {@code Mono<PaymentFrequency>} con la frecuencia, o vacío si no existe
     */
    @Override
    public Mono<PaymentFrequency> findById(String id) {
        log.debug("[R2DBC] Buscando frecuencia de pago por id: {}", id);
        String sql = sqlQueryLoader.load("deudas/obtener_frecuencia_por_id");
        return databaseClient.sql(sql)
                .bind("id", id)
                .map(this::mapRowToDomain)
                .one();
    }

    /**
     * Busca una frecuencia de pago por su nombre.
     *
     * @param name nombre de la frecuencia (mensual, quincenal)
     * @return {@code Mono<PaymentFrequency>} con la frecuencia, o vacío si no existe
     */
    @Override
    public Mono<PaymentFrequency> findByName(String name) {
        log.debug("[R2DBC] Buscando frecuencia de pago por nombre: {}", name);
        return databaseClient.sql("SELECT * FROM payment_frequencies WHERE name = :name")
                .bind("name", name)
                .map(this::mapRowToDomain)
                .all()
                .next();
    }

    /**
     * Mapea una fila del ResultSet al modelo de dominio {@link PaymentFrequency}.
     *
     * @param row fila del resultado SQL
     * @return objeto de dominio {@link PaymentFrequency}
     */
    private PaymentFrequency mapRowToDomain(Row row, RowMetadata metadata) {
        return PaymentFrequency.builder()
                .id((String) row.get("id"))
                .name((String) row.get("name"))
                .daysBetweenPayments((Integer) row.get("days_between_payments"))
                .build();
    }
}
