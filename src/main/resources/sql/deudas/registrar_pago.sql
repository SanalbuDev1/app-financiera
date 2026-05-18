-- Inserta un registro de pago en la tabla debt_payments.
-- Parámetros: :id, :debtId, :paymentDate, :totalAmount, :principalAmount,
--             :interestAmount, :paymentType, :extraPaymentStrategy, :notes, :createdAt
INSERT INTO debt_payments (
    id, debt_id, payment_date, total_amount, principal_amount,
    interest_amount, payment_type, extra_payment_strategy, notes, created_at
) VALUES (
    :id, :debtId, :paymentDate, :totalAmount, :principalAmount,
    :interestAmount, :paymentType, :extraPaymentStrategy, :notes, :createdAt
)
