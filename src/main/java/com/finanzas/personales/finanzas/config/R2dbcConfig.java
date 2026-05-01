package com.finanzas.personales.finanzas.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

import java.time.Duration;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

/**
 * Configuración explícita de la conexión R2DBC a PostgreSQL.
 * Centraliza el control del pool de conexiones y los parámetros de conexión
 * en la capa de infraestructura. Lee todos los valores desde {@code application.properties}
 * y construye el {@link ConnectionFactory}, {@link ConnectionPool} y {@link DatabaseClient}.
 * La auto-configuración de Spring Boot R2DBC está deshabilitada en {@code FinanzasApplication}.
 */
@Slf4j
@Configuration
public class R2dbcConfig {

    /** URL de conexión R2DBC (ej. r2dbc:postgresql://localhost:5432/finanzas_db). */
    @Value("${spring.r2dbc.url}")
    private String url;

    /** Usuario de la base de datos. */
    @Value("${spring.r2dbc.username}")
    private String username;

    /** Contraseña de la base de datos. */
    @Value("${spring.r2dbc.password}")
    private String password;

    /** Número inicial de conexiones en el pool. */
    @Value("${spring.r2dbc.pool.initial-size:5}")
    private int initialSize;

    /** Número máximo de conexiones en el pool. */
    @Value("${spring.r2dbc.pool.max-size:20}")
    private int maxSize;

    /** Tiempo máximo que una conexión puede estar inactiva antes de ser cerrada. */
    @Value("${spring.r2dbc.pool.max-idle-time:30m}")
    private Duration maxIdleTime;

    /** Tiempo máximo de vida de una conexión en el pool. */
    @Value("${spring.r2dbc.pool.max-life-time:1h}")
    private Duration maxLifeTime;

    /**
     * Crea el {@link ConnectionFactory} con pool de conexiones configurado.
     * Parsea la URL R2DBC y aplica credenciales y parámetros del pool.
     *
     * @return {@link ConnectionPool} listo para inyectar en {@link DatabaseClient}
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        // Parsear la URL R2DBC para obtener las opciones base (driver, host, port, database)
        ConnectionFactoryOptions baseOptions = ConnectionFactoryOptions.parse(url);

        // Reconstruir opciones con credenciales explícitas
        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .from(baseOptions)
                .option(USER, username)
                .option(PASSWORD, password)
                .build();

        ConnectionFactory connectionFactory = ConnectionFactories.get(options);

        // Configurar el pool de conexiones con todos los parámetros
        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
                .initialSize(initialSize)
                .maxSize(maxSize)
                .maxIdleTime(maxIdleTime)
                .maxLifeTime(maxLifeTime)
                .build();

        log.info("[R2DBC] Pool configurado: initialSize={}, maxSize={}, maxIdleTime={}, maxLifeTime={}",
                initialSize, maxSize, maxIdleTime, maxLifeTime);

        return new ConnectionPool(poolConfig);
    }

    /**
     * Crea el {@link DatabaseClient} a partir del {@link ConnectionFactory} configurado.
     * Este bean es inyectado en los adaptadores de salida (R2dbcUserAdapter, R2dbcTransactionAdapter).
     *
     * @param connectionFactory pool de conexiones R2DBC
     * @return instancia de DatabaseClient para ejecutar queries reactivos
     */
    @Bean
    public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        return DatabaseClient.builder()
                .connectionFactory(connectionFactory)
                .build();
    }
}
