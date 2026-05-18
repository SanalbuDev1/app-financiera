-- Elimina todos los ítems del cronograma de una deuda.
-- Se usa antes de regenerar el cronograma tras un pago extraordinario.
-- Parámetro: :debtId
DELETE FROM debt_schedule
WHERE debt_id = :debtId
  AND status = 'pending'
