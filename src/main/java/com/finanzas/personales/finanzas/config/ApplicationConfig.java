package com.finanzas.personales.finanzas.config;

import com.finanzas.personales.finanzas.transacciones.domain.model.port.TransactionRepositoryPort;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.CreateTransactionUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.DeleteTransactionUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.GetAllTransactionsUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.GetSummaryUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.ListTransactionsUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.UpdateTransactionUseCase;
import com.finanzas.personales.finanzas.auth.domain.port.UserRepositoryPort;
import com.finanzas.personales.finanzas.auth.domain.usecase.LoginUseCase;
import com.finanzas.personales.finanzas.auth.domain.usecase.RegisterUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de la capa de aplicación.
 * Declara los casos de uso del dominio como beans de Spring para que puedan
 * ser inyectados en los adaptadores de entrada, sin necesidad de anotar
 * las clases del dominio con anotaciones de Spring (@Component, @Service, etc.).
 * Esto mantiene el dominio completamente libre de dependencias de frameworks.
 */
@Configuration
public class ApplicationConfig {

    // =========================================================
    // Auth Use Cases
    // =========================================================

    /**
     * Registra {@link LoginUseCase} como bean de Spring.
     *
     * @param userRepositoryPort puerto de salida para consultar usuarios
     * @param passwordEncoder    encoder BCrypt para verificar contraseñas
     * @return instancia del caso de uso de login
     */
    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepositoryPort,
                                     PasswordEncoder passwordEncoder) {
        return new LoginUseCase(userRepositoryPort, passwordEncoder);
    }

    /**
     * Registra {@link RegisterUseCase} como bean de Spring.
     *
     * @param userRepositoryPort puerto de salida para persistir nuevos usuarios
     * @param passwordEncoder    encoder BCrypt para hashear contraseñas
     * @return instancia del caso de uso de registro
     */
    @Bean
    public RegisterUseCase registerUseCase(UserRepositoryPort userRepositoryPort,
                                           PasswordEncoder passwordEncoder) {
        return new RegisterUseCase(userRepositoryPort, passwordEncoder);
    }

    // =========================================================
    // Transaction Use Cases
    // =========================================================

    /**
     * Registra {@link CreateTransactionUseCase} como bean de Spring.
     *
     * @param transactionRepositoryPort puerto de salida para persistir transacciones
     * @return instancia del caso de uso de creación
     */
    @Bean
    public CreateTransactionUseCase createTransactionUseCase(TransactionRepositoryPort transactionRepositoryPort) {
        return new CreateTransactionUseCase(transactionRepositoryPort);
    }

    /**
     * Registra {@link ListTransactionsUseCase} como bean de Spring.
     *
     * @param transactionRepositoryPort puerto de salida para consultar transacciones
     * @return instancia del caso de uso de listado paginado
     */
    @Bean
    public ListTransactionsUseCase listTransactionsUseCase(TransactionRepositoryPort transactionRepositoryPort) {
        return new ListTransactionsUseCase(transactionRepositoryPort);
    }

    /**
     * Registra {@link DeleteTransactionUseCase} como bean de Spring.
     *
     * @param transactionRepositoryPort puerto de salida para eliminar transacciones
     * @return instancia del caso de uso de eliminación
     */
    @Bean
    public DeleteTransactionUseCase deleteTransactionUseCase(TransactionRepositoryPort transactionRepositoryPort) {
        return new DeleteTransactionUseCase(transactionRepositoryPort);
    }

    /**
     * Registra {@link GetSummaryUseCase} como bean de Spring.
     *
     * @param transactionRepositoryPort puerto de salida para consultas de agregación
     * @return instancia del caso de uso de resumen financiero
     */
    @Bean
    public GetSummaryUseCase getSummaryUseCase(TransactionRepositoryPort transactionRepositoryPort) {
        return new GetSummaryUseCase(transactionRepositoryPort);
    }

    /**
     * Registra {@link GetAllTransactionsUseCase} como bean de Spring.
     *
     * @param transactionRepositoryPort puerto de salida para listar transacciones
     * @return instancia del caso de uso para obtener todas las transacciones
     */
    @Bean
    public GetAllTransactionsUseCase getAllTransactionsUseCase(TransactionRepositoryPort transactionRepositoryPort) {
        return new GetAllTransactionsUseCase(transactionRepositoryPort);
    }

    /**
     * Registra {@link UpdateTransactionUseCase} como bean de Spring.
     *
     * @param transactionRepositoryPort puerto de salida para actualizar transacciones
     * @return instancia del caso de uso de actualización
     */
    @Bean
    public UpdateTransactionUseCase updateTransactionUseCase(TransactionRepositoryPort transactionRepositoryPort) {
        return new UpdateTransactionUseCase(transactionRepositoryPort);
    }
}
