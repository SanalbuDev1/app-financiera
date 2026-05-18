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
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtPaymentRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtScheduleRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtTypeRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.PaymentFrequencyRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.usecase.CalculateAmortizationUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.CreateDebtUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.ListDebtsUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.GetDebtDetailUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.UpdateDebtUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.DeleteDebtUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.RegisterPaymentUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.GetDebtSummaryUseCase;
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

    // =========================================================
    // Deudas Use Cases
    // =========================================================

    /**
     * Registra {@link CalculateAmortizationUseCase} como bean de Spring.
     * Este use case es también inyectado en {@link CreateDebtUseCase}.
     *
     * @return instancia del caso de uso de cálculo de amortización francesa
     */
    @Bean
    public CalculateAmortizationUseCase calculateAmortizationUseCase() {
        return new CalculateAmortizationUseCase();
    }

    /**
     * Registra {@link CreateDebtUseCase} como bean de Spring.
     *
     * @param debtRepositoryPort          puerto para persistir deudas
     * @param debtScheduleRepositoryPort  puerto para persistir el cronograma
     * @param debtTypeRepositoryPort      puerto para consultar tipos de deuda
     * @param paymentFrequencyRepositoryPort puerto para consultar frecuencias de pago
     * @param calculateAmortizationUseCase   caso de uso de cálculo de amortización
     * @return instancia del caso de uso de creación de deuda
     */
    @Bean
    public CreateDebtUseCase createDebtUseCase(
            DebtRepositoryPort debtRepositoryPort,
            DebtScheduleRepositoryPort debtScheduleRepositoryPort,
            DebtTypeRepositoryPort debtTypeRepositoryPort,
            PaymentFrequencyRepositoryPort paymentFrequencyRepositoryPort,
            CalculateAmortizationUseCase calculateAmortizationUseCase) {
        return new CreateDebtUseCase(debtRepositoryPort, debtScheduleRepositoryPort,
                debtTypeRepositoryPort, paymentFrequencyRepositoryPort, calculateAmortizationUseCase);
    }

    /**
     * Registra {@link ListDebtsUseCase} como bean de Spring.
     *
     * @param debtRepositoryPort puerto para consultar deudas
     * @return instancia del caso de uso de listado de deudas
     */
    @Bean
    public ListDebtsUseCase listDebtsUseCase(DebtRepositoryPort debtRepositoryPort) {
        return new ListDebtsUseCase(debtRepositoryPort);
    }

    /**
     * Registra {@link GetDebtDetailUseCase} como bean de Spring.
     *
     * @param debtRepositoryPort         puerto para consultar deudas
     * @param debtScheduleRepositoryPort puerto para consultar el cronograma
     * @return instancia del caso de uso de detalle de deuda
     */
    @Bean
    public GetDebtDetailUseCase getDebtDetailUseCase(
            DebtRepositoryPort debtRepositoryPort,
            DebtScheduleRepositoryPort debtScheduleRepositoryPort) {
        return new GetDebtDetailUseCase(debtRepositoryPort, debtScheduleRepositoryPort);
    }

    /**
     * Registra {@link UpdateDebtUseCase} como bean de Spring.
     *
     * @param debtRepositoryPort puerto para actualizar deudas
     * @return instancia del caso de uso de actualización de deuda
     */
    @Bean
    public UpdateDebtUseCase updateDebtUseCase(DebtRepositoryPort debtRepositoryPort) {
        return new UpdateDebtUseCase(debtRepositoryPort);
    }

    /**
     * Registra {@link DeleteDebtUseCase} como bean de Spring.
     *
     * @param debtRepositoryPort puerto para eliminar deudas
     * @return instancia del caso de uso de eliminación de deuda
     */
    @Bean
    public DeleteDebtUseCase deleteDebtUseCase(DebtRepositoryPort debtRepositoryPort) {
        return new DeleteDebtUseCase(debtRepositoryPort);
    }

    /**
     * Registra {@link RegisterPaymentUseCase} como bean de Spring.
     *
     * @param debtRepositoryPort         puerto para actualizar deudas
     * @param debtPaymentRepositoryPort  puerto para persistir pagos
     * @param debtScheduleRepositoryPort puerto para actualizar el cronograma
     * @param calculateAmortizationUseCase caso de uso de cálculo (para regenerar cronograma)
     * @return instancia del caso de uso de registro de pagos
     */
    @Bean
    public RegisterPaymentUseCase registerPaymentUseCase(
            DebtRepositoryPort debtRepositoryPort,
            DebtPaymentRepositoryPort debtPaymentRepositoryPort,
            DebtScheduleRepositoryPort debtScheduleRepositoryPort,
            CalculateAmortizationUseCase calculateAmortizationUseCase) {
        return new RegisterPaymentUseCase(debtRepositoryPort, debtPaymentRepositoryPort,
                debtScheduleRepositoryPort, calculateAmortizationUseCase);
    }

    /**
     * Registra {@link GetDebtSummaryUseCase} como bean de Spring.
     *
     * @param debtRepositoryPort puerto para consultar resúmenes agregados
     * @return instancia del caso de uso de resumen de deudas
     */
    @Bean
    public GetDebtSummaryUseCase getDebtSummaryUseCase(DebtRepositoryPort debtRepositoryPort) {
        return new GetDebtSummaryUseCase(debtRepositoryPort);
    }
}
