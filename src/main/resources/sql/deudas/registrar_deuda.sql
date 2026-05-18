-- Inserta una nueva deuda en la tabla debts.
-- Parámetros: :id, :userId, :debtTypeId, :frequencyId, :creditor, :description,
--             :originalAmount, :currentBalance, :interestRate, :interestRateType,
--             :totalInstallments, :remainingInstallments, :installmentAmount,
--             :startDate, :nextPaymentDate, :status, :notes, :createdAt
INSERT INTO debts (
    id, user_id, debt_type_id, frequency_id, creditor, description,
    original_amount, current_balance, interest_rate, interest_rate_type,
    total_installments, remaining_installments, installment_amount,
    start_date, next_payment_date, status, notes, created_at
) VALUES (
    :id, :userId, :debtTypeId, :frequencyId, :creditor, :description,
    :originalAmount, :currentBalance, :interestRate, :interestRateType,
    :totalInstallments, :remainingInstallments, :installmentAmount,
    :startDate, :nextPaymentDate, :status, :notes, :createdAt
)
