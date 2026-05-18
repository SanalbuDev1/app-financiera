-- Actualiza los campos de una deuda existente por su ID.
-- Parámetros: :creditor, :description, :currentBalance, :interestRate,
--             :interestRateType, :remainingInstallments, :installmentAmount,
--             :nextPaymentDate, :status, :notes, :id
UPDATE debts
SET
    creditor               = :creditor,
    description            = :description,
    current_balance        = :currentBalance,
    interest_rate          = :interestRate,
    interest_rate_type     = :interestRateType,
    remaining_installments = :remainingInstallments,
    installment_amount     = :installmentAmount,
    next_payment_date      = :nextPaymentDate,
    status                 = :status,
    notes                  = :notes
WHERE id = :id
